package de.iisys.pippa.core.config_manager;

public class Item {

	public String label = "";
	public String value = "";
	
	public Item() {}
	
	public Item(String label, String value) {
		
		if(label == null) {
			label = "";
		}
		
		if(value == null) {
			value = "";
		}
		
		this.label = label;
		this.value = value;
	}
}
