package de.iisys.pippa.main;

import de.iisys.pippa.main.osgi_framework.PippaOSGiFramework;

public class Main {

	static PippaOSGiFramework framework = null;

	public static void main(String[] args) {

		framework = new PippaOSGiFramework();

		framework.start();

	}

}
