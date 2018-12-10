package de.iisys.pippa.service.volume_controller.NullController;

import java.io.IOException;
import java.util.logging.Logger;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

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
public class NullController {

	/**
	 * currently set volume, is left untouched when muted
	 */
	int currentVolume = 4;

	private Logger log = null;

	/**
	 * sets bus and connects with the sound board
	 */
	public NullController(Logger log) {

		this.log = log;

		this.setVolumeTo(this.currentVolume);

	}

	public void setVolumeTo(int volume) {

		this.currentVolume = volume;
		log.fine("Setting NULL volume to " + currentVolume);

	}

	public void increaseVolumeBy(int increase) {

		this.currentVolume = currentVolume + increase;
		log.fine("Increasing NULL volume to " + currentVolume);

	}

	public void decreaseVolumeBy(int decrease) {

		this.currentVolume = currentVolume - decrease;
		log.fine("Decreasing NULL volume to " + currentVolume);

	}

	public void mute() {

		log.fine("Muting NULL Volume");

	}

	public void unmute() {

		log.fine("Unmuting NULL Volume");

	}

	public int getVolume() {
		return this.currentVolume;
	}
}
