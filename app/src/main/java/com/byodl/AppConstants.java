package com.byodl;

import okhttp3.logging.HttpLoggingInterceptor;

public class AppConstants {
    public static final class Config {
        //Splash screen duration in ms
        public static final long SPLASH_DURATION = 1500;
        public static final HttpLoggingInterceptor.Level API_DEBUG_LEVEL = HttpLoggingInterceptor.Level.BODY;
        public static final int FAIL_ATTEMPTS_COUNT = 3;
    }

    public static final class Model {
        public static final String FILENAME = FlavorConstants.Model.FILENAME;
        public static final String LABELS_FILE_NAME = FlavorConstants.Model.LABELS_FILE_NAME;
        public static final String MODEL_VERSION_FILENAME = FlavorConstants.Model.MODEL_VERSION_FILENAME;
    }

    public static final class Database {
        public static final String DB_NAME = FlavorConstants.Database.DB_NAME;
    }
    public static final class Api{
        public static final String BASE_URL = FlavorConstants.Api.BASE_URL;
        public static final String MODEL_LINK = BASE_URL+"model";
    }
}
