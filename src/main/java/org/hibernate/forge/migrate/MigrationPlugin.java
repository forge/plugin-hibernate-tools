package org.hibernate.forge.migrate;

import javax.inject.Inject;

import org.hibernate.forge.common.Constants;
import org.hibernate.forge.connections.ConnectionProfile;
import org.hibernate.forge.connections.ConnectionProfileHelper;
import org.hibernate.forge.connections.ConnectionProfileNameCompleter;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.Plugin;

@Alias("migrate-database")
@Help("Migrate a source database to a target database.")
public class MigrationPlugin implements Plugin, Constants {

	@Inject
	private Shell shell;

	@Inject
	private ConnectionProfileHelper connectionProfileHelper;

	@DefaultCommand
	public void migrateDatabase(
			@Option(name = FROM_CONNECTION_PROFILE, help = FROM_CONNECTION_PROFILE_HELP, required = false, completer = ConnectionProfileNameCompleter.class) String fromConnectionProfileName,
			@Option(name = FROM_URL, help = FROM_URL_HELP, required = false) String fromUrl,
			@Option(name = FROM_USER, help = FROM_USER_HELP, required = false) String fromUser,
			@Option(name = FROM_PASSWORD, help = FROM_PASSWORD_HELP, required = false) String fromPassword,
			@Option(name = FROM_DIALECT, help = FROM_DIALECT_HELP, required = false) String fromDialect,
			@Option(name = FROM_DRIVER, help = FROM_DRIVER_HELP, required = false) String fromDriver,
			@Option(name = FROM_PATH_TO_DRIVER, help = FROM_PATH_TO_DRIVER_HELP, required = false) String fromPath,
//			@Option(name = TO_CONNECTION_PROFILE, help = TO_CONNECTION_PROFILE_HELP, required = false, completer = ConnectionProfileNameCompleter.class) String toConnectionProfileName,
//			@Option(name = TO_URL, help = TO_URL_HELP, required = false) String toUrl,
//			@Option(name = TO_USER, help = TO_USER_HELP, required = false) String toUser,
//			@Option(name = TO_PASSWORD, help = TO_PASSWORD_HELP, required = false) String toPassword,
//			@Option(name = TO_DRIVER, help = TO_DRIVER_HELP, required = false) String toDriver,
//			@Option(name = TO_PATH_TO_DRIVER, help = TO_PATH_TO_DRIVER_HELP, required = false) String toPath,
			@Option(name = TO_DIALECT, help = TO_DIALECT_HELP, required = false) String toDialect) {
		ConnectionProfile fromProfile = buildFromProfile(
				fromConnectionProfileName, fromUrl, fromUser, fromPassword,
				fromDialect, fromDriver, fromPath);
//		ConnectionProfile toProfile = buildToProfile(
//				toConnectionProfileName, toUrl, toUser, toPassword,
//				toDialect, toDriver, toPath);
		ConnectionProfile toProfile = buildToProfile(
				null, null, null, null, toDialect, null, null);
		new MigrationHelper().migrate(fromProfile, toProfile, shell);
	}

	private ConnectionProfile buildFromProfile(String connectionProfile,
			String url, String user, String password, String dialect,
			String driver, String path) {
		ConnectionProfile result = null;
		if (connectionProfile != null) {
			result = connectionProfileHelper.loadConnectionProfiles().get(
					connectionProfile);
		}
		if (result == null) {
			result = new ConnectionProfile();
		}
		result.url = connectionProfileHelper.determineURL(FROM_URL_PROMPT, url,
				result);
		result.user = connectionProfileHelper.determineUser(FROM_USER_PROMPT,
				user, result);
		result.password = connectionProfileHelper.determinePassword(
				FROM_PASSWORD_PROMPT, password, result);
		result.dialect = connectionProfileHelper.determineDialect(
				FROM_DIALECT_PROMPT, dialect, result);
		result.driver = connectionProfileHelper.determineDriverClass(
				FROM_DRIVER_PROMPT, driver, result);
		result.path = connectionProfileHelper.determineDriverPath(
				FROM_PATH_TO_DRIVER_PROMPT, path, result);
		return result;
	}

	private ConnectionProfile buildToProfile(String connectionProfile,
			String url, String user, String password, String dialect,
			String driver, String path) {
		ConnectionProfile result = null;
		if (connectionProfile != null) {
			result = connectionProfileHelper.loadConnectionProfiles().get(
					connectionProfile);
		}
		if (result == null) {
			result = new ConnectionProfile();
		}
//		result.url = connectionProfileHelper.determineURL(TO_URL_PROMPT, url,
//				result);
//		result.user = connectionProfileHelper.determineUser(TO_USER_PROMPT,
//				user, result);
//		result.password = connectionProfileHelper.determinePassword(
//				TO_PASSWORD_PROMPT, password, result);
		result.dialect = connectionProfileHelper.determineDialect(
				TO_DIALECT_PROMPT, dialect, result);
//		result.driver = connectionProfileHelper.determineDriverClass(
//				TO_DRIVER_PROMPT, driver, result);
//		result.path = connectionProfileHelper.determineDriverPath(
//				TO_PATH_TO_DRIVER_PROMPT, path, result);
		return result;
	}

}
