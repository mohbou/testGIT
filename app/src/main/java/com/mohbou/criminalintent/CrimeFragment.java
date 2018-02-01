package com.mohbou.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;


import android.text.format.DateFormat;

import java.util.Date;

import io.realm.Realm;

import static android.widget.CompoundButton.*;


public class CrimeFragment extends Fragment {

    private static final String KEY_LOG = "LOG";
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallButton;
    private Realm mRealm;
    private Uri contactUrl;
    private String mContactId;


    public static CrimeFragment newInstance(String crimeId) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mRealm = Realm.getDefaultInstance();


        String crimeId = (String) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity(), mRealm).getCrime(crimeId);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTtitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mCrime.setTtitle(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mDateButton = v.findViewById(R.id.crime_date);
        mDateButton.setText(mCrime.getDate().toString());

        mDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mSolvedCheckBox = v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mReportButton = v.findViewById(R.id.crime_report);

        mReportButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent i = ShareCompat.IntentBuilder.from(getActivity())
                        .setChooserTitle(getString(R.string.send_report))
                        .setType("text/plain")
                        .setSubject(getString(R.string.crime_report_subject))
                        .setText(getCrimeReport())
                        .getIntent();

                startActivity(i);

            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if ((mCrime.getSuspect() != null)) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact,PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        mCallButton = v.findViewById(R.id.call_suspect);
        mCallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri number = Uri.parse("tel:"+getContactPhone(getContactID(mCrime.getSuspect())));
            Intent callIntent = new Intent(Intent.ACTION_DIAL,number);
            startActivity(callIntent);
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(KEY_LOG, "onAttach: ");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {

            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            mDateButton.setText(mCrime.getDate().toString());
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUrl = data.getData();

            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            Cursor c = getActivity().getContentResolver()
                    .query(contactUrl, queryFields, null, null, null);
            try {
                if (c.getCount() == 0) {
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
                getContactID(suspect);
            } finally {
                c.close();
            }

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.crime_delete:
                CrimeLab.get(getActivity(), mRealm).removeCrime(mCrime);
                getActivity().finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRealm != null)
            mRealm.close();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("mid3", "CrimeFragment Enters onPause: " + mCrime.getTtitle());
        CrimeLab.get(getActivity(), mRealm).updateCrime(mCrime);

    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        return getString(R.string.crime_report, mCrime.getTtitle(), dateString, solvedString, suspect);
    }


    private String getContactPhone(String contactID) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = null;
        String where = ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?";
        String[] selectionArgs = new String[] { contactID };
        String sortOrder = null;
        Cursor result = getActivity().getContentResolver()
                .query(uri, projection, where, selectionArgs, sortOrder);
        if (result.moveToFirst()) {
            String phone = result.getString(result.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (phone == null) {
                Log.d("mid3", "getContactPhone: number null");
                result.close();
                return null;
            }
            result.close();
            Log.d("mid3", "getContactPhone: number "+phone);
            return phone;
        }
        Log.d("mid3", "!result.moveToFirst: number null");
        result.close();
        return null;
    }

    private String getContactID(String contactName) {
        contactUrl = ContactsContract.Data.CONTENT_URI;
        String[] queryFields = new String[]{
                ContactsContract.Data.CONTACT_ID
        };

        String where = ContactsContract.Contacts.DISPLAY_NAME +" = ?";
        String[] selectionArgs = new String[] { contactName };

        Cursor c = getActivity().getContentResolver()
                .query(contactUrl, queryFields, where, selectionArgs, null);
        try {
            if (c.getCount() == 0) {
                return null;
            }
            c.moveToFirst();
            mContactId = String.valueOf(c.getLong(0));
             } finally {
            c.close();
        }

        return mContactId;

    }


}
