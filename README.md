Hibernate Plugin for Forge
==========================

A (for now) simple plugin that makes some of the features of Hibernate Tools availabl from within forge.

Installation
============
    forge install-plugin hibernate-tools

How to use
==========

Before using make sure to copy your jdbc driver to $FORGE_HOME/lib
since otherwise Forge can't connect to your database.

On a project with the persistence facet you can do:

$ generate-entities --url jdbc:hsqldb:localhost:9001 --driver org.hsqldb.jdbcDriver

and it will use JDBC to connect to the database and generate JPA entities.

Commands
========

[generate-entities] - Generate entities from a database.  

[OPTIONS]  
    [--table] - Table pattern to include. 'CUSTOMER' for specific table, 'CUST*' for substring match and '*' for all (the default)  
    [--schema] - Schema pattern to include. Same syntax as for table  
    [--catalog] - Catalog pattern to include. Same syntax as for table  
    [--entityPackage] - Package to use for generated entities.  
    [--driver] - Class name for JDBC driver  
    [--url] - URL for JDBC connection  
    [--user] - Username for JDBC connection  
    [--password] - Password for JDBC connection  
    [--detectManyToMany] - Detect many to many associations between tables.  
    [--detectOneToOne] - Detect one-to-one associations between tables.  
    [--detectOptimisticLock] - Detect optimistic locking tables, i.e. if a table has a column named 'version' with a numeric type optimistic locking will be setup for that table.  

TODO's
======

 * Use "connection profiles" instead having users to specify jdbc  
   details everytime.  

 * Avoid shading so can remove the Loader class to circumvent what
   looks like a bug in Weld for scanning shaded Freemarker jars.

 * Add support for remaining Hibernate Tool features such as template
   path, reveng.xml files and possibly alternative generation options
   than just JPA entities.
    
 * World Domination