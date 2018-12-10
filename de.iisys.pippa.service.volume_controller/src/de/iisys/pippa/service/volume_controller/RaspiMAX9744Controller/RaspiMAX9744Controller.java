package de.iisys.pippa.service.volume_controller.RaspiMAX9744Controller;

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
public class RaspiMAX9744Controller {

	/**
	 * I2C address which is set inside MAX9744 board
	 */
	private final int MAX9744_ADDRESS = 0x4B;

	/**
	 * I2C bus number the MAX9744 is connected to in the Raspberry GPIO bus
	 */
	private final int BUS_NUMBER = 1;

	/**
	 * Volume values the system can set
	 */
	public static final int MIN_VOLUME = 0;
	public static final int MAX_VOLUME = 10;

	/**
	 * Volume values the sound board does understand are 0 - 63, but the relation
	 * between numbers and actual volume seems not to be exactly linear in lower
	 * regions. Therefore the minimum is lifted a little.
	 */
	private static final int MIN_MAPPED_VOLUME = 25;
	private static final int MAX_MAPPED_VOLUME = 63;

	/**
	 * currently set volume, is left untouched when muted
	 */
	int currentVolume = 4;

	private I2CBus i2c = null;
	private I2CDevice device = null;

	private Logger log = null;
	
	/**
	 * sets bus and connects with the sound board
	 */
	public RaspiMAX9744Controller(Logger log) {

		this.log = log;
		
		try {
			this.setBus(BUS_NUMBER);
		} catch (UnsupportedBusNumberException e1) {
			this.log.severe("Usupported Bus Number when setting MAX9744 Bus");
		} catch (IOException e1) {
			this.log.severe("IOException when settinng MAX9744 Bus");
		}

		try {
			this.setDevice();
		} catch (IOException e) {
			this.log.severe("IOExecption when setting Max9744 Device");
		}

		this.setVolumeTo(this.currentVolume);

	}

	public void setVolumeTo(int volume) {

		this.currentVolume = this.checkMinMax(volume);
		
		log.fine("Setting volume to " + this.currentVolume);
		
		try {
			device.write(this.mapVolume(this.currentVolume));
		} catch (IOException e) {
			this.log.severe("IOException writing volume to the Max9744 device.");
		}
	}

	public void increaseVolumeBy(int increase) {

		this.currentVolume = this.checkMinMax(this.currentVolume + increase);

		log.fine("Increasing volume to " + this.currentVolume);
		
		try {
			device.write(this.mapVolume(currentVolume));
		} catch (IOException e) {
			this.log.severe("IOException writing volume to the Max9744 device.");
		}
	}

	public void decreaseVolumeBy(int decrease) {

		this.currentVolume = this.checkMinMax(this.currentVolume - decrease);

		log.fine("Decreasing volume to " + this.currentVolume);
		
		try {
			device.write(this.mapVolume(this.currentVolume));
		} catch (IOException e) {
			this.log.severe("IOException writing volume to the Max9744 device.");
		}
	}

	public void mute() {
		log.fine("Muting Volume");
		try {
			device.write((byte) 0);
		} catch (IOException e) {
			this.log.severe("IOException writing volume to the Max9744 device.");
		}

	}

	public void unmute() {
		log.fine("Unmuting Volume");
		try {
			device.write(this.mapVolume(this.currentVolume));
		} catch (IOException e) {
			this.log.severe("IOException writing volume to the Max9744 device.");
		}
	}

	/**
	 * checks if the given volume is within the controllers min and max accepted
	 * volume values
	 * 
	 * @param volume
	 * @return corrected volume
	 */
	private int checkMinMax(int volume) {
		if (volume < RaspiMAX9744Controller.MIN_VOLUME) {
			volume = RaspiMAX9744Controller.MIN_VOLUME;
		}

		if (volume > RaspiMAX9744Controller.MAX_VOLUME) {
			volume = RaspiMAX9744Controller.MAX_VOLUME;
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

		byte mappedVolume = (byte) ((((double) volume - RaspiMAX9744Controller.MIN_VOLUME)
				/ (RaspiMAX9744Controller.MAX_VOLUME - RaspiMAX9744Controller.MIN_VOLUME))
				* (RaspiMAX9744Controller.MAX_MAPPED_VOLUME - RaspiMAX9744Controller.MIN_MAPPED_VOLUME)
				+ RaspiMAX9744Controller.MIN_MAPPED_VOLUME);

		return mappedVolume;
	}

	private void setBus(int bus) throws UnsupportedBusNumberException, IOException {
		this.i2c = I2CFactory.getInstance(bus);
	}

	private void setDevice() throws IOException {
		this.device = this.i2c.getDevice(MAX9744_ADDRESS);
	}

	public int getVolume() {
		return this.currentVolume;
	}
}
