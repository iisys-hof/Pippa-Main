package de.iisys.pippa.support.config_manager;

import de.iisys.pippa.core.config_manager.Config;
import de.iisys.pippa.core.config_manager.ConfigManager;
import de.iisys.pippa.core.config_manager.Item;
import de.iisys.pippa.core.config_manager.Property;
import de.iisys.pippa.core.config_manager.PropertyGroup;
import de.iisys.pippa.core.config_manager.ConfigTypes.CONFIG_TYPES;
import de.iisys.pippa.core.skill.Skill;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class ConfigManagerImpl implements ConfigManager {
	// load config only once and listen for changes afterwards
	private Gson gson = new Gson();

	private final String SKILL_CONFIG_URL_PREFIX = System.getProperty("user.home") + "/Desktop/SkillConfig/";
	private final String MAIN_CONFIG_URL_PREFIX = System.getProperty("user.home") + "/Desktop/MainConfig/";
	private final String MAIN_CONFIG_FILE = "mainconf.json";

	private static ConfigManagerImpl instance;

	private ConfigManagerImpl() {
	}

	public static synchronized ConfigManagerImpl getInstance() {
		if (ConfigManagerImpl.instance == null) {
			ConfigManagerImpl.instance = new ConfigManagerImpl();
		}
		return ConfigManagerImpl.instance;
	}

	public synchronized Config loadSkillConfig(Skill skillRef) {

		if (skillRef == null) {
			throw new NullPointerException("No Reference to a Skill was given when loading a Configuration.");
		}

		if (!(skillRef instanceof Skill)) {
			throw new IllegalArgumentException("Given Reference not of Type Skill when loading a Configuration.");
		}

		String referenceName = skillRef.getClass().getName();
		FileReader fileReader = null;
		StringBuilder stringBuilder = new StringBuilder();
		String jsonString = "";

		try {
			fileReader = new FileReader(this.SKILL_CONFIG_URL_PREFIX + referenceName + ".json");
		} catch (FileNotFoundException e1) {
			return null;
		}

		try {
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String nextLine;
			while ((nextLine = bufferedReader.readLine()) != null) {
				stringBuilder.append(nextLine).append(" ");
			}
		} catch (Exception e) {
			// TODO exeception handling
			e.printStackTrace();
		}

		jsonString = stringBuilder.toString();

		Config config = null;

		config = this.jsonToConfig(jsonString);

		return config;

	}

	@Override
	public synchronized boolean storeSkillConfig(Skill skillRef, Config skillConfig) throws Exception {

		if (skillRef == null) {
			throw new NullPointerException("No Reference to a Skill was given when loading a Configuration.");
		}

		if (!(skillRef instanceof Skill)) {
			throw new IllegalArgumentException("Given Reference not of Type Skill when loading a Configuration.");
		}

		String referenceName = skillRef.getClass().getName();

		try {

			String jsonString = this.configToJson(skillConfig);

			File directory = new File(this.SKILL_CONFIG_URL_PREFIX);
			if (!directory.exists()) {
				directory.mkdirs();
			}

			FileWriter fileWriter = new FileWriter(this.SKILL_CONFIG_URL_PREFIX + referenceName + ".json");
			fileWriter.write(jsonString);
			fileWriter.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			// TODO some exception handling
			return false;
		}

	}

	@Override
	public synchronized Config loadMainConfig() {

		FileReader fileReader = null;
		StringBuilder stringBuilder = new StringBuilder();
		String jsonString = "";

		try {
			fileReader = new FileReader(this.MAIN_CONFIG_URL_PREFIX + this.MAIN_CONFIG_FILE);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block

			this.setDefaultMainConfig();

		}

		try {
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String nextLine;
			while ((nextLine = bufferedReader.readLine()) != null) {
				stringBuilder.append(nextLine).append(" ");
			}
		} catch (Exception e) {
			// TODO exeception handling
			e.printStackTrace();
		}

		jsonString = stringBuilder.toString();

		Config config = null;

		config = this.jsonToConfig(jsonString);

		return config;

	}

	private boolean storeMainConfig(Config configuration) throws Exception {

		try {

			String jsonString = configToJson(configuration);

			new File(MAIN_CONFIG_URL_PREFIX).mkdirs();
			FileWriter fileWriter = new FileWriter(MAIN_CONFIG_URL_PREFIX + MAIN_CONFIG_FILE);
			fileWriter.write(jsonString);
			fileWriter.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			// TODO some exception handling
			return false;
		}

	}

	private void setDefaultMainConfig() {

		Config configuration = new Config();

		PropertyGroup pg1 = new PropertyGroup("personalInfo", "Personal Information", "Personal Information",
				"Tell PIPPA a little more about yourself. But just as much as you feel comfortable with. All Skills have access to this and can use the information to improve their performance.");

		Property pp1 = new Property("firstName", CONFIG_TYPES.TEXT, "", "", "First Name", "");
		Property pp2 = new Property("lastName", CONFIG_TYPES.TEXT, "", "", "Last Name", "");
		Property pp3 = new Property("dateBirth", CONFIG_TYPES.DATE, "", "", "Date Of Birth", "");
		Property pp4 = new Property("sex", CONFIG_TYPES.DROPDOWN, "sexNotSaying", "Gender",
				new Item[] { new Item("Rather Not Say", "sexNotSaying"), new Item("Femal", "sexFemale"),
						new Item("Male", "sexMale") });
		Property pp5 = new Property("eMail", CONFIG_TYPES.TEXT,
				"(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])",
				"Please enter a valid Email Address or none!", "EMail", "");

		String[] locales = Locale.getISOCountries();
		Item[] countries = new Item[locales.length + 1];
		for (int i = 0; i < locales.length; i++) {
			Locale obj = new Locale("", locales[i]);
			countries[i + 1] = new Item(obj.getDisplayCountry(Locale.ENGLISH), obj.getCountry());
		}
		countries[0] = new Item("Rather Not Say", "countryNotSaying");
		Property pp6 = new Property("country", CONFIG_TYPES.DROPDOWN, "countryNotSaying", "Country", countries);
		Property pp7 = new Property("city", CONFIG_TYPES.TEXT, "", "", "City", "");
		Property pp8 = new Property("postCode", CONFIG_TYPES.NUMBER, "", "", "Post Code", "");
		Property pp9 = new Property("street", CONFIG_TYPES.TEXT, "", "", "Street", "");
		Property pp10 = new Property("houseNumber", CONFIG_TYPES.NUMBER, "", "", "House Number", "");

		pg1.properties.add(pp1);
		pg1.properties.add(pp2);
		pg1.properties.add(pp3);
		pg1.properties.add(pp4);
		pg1.properties.add(pp5);
		pg1.properties.add(pp6);
		pg1.properties.add(pp7);
		pg1.properties.add(pp8);
		pg1.properties.add(pp9);
		pg1.properties.add(pp10);

		PropertyGroup pg2 = new PropertyGroup("generalConfig", "General Configuration", "General Configuration",
				"Here you can configure more hardware related settings. (All settings just for demonstration yet.)");

		Property pp11 = new Property("password", CONFIG_TYPES.PASSWORD, "", "", "Password", "P4ssword");
		Property pp22 = new Property("energyMode", CONFIG_TYPES.CHECKBOX, "", "", "Energy Saving Mode", "true");
		Property pp33 = new Property("syncTime", CONFIG_TYPES.TIME, "", "", "Automatic Sync Time", "");

		pg2.properties.add(pp11);
		pg2.properties.add(pp22);
		pg2.properties.add(pp33);

		configuration.groups.add(pg1);
		configuration.groups.add(pg2);

		try {
			storeMainConfig(configuration);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Config jsonToConfig(String jsonString) {

		Config config = new Config();

		config.groups = new ArrayList<PropertyGroup>(Arrays.asList(gson.fromJson(jsonString, PropertyGroup[].class)));

		return config;

	}

	private String configToJson(Config configuration) {

		JsonElement jsonElement = gson.toJsonTree(configuration.groups, configuration.groups.getClass());

		String jsonString = jsonElement.toString();

		return jsonString;
	}

}
