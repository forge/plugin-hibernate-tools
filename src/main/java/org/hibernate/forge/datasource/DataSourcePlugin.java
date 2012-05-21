package org.hibernate.forge.datasource;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;

@Alias("datasource")
@Help("Create datasources from templates to use in projects.")
public class DataSourcePlugin implements Plugin, DataSourceConstants
{
   
   private static final String LF = System.getProperty("line.separator");

   @Inject
   private Shell shell;
   
   @Inject
   private DataSourceHelper dataSourceHelper;

   @Command(value = "info")
   public void showDataSource(
            @Option(name = NAME, help = NAME_HELP, required = false, completer = DataSourceNameCompleter.class) String name
            ) {
      Map<String, DataSourceDescriptor> datasources = dataSourceHelper.loadDataSources(); 
      if (name != null) {
         DataSourceDescriptor descriptor = datasources.get(name);
         if (descriptor == null) {
            shell.println(LF + "There is no data source named \"" + name + "\" configured for the current user." + LF);
         } else {
            shell.println();
            printInfo(descriptor);
            shell.println();
         }
      } else if (datasources.isEmpty()) {
         shell.println(LF + "There are no data sources configured for the current user." + LF);
      } else {
         shell.println();
         for (DataSourceDescriptor descriptor : datasources.values()) {
            printInfo(descriptor);
            shell.println();
         }
      }
   }
   
   @Command(value = "add")
   public void addDataSource(
            @Option(name = NAME, help = NAME_HELP, required = true) String name,
            @Option(name = TYPE, help = TYPE_HELP, required = false, completer = DataSourceTypeCompleter.class) String type,
            @Option(name = DIALECT, help = DIALECT_HELP, required = false) String dialect,
            @Option(name = DRIVER, help = DRIVER_HELP, required = false) String driver,
            @Option(name = PATH_TO_DRIVER, help = PATH_TO_DRIVER_HELP, required = false) String pathToDriver,
            @Option(name = URL, help = URL_HELP, required = false) String url,
            @Option(name = USER, help = USER_HELP, required = false) String user,
            final PipeOut out
            )
   {
      Map<String, DataSourceDescriptor> datasources = dataSourceHelper.loadDataSources(); 
      if (datasources.containsKey(name) && !overwriteDataSource(name)) {
         return;
      }
      DataSourceDescriptor dataSourceDescriptor = new DataSourceDescriptor();
      DataSourceType dataSourceType = DataSourceType.allTypes().get(type);
      dataSourceDescriptor.name = name;
      dataSourceDescriptor.dialect = determineDialect(dialect, dataSourceType);
      dataSourceDescriptor.driverClass = determineDriverClass(driver, dataSourceType);
      dataSourceDescriptor.driverLocation = determineDriverLocation(pathToDriver, dataSourceType);
      dataSourceDescriptor.url = determineURL(url, dataSourceType, dataSourceDescriptor.driverClass);
      dataSourceDescriptor.user = determineUser(user);
      datasources.put(name, dataSourceDescriptor);
      dataSourceHelper.saveDataSources(datasources.values());
      ShellMessages.success(
               out, 
               LF +
               "Data source \"" + name + "\" was saved succesfully:"       + LF +
               "  dialect:         " + dataSourceDescriptor.dialect        + LF +
               "  driver class:    " + dataSourceDescriptor.driverClass    + LF +
               "  driver location: " + dataSourceDescriptor.driverLocation + LF +
               "  url:             " + dataSourceDescriptor.url            + LF +
               "  user:            " + dataSourceDescriptor.user);
   }
   
   @Command(value = "remove")
   public void removeDataSource(
            @Option(name = NAME, help = NAME_HELP, required = false, completer = DataSourceNameCompleter.class) String name,
            final PipeOut out
            ) {
      Map<String, DataSourceDescriptor> datasources = dataSourceHelper.loadDataSources(); 
      DataSourceDescriptor descriptor = datasources.get(name);
      if (descriptor == null) {
         ShellMessages.warn(
                  out, 
                  "There is no data source named \"" + name + "\" configured for the current user.");
      } else {
         datasources.remove(name);
         dataSourceHelper.saveDataSources(datasources.values());
         ShellMessages.success(
                  out, 
                  "Data source named \"" + name + "\" is removed succesfully.");
      }
   }
   
     
   private String determineDialect(String dialect, DataSourceType type) {
      if (dialect != null) return dialect;
      if (type != null && type.getDialect() != null) return type.getDialect();
      return shell.prompt(DIALECT_PROMPT, (String)null);
   }
   
   private String determineDriverClass(String driver, DataSourceType type) {
      if (driver != null) return driver;
      if (type != null) {
         ArrayList<String> candidates = new ArrayList<String>(type.getDrivers().keySet());
         if (candidates.size() > 1) {
            return candidates.get(shell.promptChoice(DRIVER_PROMPT, candidates));
         } else if (candidates.size() == 1){
            return candidates.get(0);
         }
      }
      return shell.prompt(DRIVER_PROMPT, (String)null);
   }
   
   private String determineDriverLocation(String location, DataSourceType type) {
      if (location != null) return location;
      // TODO resolve driver location in maven repo if possible
      return shell.prompt(PATH_TO_DRIVER_PROMPT, (String)null);
   }
   
   private String determineURL(String url, DataSourceType type, String driverClass) {
      if (url != null) return url;
      // TODO suggest the proper url format based on the type and the driverClass
      return shell.prompt(URL_PROMPT, (String)null);
   }
   
   private String determineUser(String user) {
      if (user != null) return user;
      return shell.prompt(USER_PROMPT, (String)null);
   }
   
   private boolean overwriteDataSource(String name) {
      return shell.promptBoolean("Overwrite existing datasource named " + name + "?", false);
   }
   
   private void printInfo(DataSourceDescriptor descriptor) {
      shell.println(
               "Data source \"" + descriptor.name + "\":"       + LF +
               "  dialect:         " + descriptor.dialect        + LF +
               "  driver class:    " + descriptor.driverClass    + LF +
               "  driver location: " + descriptor.driverLocation + LF +
               "  url:             " + descriptor.url            + LF +
               "  user:            " + descriptor.user);
   }

}
