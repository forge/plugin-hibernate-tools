package org.hibernate.forge.connections;

import java.util.Map;

import javax.inject.Inject;

import org.hibernate.forge.common.Constants;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;

@Alias("connection-profiles")
@Help("Create connection profiles from types to use in projects.")
public class ConnectionProfilePlugin implements Plugin, Constants {

	private static final String LF = System.getProperty("line.separator");

	@Inject
	private Shell shell;

	@Inject
	private ConnectionProfileHelper connectionProfileHelper;

	@Command(value = "list")
	public void showConnectionProfiles(
			@Option(name = NAME, help = NAME_HELP, required = false, completer = ConnectionProfileNameCompleter.class) String name,
			final PipeOut out) {
		Map<String, ConnectionProfile> connectionProfiles = connectionProfileHelper
				.loadConnectionProfiles();
		if (name != null) {
			ConnectionProfile descriptor = connectionProfiles.get(name);
			if (descriptor == null) {
				noConnectionProfile(name, out);
			} else {
				shell.println();
				printInfo(descriptor);
				shell.println();
			}
		} else if (connectionProfiles.isEmpty()) {
			shell.println(LF
					+ "There are no connection profiles configured for the current user."
					+ LF);
		} else {
			shell.println();
			for (ConnectionProfile descriptor : connectionProfiles.values()) {
				printInfo(descriptor);
				shell.println();
			}
		}
	}

	@Command(value = "create")
	public void createConnectionProfile(
			@Option(name = NAME, help = NAME_HELP, required = true) String name,
			@Option(name = TYPE, help = TYPE_HELP, required = false, completer = ConnectionProfileTypeCompleter.class) String type,
			@Option(name = DIALECT, help = DIALECT_HELP, required = false) String dialect,
			@Option(name = DRIVER, help = DRIVER_HELP, required = false) String driver,
			@Option(name = PATH_TO_DRIVER, help = PATH_TO_DRIVER_HELP, required = false) String path,
			@Option(name = URL, help = URL_HELP, required = false) String url,
			@Option(name = USER, help = USER_HELP, required = false) String user,
			final PipeOut out) {
		Map<String, ConnectionProfile> connectionProfiles = connectionProfileHelper
				.loadConnectionProfiles();
		ConnectionProfile connectionProfile = null;
		if (connectionProfiles.containsKey(name)) {
			if (!overwriteConnectionProfile(name)) {
				return;
			} else {
				connectionProfile = connectionProfiles.get(name);			}
		}
		if (connectionProfile == null) {
			connectionProfile = new ConnectionProfile();
		}
		editConnectionProfile(
				connectionProfiles, 
				connectionProfile, 
				name, 
				type, 
				dialect, 
				driver, 
				path, 
				url, 
				user, 
				out);
	}

	@Command(value = "edit")
	public void editConnectionProfile(
			@Option(name = NAME, help = NAME_HELP, required = true) String name,
			@Option(name = TYPE, help = TYPE_HELP, required = false, completer = ConnectionProfileTypeCompleter.class) String type,
			@Option(name = DIALECT, help = DIALECT_HELP, required = false) String dialect,
			@Option(name = DRIVER, help = DRIVER_HELP, required = false) String driver,
			@Option(name = PATH_TO_DRIVER, help = PATH_TO_DRIVER_HELP, required = false) String path,
			@Option(name = URL, help = URL_HELP, required = false) String url,
			@Option(name = USER, help = USER_HELP, required = false) String user,
			final PipeOut out) {
		Map<String, ConnectionProfile> connectionProfiles = connectionProfileHelper
				.loadConnectionProfiles();
		ConnectionProfile connectionProfile = connectionProfiles.get(name);
		if (connectionProfile == null) {
			noConnectionProfile(name, out);
		} else {
			editConnectionProfile(
					connectionProfiles, 
					connectionProfile, 
					name, 
					type,
					dialect, 
					driver, 
					path, 
					url, 
					user, 
					out);
		}
	}

	@Command(value = "remove")
	public void removeConnectionProfile(
			@Option(name = NAME, help = NAME_HELP, required = false, completer = ConnectionProfileNameCompleter.class) String name,
			final PipeOut out) {
		Map<String, ConnectionProfile> connectionProfiles = connectionProfileHelper
				.loadConnectionProfiles();
		ConnectionProfile connectionProfile = connectionProfiles.get(name);
		if (connectionProfile == null) {
			noConnectionProfile(name, out);
		} else {
			connectionProfiles.remove(name);
			connectionProfileHelper.saveConnectionProfiles(connectionProfiles.values());
			removeSuccess(name, out);
		}
	}

	private void printInfo(ConnectionProfile connectionProfile) {
		shell.println("Connection profile \"" + connectionProfile.name + "\":" + LF
				+ "  dialect:         " + connectionProfile.dialect + LF
				+ "  driver class:    " + connectionProfile.driver + LF
				+ "  driver location: " + connectionProfile.path + LF
				+ "  url:             " + connectionProfile.url + LF
				+ "  user:            " + connectionProfile.user);
	}
	
	private void noConnectionProfile(String name, PipeOut out) {
		ShellMessages.warn(out, "There is no connection profile named \"" + name
				+ "\" configured for the current user.");	
	}

	private void removeSuccess(String name, PipeOut out) {
		ShellMessages.success(out, "Connection profile named \"" + name
				+ "\" is removed succesfully.");	
	}

	private void saveSuccess(ConnectionProfile connectionProfile, PipeOut out) {
		ShellMessages.success(out, LF + "Connection profile \"" + connectionProfile.name
				+ "\" was saved succesfully:" + LF + "  dialect:         "
				+ connectionProfile.dialect + LF + "  driver class:    "
				+ connectionProfile.driver + LF + "  driver location: "
				+ connectionProfile.path + LF
				+ "  url:             " + connectionProfile.url + LF
				+ "  user:            " + connectionProfile.user);
	}

	private boolean overwriteConnectionProfile(String name) {
		return shell.promptBoolean("Overwrite existing connection profile named "
				+ name + "?", false);
	}
	
	private void editConnectionProfile(
			Map<String, ConnectionProfile> connectionProfiles,
			ConnectionProfile connectionProfile,
			String name,
			String type,
			String dialect,
			String driver,
			String path,
			String url,
			String user,
			final PipeOut out) {
		ConnectionProfileType connectionProfileType = ConnectionProfileType.allTypes().get(type);
		connectionProfile.name = name;
		connectionProfile.dialect = connectionProfileHelper.determineDialect(
				DIALECT_PROMPT, dialect, connectionProfileType);
		connectionProfile.driver = connectionProfileHelper
				.determineDriverClass(DRIVER_PROMPT, driver, connectionProfileType);
		connectionProfile.path = connectionProfileHelper
				.determineDriverPath(PATH_TO_DRIVER_PROMPT, path, connectionProfileType);
		connectionProfile.url = connectionProfileHelper.determineURL(URL_PROMPT, url,
				connectionProfileType, connectionProfile.driver);
		connectionProfile.user = connectionProfileHelper.determineUser(USER_PROMPT, user);
		connectionProfiles.put(name, connectionProfile);
		connectionProfileHelper.saveConnectionProfiles(connectionProfiles.values());
		saveSuccess(connectionProfile, out);
	}

}
