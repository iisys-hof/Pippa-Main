package de.iisys.pippa.core.config_manager;

import java.util.ArrayList;

public class Config {

	public ArrayList<PropertyGroup> groups = new ArrayList<PropertyGroup>();

	public Config() {
	}

	public Config(ArrayList<PropertyGroup> groups) {
		// TODO ERROR HANDLING

		this.groups = groups;
	}

}
