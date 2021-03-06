package com.mohbou.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

import io.realm.Realm;


public class CrimeListFragment extends Fragment {


    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private int mItemUpdatedPosition;
    private boolean mSubtitleVisible;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private Realm mRealm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mRealm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }
        View v = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = v.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI(mItemUpdatedPosition);
        return v;
    }

    private class CrimeHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView mMTitleTextView;
        private TextView mMDateTextView;
        private ImageView mSolvedImageView;
        private Crime mCrime;
        private int mPosition;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));
            itemView.setOnClickListener(this);
            mMTitleTextView = itemView.findViewById(R.id.crime_title);
            mMDateTextView = itemView.findViewById(R.id.crime_date);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved);

        }

        public void bind(Crime crime, int position) {
            mPosition = getAdapterPosition();
            mCrime = crime;
            mMTitleTextView.setText(crime.getTtitle());
            mMDateTextView.setText(crime.getDate().toString());
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);


        }

        @Override
        public void onClick(View view) {
            mItemUpdatedPosition = mPosition;
            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
            startActivity(intent);
        }
    }

    /*private class CrimePoliceHolder extends RecyclerView.ViewHolder
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
*/

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {


        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            this.mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime, position);

        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Enter", "onResume:  mItemUpdatedPosition "+mItemUpdatedPosition);
        updateUI(mItemUpdatedPosition);
    }

    private void updateUI(int position) {
        CrimeLab crimeLab = CrimeLab.get(getActivity(),mRealm);
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            Log.d("mid3", "updateUI: enters on Else");
            mAdapter.setCrimes(crimes);
            for (Crime crime:
                 crimes) {
                Log.d("mid3", "updateUI: loop "+crime.getTtitle()+" id "+crime.getId());
            }
           // mAdapter.notifyItemChanged(mItemUpdatedPosition);
            mAdapter.notifyDataSetChanged();
            mItemUpdatedPosition = RecyclerView.NO_POSITION;
        }
        updateSubtitle();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                crime.setId(UUID.randomUUID().toString());
                Log.d("mid3", "onOptionsItemSelected: "+crime.getId());
                CrimeLab.get(getActivity(),mRealm).addCrime(crime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
                startActivity(intent);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity(),mRealm);
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getString(R.string.subtitle_format, crimeCount);
        if (!mSubtitleVisible)
            subtitle = null;

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mRealm!=null)
            mRealm.close();
    }
}
