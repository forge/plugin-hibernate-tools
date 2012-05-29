package org.hibernate.forge.datasource;

import java.util.Map;

import javax.inject.Inject;

import org.hibernate.forge.Constants;
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
public class DataSourcePlugin implements Plugin, Constants {

	private static final String LF = System.getProperty("line.separator");

	@Inject
	private Shell shell;

	@Inject
	private DataSourceHelper dataSourceHelper;

	@Command(value = "info")
	public void showDataSource(
			@Option(name = NAME, help = NAME_HELP, required = false, completer = DataSourceNameCompleter.class) String name) {
		Map<String, DataSourceDescriptor> datasources = dataSourceHelper
				.loadDataSources();
		if (name != null) {
			DataSourceDescriptor descriptor = datasources.get(name);
			if (descriptor == null) {
				shell.println(LF + "There is no data source named \"" + name
						+ "\" configured for the current user." + LF);
			} else {
				shell.println();
				printInfo(descriptor);
				shell.println();
			}
		} else if (datasources.isEmpty()) {
			shell.println(LF
					+ "There are no data sources configured for the current user."
					+ LF);
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
			@Option(name = PATH_TO_DRIVER, help = PATH_TO_DRIVER_HELP, required = false) String path,
			@Option(name = URL, help = URL_HELP, required = false) String url,
			@Option(name = USER, help = USER_HELP, required = false) String user,
			final PipeOut out) {
		Map<String, DataSourceDescriptor> datasources = dataSourceHelper
				.loadDataSources();
		if (datasources.containsKey(name) && !overwriteDataSource(name)) {
			return;
		}
		DataSourceDescriptor dataSourceDescriptor = new DataSourceDescriptor();
		DataSourceType dataSourceType = DataSourceType.allTypes().get(type);
		dataSourceDescriptor.name = name;
		dataSourceDescriptor.dialect = dataSourceHelper.determineDialect(
				dialect, dataSourceType);
		dataSourceDescriptor.driver = dataSourceHelper
				.determineDriverClass(driver, dataSourceType);
		dataSourceDescriptor.path = dataSourceHelper
				.determineDriverPath(path, dataSourceType);
		dataSourceDescriptor.url = dataSourceHelper.determineURL(url,
				dataSourceType, dataSourceDescriptor.driver);
		dataSourceDescriptor.user = dataSourceHelper.determineUser(user);
		datasources.put(name, dataSourceDescriptor);
		dataSourceHelper.saveDataSources(datasources.values());
		ShellMessages.success(out, LF + "Data source \"" + name
				+ "\" was saved succesfully:" + LF + "  dialect:         "
				+ dataSourceDescriptor.dialect + LF + "  driver class:    "
				+ dataSourceDescriptor.driver + LF + "  driver location: "
				+ dataSourceDescriptor.path + LF
				+ "  url:             " + dataSourceDescriptor.url + LF
				+ "  user:            " + dataSourceDescriptor.user);
	}

	@Command(value = "remove")
	public void removeDataSource(
			@Option(name = NAME, help = NAME_HELP, required = false, completer = DataSourceNameCompleter.class) String name,
			final PipeOut out) {
		Map<String, DataSourceDescriptor> datasources = dataSourceHelper
				.loadDataSources();
		DataSourceDescriptor descriptor = datasources.get(name);
		if (descriptor == null) {
			ShellMessages.warn(out, "There is no data source named \"" + name
					+ "\" configured for the current user.");
		} else {
			datasources.remove(name);
			dataSourceHelper.saveDataSources(datasources.values());
			ShellMessages.success(out, "Data source named \"" + name
					+ "\" is removed succesfully.");
		}
	}

	private boolean overwriteDataSource(String name) {
		return shell.promptBoolean("Overwrite existing datasource named "
				+ name + "?", false);
	}

	private void printInfo(DataSourceDescriptor descriptor) {
		shell.println("Data source \"" + descriptor.name + "\":" + LF
				+ "  dialect:         " + descriptor.dialect + LF
				+ "  driver class:    " + descriptor.driver + LF
				+ "  driver location: " + descriptor.path + LF
				+ "  url:             " + descriptor.url + LF
				+ "  user:            " + descriptor.user);
	}

}
