package org.hibernate.forge.common;

public interface Constants {

	final String NAME = "name";
	final String NAME_HELP = "Name for this datasource.";

	final String TYPE = "type";
	final String TYPE_HELP = "Predefined datasource type to use.";

	final String DIALECT = "dialect";
	final String DIALECT_HELP = "Dialect to use for the datasource.";
	final String DIALECT_PROMPT = "Enter the dialect to use for the datasource.";
	final String DIALECT_DEFAULT = "org.hibernate.dialect.H2Dialect";

	final String DRIVER = "driver";
	final String DRIVER_HELP = "Class name for the JDBC driver for the datasource.";
	final String DRIVER_PROMPT = "Specify the class name for the JDBC driver for the datasource.";
	final String DRIVER_DEFAULT = "org.h2.Driver";

	final String PATH_TO_DRIVER = "pathToDriver";
	final String PATH_TO_DRIVER_HELP = "Path in the local file system to the jar file containing the JDBC driver.";
	final String PATH_TO_DRIVER_PROMPT = "Enter the path in the local file system to the jar file containing the JDBC driver.";

	final String URL = "url";
	final String URL_HELP = "URL for the JDBC connection.";
	final String URL_PROMPT = "Specify the URL for the JDBC connection.";
	final String URL_DEFAULT = "jdbc:h2:tcp://localhost/sakila";

	final String USER = "user";
	final String USER_HELP = "User name for JDBC connection.";
	final String USER_PROMPT = "Enter the user name for JDBC connection.";
	final String USER_DEFAULT = "sa";

	final String PASSWORD = "password";
	final String PASSWORD_HELP = "Password for JDBC connection.";
	final String PASSWORD_PROMPT = "Enter the password for JDBC connection.";
	
	final String SAVE_PASSWORD = "savePassword";
	final String SAVE_PASSWORD_HELP = "Should password for JDBC connection be saved?";
	final String SAVE_PASSWORD_PROMPT = "Save password for JDBC connection?";
	final String SAVE_PASSWORD_DEFAULT = "false";
	
	final String CONNECTION_PROFILE = "connection-profile";
	final String CONNECTION_PROFILE_HELP = "Name of the connection profile to use.";

	final String TABLE_ID = "table";
	final String TABLE_HELP = "Table pattern to include. 'CUSTOMER' for specific table, 'CUST*' for substring match and '*' for all (the default)";
	final String TABLE_DEFAULT = "*";

	final String SCHEMA_ID = "schema";
	final String SCHEMA_HELP = "Schema pattern to include. 'PRODUCTION' for specific schema, 'PR*' for substring match and '*' for all (the default)";
	final String SCHEMA_DEFAULT = "*";

	final String CATALOG_ID = "catalog";
	final String CATALOG_HELP = "Catalog pattern to include. 'MAIN' for specific schema, 'M*' for substring match and '*' for all (the default)";
	final String CATALOG_DEFAULT = "*";

	final String ENTITY_PACKAGE = "entityPackage";
	final String ENTITY_PACKAGE_HELP = "Package to use for generated entities.";
	final String ENTITY_PACKAGE_PROMPT = "In which package you'd like to generate the entities, or enter for default:";
	
	final String DETECT_MANY_TO_MANY_ID = "detectManyToMany";
	final String DETECT_MANY_TO_MANY_HELP = "Detect many-to-many associations between tables.";
//	final Boolean DETECT_MANY_TO_MANY_DEFAULT = Boolean.TRUE;
//
	final String DETECT_ONE_TO_ONE_ID = "detectOneToOne";
	final String DETECT_ONE_TO_ONE_HELP = "Detect one-to-one associations between tables.";
//	final Boolean DETECT_ONE_TO_ONE_DEFAULT = Boolean.TRUE;
//
	final String DETECT_OPTIMISTIC_LOCK_ID = "detectOptimisticLock";
	final String DETECT_OPTIMISTIC_LOCK_HELP = "Detect optimistic locking tables, i.e. if a table has a column named 'version' with a numeric type optimistic locking will be setup for that table.";
//	final Boolean DETECT_OPTIMISTIC_LOCK_DEFAULT = Boolean.TRUE;

}
