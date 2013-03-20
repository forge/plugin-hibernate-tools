package org.hibernate.forge;

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
	
}
