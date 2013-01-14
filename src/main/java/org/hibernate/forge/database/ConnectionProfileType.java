package org.hibernate.forge.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;

public class ConnectionProfileType {
	
	private static Map<String, ConnectionProfileType> ALL_TYPES = null;
	
	private String name = null;
	private String dialect = null;
	private Map<String, List<String>> drivers = null;
	
	public static Map<String, ConnectionProfileType> allTypes() {
		if (ALL_TYPES == null) {
			initializeAllTypes();
		}
		return ALL_TYPES;
	}
	
	private static void initializeAllTypes() {
		ALL_TYPES = new HashMap<String, ConnectionProfileType>();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Node main = XMLParser.parse(cl.getResourceAsStream("connection.profile.types"));
		if (!main.getName().equals("types")) return; // Ill formatted datasource resource
		for (Node node : main.getChildren()) {
			if (node.getName().equals("type")) {
				ConnectionProfileType type = createConnectionProfileType(node);
				ALL_TYPES.put(type.name, type);
			}
		}
	}
	
	private static ConnectionProfileType createConnectionProfileType(Node node) {
		ConnectionProfileType result = new ConnectionProfileType();
		result.name = node.getAttribute("name");
		result.dialect = node.getAttribute("dialect");
		result.drivers = new HashMap<String, List<String>>();
		for (Node driver : node.getChildren()) {
			if (!driver.getName().equals("driver")) continue; // Not a driver
			String driverClass = driver.getAttribute("class");
			if (driverClass == null) continue; // Drivers should have class attribute
			List<String> urls = new ArrayList<String>();
			for (Node url : driver.getChildren()) {
				if (!url.getName().equals("url")) continue; // Not a url
				String value = url.getAttribute("value");
				if (value == null) continue; // URLs should have value attribute
				urls.add(value);
			}
			result.drivers.put(driverClass, urls);
		}
		return result;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDialect() {
		return dialect;
	}
	
	public Map<String, List<String>> getDrivers() {
		return drivers;
	}

}
