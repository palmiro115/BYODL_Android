package com.byodl;

public class FlavorConstants {
	/**
	 * Model settings
	 */
    public static final class Model {
		/**
		 * Name of mode file in assets
		 */
        public static final String FILENAME = "stripped_graph.pb";
		/**
		 * Name of labels file in assets
		 * Should store one label per line
		 */
        public static final String LABELS_FILE_NAME = "labels.txt";
		/**
		 * Name of file with version in assets
		 * Version in format yyyymmddhhmmss as it stored at backend
		 */
        public static final String MODEL_VERSION_FILENAME = "version.txt";
    }

	/**
	 * Api settings
	 */
	public static final class Api{
		/**
		 * Base api link
		 */
        public static final String BASE_URL = "http://52.41.231.25/";
    }

	/**
	 * Database settings
	 */
	public static final class Database {
		/**
		 * Database file name
		 */
		public static final String DB_NAME = "database";
    }
}
