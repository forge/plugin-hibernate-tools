package org.hibernate.forge.datasource;

public interface DataSourceConstants
{

   final String NAME = "name";
   final String NAME_HELP = "Name for this datasource.";
   final String TYPE = "type";
   final String TYPE_HELP = "Predefined datasource type to use.";
   final String DIALECT = "dialect";
   final String DIALECT_HELP = "Dialect to use for the datasource.";
   final String DIALECT_PROMPT = "Enter the dialect to use for the datasource.";
   final String DRIVER = "driver";
   final String DRIVER_HELP = "Class name for the JDBC driver for the datasource.";
   final String DRIVER_PROMPT = "Specify the class name for the JDBC driver for the datasource.";
   final String PATH_TO_DRIVER = "pathToDriver";
   final String PATH_TO_DRIVER_HELP = "Path in the local file system to the jar file containing the JDBC driver.";
   final String PATH_TO_DRIVER_PROMPT = "Enter the path in the local file system to the jar file containing the JDBC driver.";
   final String URL = "url";
   final String URL_HELP = "URL for the JDBC connection.";
   final String URL_PROMPT = "Specify the URL for the JDBC connection.";
   final String USER = "user";
   final String USER_HELP = "Username for JDBC connection.";
   final String USER_PROMPT = "Enter the username for JDBC connection.";
   
}
