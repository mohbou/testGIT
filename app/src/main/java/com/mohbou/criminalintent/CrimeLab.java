package com.mohbou.criminalintent;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class CrimeLab {

    private static CrimeLab sCrimeLab;
    private Realm sRealm;

    public static CrimeLab get(Context context, Realm sRealm) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);

        }

        sCrimeLab.sRealm = sRealm;

        return sCrimeLab;
    }

    private CrimeLab(Context context) {
    }

    public List<Crime> getCrimes() {

        final RealmResults<CrimeRealm> crimeRealms = sRealm.where(CrimeRealm.class).findAll();
        List<Crime> crimes= crimeRealms.stream()
                .map(new Function<CrimeRealm, Crime>() {
                    @Override
                    public Crime apply(CrimeRealm crimeRealm) {
                        Crime crime = new Crime();
                        if (crimeRealm.getDate() != null)
                            crime.setDate(crimeRealm.getDate());
                        else
                            crime.setDate(new Date());
                        crime.setTtitle(crimeRealm.getTtitle());
                        Log.d("mid3", "apply: in getCrimes "+crime.getTtitle());
                        crime.setSolved(crimeRealm.isSolved());
                        crime.setId(crimeRealm.getId());
                        return crime;
                    }
                }).collect(Collectors.<Crime>toList());

        return crimes;

    }

    public Crime getCrime(String id) {

        final CrimeRealm crimeRealm = sRealm.where(CrimeRealm.class)
                .equalTo("mId", id)
                .findFirst();
        if (crimeRealm != null) {
            Crime crime = new Crime();
            if(crimeRealm.getDate()!=null)
            crime.setDate(crimeRealm.getDate());
            else
                crime.setDate(new Date());
            crime.setTtitle(crimeRealm.getTtitle());
            crime.setSolved(crimeRealm.isSolved());
            crime.setId(crimeRealm.getId());

            return crime;
        }
        return null;

    }

    public void addCrime(final Crime c) {
        sRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                CrimeRealm crimeRealm = realm.createObject(CrimeRealm.class);
                if (c.getDate() != null)
                crimeRealm.setDate(c.getDate());
                else
                    crimeRealm.setDate(new Date());
                crimeRealm.setTtitle(c.getTtitle());
                crimeRealm.setSolved(c.isSolved());
                crimeRealm.setId(c.getId());
                Log.d("mid3", "addCrime execute: "+c.getId());

            }
        });
    }

    public void removeCrime(Crime c) {
        final CrimeRealm crimeRealm = sRealm.where(CrimeRealm.class).equalTo("mId", c.getId()).findFirst();

        sRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                crimeRealm.deleteFromRealm();
            }
        });

    }

    public void updateCrime(final Crime crime) {


        sRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                CrimeRealm crimeRealm = realm.where(CrimeRealm.class).equalTo("mId", crime.getId()).findFirst();
                crimeRealm.setDate(crime.getDate());
                crimeRealm.setTtitle(crime.getTtitle());
                crimeRealm.setSolved(crime.isSolved());
                Log.d("mid3", "updateCrime execute: "+crimeRealm.getTtitle());
            }
        });

    }


}
