package de.iisys.pippa.core.config_manager;

import de.iisys.pippa.core.config_manager.ConfigTypes.CONFIG_TYPES;

public class Property {

	public String identifier = "";
	public CONFIG_TYPES type = null;
	public String pattern = "";
	public String patternErrorMessage = "";
	public String selected = "";
	public Item item = null;
	
	public String dropdownLabel = "";
	public Item[] dropdownItems = null;

	// constructor for single value properties (input - text, number, date, ...)
	public Property(String identifier, CONFIG_TYPES type, String pattern, String patternErrorMessage, String label, String value) {

		// TODO check config-types

		if (identifier == "" || identifier == null) {
			// TODO throw some stuff return null
		}

		if (type == null) {
			// TODO throw some stuff return null
		}

		if (pattern == null) {
			pattern = "";
		}
		
		if(patternErrorMessage == null) {
			patternErrorMessage = "";
		}

		this.identifier = identifier;
		this.type = type;
		this.pattern = pattern;
		this.patternErrorMessage = patternErrorMessage;
		this.item = new Item(label, value);

	}

	// constructor for single value properties (input - text, number, date, ...)
	public Property(String identifier, CONFIG_TYPES type, String pattern, String patternErrorMessage, Item item) {

		// TODO check config-types

		if (identifier == "" || identifier == null) {
			// TODO throw some stuff return null
		}

		if (type == null) {
			// TODO throw some stuff return null
		}

		if (pattern == null) {
			pattern = "";
		}
		
		if(patternErrorMessage == null) {
			patternErrorMessage = "";
		}

		if (item == null) {
			item = new Item();
		}

		this.identifier = identifier;
		this.type = type;
		this.pattern = pattern;
		this.patternErrorMessage = patternErrorMessage;
		this.item = item;

	}

	// constructor for multi value properties (select)
	public Property(String identifier, CONFIG_TYPES type, String selected, String dropdownLabel, Item[] dropdownItems) {

		// TODO check config-types

		if (identifier == "" || identifier == null) {
			// TODO throw some stuff return null
		}

		if (type == null) {
			// TODO throw some stuff return null
		}
		
		if (selected == null) {
			selected = "";
		}

		if(dropdownLabel == null) {
			dropdownLabel = "";
		}
		
		if (dropdownItems == null) {
			// TODO throw some stuff return null
		}

		this.identifier = identifier;
		this.type = type;
		this.selected = selected;
		this.dropdownLabel = dropdownLabel;
		this.dropdownItems = dropdownItems;

	}
}
