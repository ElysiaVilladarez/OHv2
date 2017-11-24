package com.oohana.splashscreen;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by SaperiumDev on 11/23/2017.
 */

public class SplashScreenModel implements SplashScreenContract.SplashScreenModelToPresenter {
    private Context c;
    public SplashScreenModel(Context c){
        this.c = c;
    }

    @Override
    public void createRealmConfiguration() {
        Realm.init(c);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(2)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
