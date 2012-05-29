package org.hibernate.forge.datasource;

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

public class DataSourceHelper implements Constants {

	@Inject
	private Configuration configuration;

	@Inject
	private Shell shell;

	public Map<String, DataSourceDescriptor> loadDataSources() {
		HashMap<String, DataSourceDescriptor> result = new HashMap<String, DataSourceDescriptor>();
		Configuration config = configuration
				.getScopedConfiguration(ConfigurationScope.USER);
		String datasources = config.getString("datasources");
		if (datasources != null) {
			Node node = XMLParser.parse(datasources);
			for (Node child : node.getChildren()) {
				if (!child.getName().equals("datasource"))
					continue; // Only datasource elements are valid
				DataSourceDescriptor descriptor = new DataSourceDescriptor();
				descriptor.name = child.getAttribute(NAME);
				descriptor.dialect = child.getAttribute(DIALECT);
				descriptor.driver = child.getAttribute(DRIVER);
				descriptor.path = child.getAttribute(PATH_TO_DRIVER);
				descriptor.url = child.getAttribute(URL);
				descriptor.user = child.getAttribute(USER);
				result.put(descriptor.name, descriptor);
			}
		}
		return result;
	}

	public void saveDataSources(Collection<DataSourceDescriptor> datasources) {
		Node root = new Node("datasources");
		for (DataSourceDescriptor descriptor : datasources) {
			Node child = root.createChild("datasource");
			child.attribute(NAME, descriptor.name);
			child.attribute(DIALECT, descriptor.dialect);
			child.attribute(DRIVER, descriptor.driver);
			child.attribute(PATH_TO_DRIVER, descriptor.path);
			child.attribute(URL, descriptor.url);
			child.attribute(USER, descriptor.user);
		}
		Configuration config = configuration
				.getScopedConfiguration(ConfigurationScope.USER);
		if (root.getChildren().isEmpty()) {
			config.clearProperty("datasources");
		} else {
			config.setProperty("datasources", XMLParser.toXMLString(root));
		}
	}

	public String determineDialect(String dialect, DataSourceType type) {
		if (dialect != null)
			return dialect;
		if (type != null && type.getDialect() != null)
			return type.getDialect();
		return shell.prompt(DIALECT_PROMPT, DIALECT_DEFAULT);
	}

	public String determineDialect(String dialect, DataSourceDescriptor descriptor) {
		if (dialect != null)
			return dialect;
		if (descriptor != null && descriptor.dialect != null && !"".equals(descriptor.dialect.trim()))
			return descriptor.dialect;
		return shell.prompt(DIALECT_PROMPT, DIALECT_DEFAULT);
	}

	public String determineDriverClass(String driver, DataSourceType type) {
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
		return shell.prompt(DRIVER_PROMPT, DRIVER_DEFAULT);
	}

	public String determineDriverClass(String driver, DataSourceDescriptor descriptor) {
		if (driver != null)
			return driver;
		if (descriptor != null && descriptor.driver != null && !"".equals(descriptor.driver.trim())) {
			return descriptor.driver;
		}
		return shell.prompt(DRIVER_PROMPT, DRIVER_DEFAULT);
	}

	public String determineDriverPath(String path, DataSourceType type) {
		if (path != null)
			return path;
		// TODO resolve driver location in maven repo if possible
		return shell.prompt(PATH_TO_DRIVER_PROMPT, (String) null);
	}

	public String determineDriverPath(String path, DataSourceDescriptor descriptor) {
		if (path != null)
			return path;
		if (descriptor != null && descriptor.path != null && !"".equals(descriptor.path.trim())) { 
			return descriptor.path;
		}
		return shell.prompt(PATH_TO_DRIVER_PROMPT, (String) null);
	}

	public String determineURL(String url, DataSourceType type, String driverClass) {
		if (url != null)
			return url;
		// TODO suggest the proper url format based on the type and the
		// driverClass
		return shell.prompt(URL_PROMPT, URL_DEFAULT);
	}

	public String determineURL(String url, DataSourceDescriptor descriptor) {
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

	public String determineUser(String user, DataSourceDescriptor descriptor) {
		if (user != null)
			return user;
		if (descriptor != null && descriptor.user != null && !"".equals(descriptor.user.trim())) {
			return descriptor.user;
		}
		return shell.prompt(USER_PROMPT, (String) null);
	}

	public String determinePassword() {
		return shell.promptSecret(PASSWORD_PROMPT);
	}

}
