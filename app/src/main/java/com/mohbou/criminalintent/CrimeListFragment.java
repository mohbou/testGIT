package com.mohbou.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class CrimeListFragment extends Fragment {


    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    public static final int ITEM_TYPE_NORMAL_ROW = 0;
    public static final int ITEM_TYPE_POLICE_ROW = 1;
    private int mCurrentPosition;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = v.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI(mCurrentPosition);
        return v;
    }

    private class CrimeHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView mMTitleTextView;
        private TextView mMDateTextView;
        private ImageView mSolvedImageView;
        private Crime mCrime;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));
            itemView.setOnClickListener(this);
            mMTitleTextView = itemView.findViewById(R.id.crime_title);
            mMDateTextView = itemView.findViewById(R.id.crime_date);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved);

        }

        public void bind(Crime crime) {
            mCrime = crime;
            mMTitleTextView.setText(crime.getTtitle());
            mMDateTextView.setText(crime.getDate().toString());
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);


        }

        @Override
        public void onClick(View view) {

            mCurrentPosition = getAdapterPosition();
            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
            startActivity(intent);
        }
    }

    private class CrimePoliceHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView mMTitleTextView;
        private TextView mMDateTextView;
        private Button MCallPoliceButton;
        private Crime mCrime;

        public CrimePoliceHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime_police, parent, false));
            mMTitleTextView = itemView.findViewById(R.id.crime_title);
            mMDateTextView = itemView.findViewById(R.id.crime_date);
            MCallPoliceButton = itemView.findViewById(R.id.crime_police);
            MCallPoliceButton.setOnClickListener(this);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mMTitleTextView.setText(crime.getTtitle());
            mMDateTextView.setText(crime.getDate().toString());

        }

        @Override
        public void onClick(View view) {
            Toast.makeText(getActivity(), mCrime.getTtitle() + " call police!", Toast.LENGTH_SHORT).show();
        }
    }


    private class CrimeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            this.mCrimes = crimes;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == ITEM_TYPE_NORMAL_ROW) {
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                return new CrimeHolder(layoutInflater, parent);
            } else if (viewType == ITEM_TYPE_POLICE_ROW) {
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                return new CrimePoliceHolder(layoutInflater, parent);
            } else
                return null;
        }

        @Override
        public int getItemViewType(int position) {
            Crime crime = mCrimes.get(position);
            if (crime.isRequiresPolice()) {
                return ITEM_TYPE_POLICE_ROW;
            } else
                return ITEM_TYPE_NORMAL_ROW;

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final int itemType = getItemViewType(position);
            Crime crime = mCrimes.get(position);
            if (itemType == ITEM_TYPE_NORMAL_ROW) {
                ((CrimeHolder) holder).bind(crime);
            } else if (itemType == ITEM_TYPE_POLICE_ROW) {
                ((CrimePoliceHolder) holder).bind(crime);
            }
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI(mCurrentPosition);
    }

    private void updateUI(int position) {

        if (mAdapter == null) {
            CrimeLab crimeLab = CrimeLab.get(getActivity());
            List<Crime> crimes = crimeLab.getCrimes();
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {

            mAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getString(R.string.subtitle_format,crimeCount);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }
}
