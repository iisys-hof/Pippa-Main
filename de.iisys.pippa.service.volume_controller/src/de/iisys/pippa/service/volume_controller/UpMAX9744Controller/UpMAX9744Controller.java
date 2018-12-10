package de.iisys.pippa.service.volume_controller.UpMAX9744Controller;

import java.io.IOException;
import java.util.logging.Logger;

import mraa.I2c;
import mraa.Result;

/**
 * Interfaces with an attached Adafruit MAX9774 sound board via I2C and controls
 * it's volume according to given values.
 * 
 * Given values are mapped between MIN_ and MAX_VOLUME (system input) and MIN_
 * and MAX_MAPPED_VOLUME (hardware input). Too high or low values are caught in
 * between min and max automatically.
 * 
 * @author rszabad
 * 
 */
public class UpMAX9744Controller {

	/**
	 * I2C address which is set inside MAX9744 board
	 */
	private final short MAX9744_ADDRESS = 0x4B;

	/**
	 * I2C bus number the MAX9744 is connected to in the Raspberry GPIO bus
	 */
	private final int BUS_NUMBER = 0;

	/**
	 * Volume values the system can set
	 */
	public static final int MIN_VOLUME = 0;
	public static final int MAX_VOLUME = 10;

	/**
	 * Volume values the sound board does understand are 0 - 63, but the
	 * relation between numbers and actual volume seems not to be exactly linear
	 * in lower regions. Therefore the minimum is lifted a little.
	 */
	private static final int MIN_MAPPED_VOLUME = 25;
	private static final int MAX_MAPPED_VOLUME = 63;

	/**
	 * currently set volume, is left untouched when muted
	 */
	int currentVolume = 4;

	private I2c i2c = null;

	private Logger log = null;

	static {
		try {
			System.loadLibrary("mraajava");
		} catch (UnsatisfiedLinkError e) {
			System.err
					.println("Native code library failed to load. See the chapter on Dynamic Linking Problems in the SWIG Java documentation for help.\n"
							+ e);
			System.exit(1);
		}
	}

	/**
	 * sets bus and connects with the sound board
	 */
	public UpMAX9744Controller(Logger log) {

		this.log = log;

		this.i2c = new I2c(BUS_NUMBER);
		Result r = this.i2c.address(MAX9744_ADDRESS);
		
		this.setVolumeTo(this.currentVolume);

	}

	public void setVolumeTo(int volume) {

		this.currentVolume = this.checkMinMax(volume);

		log.fine("Setting volume to " + this.currentVolume);

		this.i2c.writeByte(this.mapVolume(this.currentVolume));

	}

	public void increaseVolumeBy(int increase) {

		this.currentVolume = this.checkMinMax(this.currentVolume + increase);

		log.fine("Increasing volume to " + this.currentVolume);

		this.i2c.writeByte(this.mapVolume(this.currentVolume));
	}

	public void decreaseVolumeBy(int decrease) {

		this.currentVolume = this.checkMinMax(this.currentVolume - decrease);

		log.fine("Decreasing volume to " + this.currentVolume);

		this.i2c.writeByte(this.mapVolume(this.currentVolume));
	}

	public void mute() {
		log.fine("Muting Volume");

		this.i2c.writeByte((byte) 0);

	}

	public void unmute() {
		log.fine("Unmuting Volume");
		
		this.i2c.writeByte(this.mapVolume(this.currentVolume));
	}

	/**
	 * checks if the given volume is within the controllers min and max accepted
	 * volume values
	 * 
	 * @param volume
	 * @return corrected volume
	 */
	private int checkMinMax(int volume) {
		if (volume < UpMAX9744Controller.MIN_VOLUME) {
			volume = UpMAX9744Controller.MIN_VOLUME;
		}

		if (volume > UpMAX9744Controller.MAX_VOLUME) {
			volume = UpMAX9744Controller.MAX_VOLUME;
		}
		return volume;
	}

	/**
	 * maps the given volume from it's current range onto the sound boards range
	 * 
	 * @param volume
	 * @return mapped volume
	 */
	private byte mapVolume(int volume) {

		if (volume == 0) {
			return 0;
		}

		byte mappedVolume = (byte) ((((double) volume - UpMAX9744Controller.MIN_VOLUME) / (UpMAX9744Controller.MAX_VOLUME - UpMAX9744Controller.MIN_VOLUME))
				* (UpMAX9744Controller.MAX_MAPPED_VOLUME - UpMAX9744Controller.MIN_MAPPED_VOLUME) + UpMAX9744Controller.MIN_MAPPED_VOLUME);

		return mappedVolume;
	}

	public int getVolume() {
		return this.currentVolume;
	}
}
