package de.iisys.pippa.core.config_manager;

import java.util.ArrayList;

public class PropertyGroup {

	public String identifier = "";
	public String name = "";
	public String heading = "";
	public String description = "";
	public ArrayList<Property> properties = new ArrayList<Property>();

	public PropertyGroup(String identifier, String name, String heading, String description) {

		// TODO ERROR HANDLING
		this.name = name;
		this.identifier = identifier;
		this.heading = heading;
		this.description = description;
	}

	public PropertyGroup(String identifier, String name, String heading, String description,
			ArrayList<Property> properties) {

		// TODO ERROR HANDLING
		this.identifier = identifier;
		this.name = name;
		this.heading = heading;
		this.description = description;
		this.properties = properties;
	}

}
