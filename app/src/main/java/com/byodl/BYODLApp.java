package com.byodl;

import android.app.Application;

import com.byodl.model.DaoMaster;
import com.byodl.model.DaoSession;
import com.byodl.utils.ModelHelper;
import com.byodl.utils.db.DatabaseUpgradeHelper;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import org.greenrobot.greendao.database.Database;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class BYODLApp extends Application {
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        initFabric();
        initCalligraphy();
        initDao();
        initModel();
    }
    private void initFabric() {
        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

// Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit);
    }

	private void initModel() {
        ModelHelper.init(this,daoSession);
    }

    /**
     * Init calligraphy object
     */
    private void initCalligraphy() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
    /**
     * Init Database framework
     */
    private void initDao() {
        //noinspection ConstantConditions
        DatabaseUpgradeHelper helper = new DatabaseUpgradeHelper(this, AppConstants.Database.DB_NAME);
        //noinspection ConstantConditions
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    /**
     * Get DAO session object
     * @return dao session object
     */
    public DaoSession getDaoSession() {
        return daoSession;
    }

}
