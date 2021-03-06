package com.mohbou.criminalintent;

import android.util.Log;

import java.util.Date;



public class Crime {
    private String mId;
    private String mTtitle;
    private Date mDate;
    private boolean mSolved;
    private boolean mRequiresPolice;
    private String mSuspect;


    public Crime() {

        Log.d("mid", "Crime id : "+mId);
    }
    public void setId(String id) {
        mId = id;
    }
    public String getId() {
        return mId;
    }

    public String getTtitle() {
        return mTtitle;
    }

    public void setTtitle(String ttitle) {
        mTtitle = ttitle;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public boolean isRequiresPolice() {
        return mRequiresPolice;
    }

    public void setRequiresPolice(boolean requiresPolice) {
        mRequiresPolice = requiresPolice;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }
}
