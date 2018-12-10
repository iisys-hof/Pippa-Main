package de.iisys.pippa.service.speech_out.TTS.Flite;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import de.iisys.pippa.core.speech_out.TTS.TTS;

public class FliteTTS implements TTS {

	private final String COMMAND = "flite";
	private final String SOUNDFILES_PATH = System.getProperty("user.home") + "/Desktop/Sound/";

	private final String VOICEFILES_PATH = System.getProperty("user.home") + "/Desktop/Voices/";
	private final String VOICE = "cmu_us_bdl.flitevox";

	public FliteTTS() {
		this.checkAndSetDirectory(SOUNDFILES_PATH);
	}

	public File convert(String text, boolean hasMarkup) {

		if (this.isWindowsSystem()) {
			return this.convertWindows(text, hasMarkup);
		}

		else if (this.isLinuxSystem()) {
			return this.convertLinux(text, hasMarkup);
		}

		else {
			// TODO could not set operating system error handling
			return null;
		}

	}

	/**
	 * Takes a String and lets Flite TTS create a Wav-File with the respective
	 * speech. The file is stored with a random name under the class-defined path.
	 * The File-object is then returned.
	 * 
	 * Uses a windows-specific exec-call to the installed Flite-engine.
	 * 
	 * @param text
	 * @param hasMarkup
	 *            is currently ignored
	 * @return
	 */
	private File convertWindows(String text, boolean hasMarkup) {

		String fileName = UUID.randomUUID().toString();

		try {

			Process fliteProcess = Runtime.getRuntime().exec("cmd /c " + COMMAND + " \"" + text + " \" -voice "
					+ this.VOICEFILES_PATH + this.VOICE + " " + SOUNDFILES_PATH + fileName + ".wav");

			fliteProcess.waitFor();

			File audioFile = null;

			audioFile = new File(SOUNDFILES_PATH + fileName + ".wav");

			return audioFile;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 
	 * Takes a String and lets Flite TTS create a Wav-File with the respective
	 * speech. The file is stored with a random name under the class-defined path.
	 * The File-object is then returned.
	 * 
	 * Uses a linux-specific exec-call to the installed Flite-engine.
	 * 
	 * @param text
	 * @param markup
	 *            is currently ignored
	 * @return
	 */
	private File convertLinux(String text, boolean hasMarkup) {

		String fileName = UUID.randomUUID().toString();

		String linuxCommand[] = { COMMAND, "-t", text, "-voice", (this.VOICEFILES_PATH + this.VOICE),
				(SOUNDFILES_PATH + fileName + ".wav") };

		try {

			// Process fliteProcess = Runtime.getRuntime().exec(COMMAND + " -t \"" + text +
			// "\" -voice "
			// + this.VOICEFILES_PATH + this.VOICE + " " + SOUNDFILES_PATH + fileName +
			// ".wav");

			Process fliteProcess = Runtime.getRuntime().exec(linuxCommand);

			fliteProcess.waitFor();

			File audioFile = null;

			audioFile = new File(SOUNDFILES_PATH + fileName + ".wav");

			return audioFile;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Checks of the environment is a windows-system.
	 * 
	 * @return
	 */
	private boolean isWindowsSystem() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.indexOf("windows") >= 0;
	}

	/**
	 * Checks of the environment is a linux-system.
	 * 
	 * @return
	 */
	private boolean isLinuxSystem() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.indexOf("linux") >= 0;
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
