package de.iisys.pippa.support.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import de.iisys.pippa.core.logger.PippaLogger;

public class PippaLoggerImpl implements PippaLogger {

	private static PippaLoggerImpl instance;
	private Logger log = Logger.getAnonymousLogger();

	private final String LOG_DIRECTORY_PATH = System.getProperty("user.home") + "/Desktop/Log";
	private final String LOG_DIRECTORY_PATH_PATTERN = "%h/Desktop/Log/log.log";
	private final int LOG_FILE_LENGHT_BYTES = 30000000;
	private final int LOG_FILE_COUNT = 1;
	private final boolean LOG_FILE_APPEND_MODE = false;
	private final Level LOG_LEVEL = Level.ALL;
	private final Formatter LOG_FORMATTER = new SimpleFormatter();

	static synchronized PippaLoggerImpl getInstance() {
		if (PippaLoggerImpl.instance == null) {
			PippaLoggerImpl.instance = new PippaLoggerImpl();
		}
		return PippaLoggerImpl.instance;
	}

	/**
	 * Creates and supplies a central Logger that can be used within the system to
	 * write a central, shared Log-File.
	 */
	private PippaLoggerImpl() {

		this.checkAndSetDirectory(this.LOG_DIRECTORY_PATH);

		try {

			Handler handler = new FileHandler(this.LOG_DIRECTORY_PATH_PATTERN, this.LOG_FILE_LENGHT_BYTES,
					this.LOG_FILE_COUNT, this.LOG_FILE_APPEND_MODE);
			handler.setFormatter(this.LOG_FORMATTER);
			handler.setLevel(LOG_LEVEL);

			this.log.addHandler(handler);
			this.log.setLevel(LOG_LEVEL);

		} catch (SecurityException e) {
			System.err.println("SecurityException while creating Pippa-Logger, using System Standard Out for Logging");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException while creating Pippa-Logger, using System Standard Out for Logging");
			e.printStackTrace();
		}

	}

	/**
	 * Returns a (parameterized) java.util.logging.Logger which writes to a central
	 * Log.
	 */
	@Override
	public Logger getLogger() {
		return this.log;
	}

	/**
	 * Checks if the given directory exists and creates it if not.
	 * 
	 * @param directoryPath
	 */
	private void checkAndSetDirectory(String directoryPath) {

		File directory = new File(directoryPath);

		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

}
