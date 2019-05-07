package de.iisys.pippa.main;

import de.iisys.pippa.main.osgi_framework.PippaOSGiFramework;

public class Main {

	static PippaOSGiFramework framework = null;

	public static void main(String[] args) {

		String pathToConfig = "pippa_config.properties";
		
		if(args.length > 0) {
			if(args[0] != null & args[0] != "") {
			pathToConfig = args[0];
			}
		}
		
		framework = new PippaOSGiFramework();

		framework.start(pathToConfig);

	}

}
