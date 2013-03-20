package org.hibernate.forge.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.hibernate.forge.Constants;
import org.jboss.forge.env.Configuration;
import org.jboss.forge.env.ConfigurationScope;
import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.forge.shell.Shell;

public class ConnectionProfileHelper implements Constants {

	@Inject
	private Configuration configuration;

	@Inject
	private Shell shell;

	public Map<String, ConnectionProfile> loadConnectionProfiles() {
		HashMap<String, ConnectionProfile> result = new HashMap<String, ConnectionProfile>();
		Configuration config = configuration
				.getScopedConfiguration(ConfigurationScope.USER);
		String connectionProfiles = config.getString("connection-profiles");
		if (connectionProfiles != null) {
			Node node = XMLParser.parse(connectionProfiles);
			for (Node child : node.getChildren()) {
				if (!child.getName().equals("connection-profile"))
					continue; // Only profile elements are valid
				ConnectionProfile descriptor = new ConnectionProfile();
				descriptor.name = child.getAttribute(NAME);
				descriptor.dialect = child.getAttribute(DIALECT);
				descriptor.driver = child.getAttribute(DRIVER);
				descriptor.path = child.getAttribute(PATH_TO_DRIVER);
				descriptor.url = child.getAttribute(URL);
				descriptor.user = child.getAttribute(USER);
				descriptor.savePassword = Boolean.getBoolean(child.getAttribute(SAVE_PASSWORD));
				result.put(descriptor.name, descriptor);
			}
		}
		return result;
	}

	public void saveConnectionProfiles(Collection<ConnectionProfile> connectionProfiles) {
		Node root = new Node("connection-profiles");
		for (ConnectionProfile descriptor : connectionProfiles) {
			Node child = root.createChild("connection-profile");
			child.attribute(NAME, descriptor.name);
			child.attribute(DIALECT, descriptor.dialect);
			child.attribute(DRIVER, descriptor.driver);
			child.attribute(PATH_TO_DRIVER, descriptor.path);
			child.attribute(URL, descriptor.url);
			child.attribute(USER, descriptor.user);
			child.attribute(SAVE_PASSWORD, String.valueOf(descriptor.savePassword));
			if (descriptor.savePassword) {
				child.attribute(PASSWORD, descriptor.password);
			}
		}
		Configuration config = configuration
				.getScopedConfiguration(ConfigurationScope.USER);
		if (root.getChildren().isEmpty()) {
			config.clearProperty("connection-profiles");
		} else {
			config.setProperty("connection-profiles", XMLParser.toXMLString(root));
		}
	}

	public String determineDialect(String dialect, ConnectionProfileType type) {
		if (dialect != null)
			return dialect;
		if (type != null && type.getDialect() != null)
			return type.getDialect();
		return determineDialect(DIALECT_DEFAULT);
	}
	
	public String determineDialect(String dialect, ConnectionProfile descriptor) {
		if (dialect != null)
			return dialect;
		if (descriptor != null && descriptor.dialect != null && !"".equals(descriptor.dialect.trim()))
			return descriptor.dialect;
		return determineDialect(DIALECT_DEFAULT);
	}

	public String determineDialect(String defaultDialect) {
		return shell.prompt(DIALECT_PROMPT, defaultDialect);
	}

	public String determineDriverClass(String driver, ConnectionProfileType type) {
		if (driver != null)
			return driver;
		if (type != null) {
			ArrayList<String> candidates = new ArrayList<String>(type
					.getDrivers().keySet());
			if (candidates.size() > 1) {
				return candidates.get(shell.promptChoice(DRIVER_PROMPT,
						candidates));
			} else if (candidates.size() == 1) {
				return candidates.get(0);
			}
		}
		return determineDriverClass(DRIVER_DEFAULT);
	}

	public String determineDriverClass(String driver, ConnectionProfile descriptor) {
		if (driver != null)
			return driver;
		if (descriptor != null && descriptor.driver != null && !"".equals(descriptor.driver.trim())) {
			return descriptor.driver;
		}
		return determineDriverClass(DRIVER_DEFAULT);
	}
	
	public String determineDriverClass(String defaultDriverClass) {
		return shell.prompt(DRIVER_PROMPT, defaultDriverClass);
	}

	public String determineDriverPath(String path, ConnectionProfileType type) {
		if (path != null)
			return path;
		// TODO resolve driver location in maven repo if possible
		return shell.prompt(PATH_TO_DRIVER_PROMPT, (String) null);
	}

	public String determineDriverPath(String path, ConnectionProfile descriptor) {
		if (path != null)
			return path;
		if (descriptor != null && descriptor.path != null && !"".equals(descriptor.path.trim())) { 
			return descriptor.path;
		}
		return shell.prompt(PATH_TO_DRIVER_PROMPT, (String) null);
	}

	public String determineURL(String url, ConnectionProfileType type, String driverClass) {
		if (url != null)
			return url;
		// TODO suggest the proper url format based on the type and the
		// driverClass
		return shell.prompt(URL_PROMPT, URL_DEFAULT);
	}

	public String determineURL(String url, ConnectionProfile descriptor) {
		if (url != null)
			return url;
		if (descriptor != null && descriptor.url != null && !"".equals(descriptor.url.trim())) { 
			return descriptor.url;
		}
		return shell.prompt(URL_PROMPT, URL_DEFAULT);
	}

	public String determineUser(String user) {
		if (user != null)
			return user;
		return shell.prompt(USER_PROMPT, USER_DEFAULT);
	}

	public String determineUser(String user, ConnectionProfile descriptor) {
		if (user != null)
			return user;
		if (descriptor != null && descriptor.user != null && !"".equals(descriptor.user.trim())) {
			return descriptor.user;
		}
		return shell.prompt(USER_PROMPT, (String) null);
	}

	public String determinePassword(String password, ConnectionProfile descriptor) {
		if (password != null) 
			return password;
		if (descriptor != null && descriptor.password != null && !"".equals(descriptor.password.trim())) {
			return descriptor.password;
		}
		return shell.promptSecret(PASSWORD_PROMPT, "");
	}

}
