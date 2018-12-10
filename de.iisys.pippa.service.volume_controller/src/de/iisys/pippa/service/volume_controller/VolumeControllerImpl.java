package de.iisys.pippa.service.volume_controller;

import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import de.iisys.pippa.core.pippa_service.PippaService;
import de.iisys.pippa.core.service_loader.PippaServiceLoader;
import de.iisys.pippa.core.status.StatusAccess;
import de.iisys.pippa.core.volume_controller.VolumeController;
import de.iisys.pippa.service.volume_controller.NullController.NullController;

public class VolumeControllerImpl implements VolumeController, PippaService {

	private Logger log = null;

	/**
	 * reference to the system's status object
	 */
	protected StatusAccess status = null;

	/**
	 * reference to the bundle's context
	 */
	protected BundleContext context = null;
	
	private NullController controller = null;

	public VolumeControllerImpl() {

		this.context = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();

		if (this.log == null) {
			this.log = PippaServiceLoader.getLogger(this.context);
		}
		if (this.status == null) {
			this.status = PippaServiceLoader.getStatus(this.context);
			if (this.status == null) {
				this.log.severe("could not retrieve the needed Status-Object");
			}
		}
		this.controller = new NullController(this.log);
	}

	protected VolumeControllerImpl(StatusAccess status, Logger log) {
		this.status = status;
		this.log = log;
		this.controller = new NullController(this.log);
	}

	public void setVolumeTo(int volume) {
		if (this.controller != null) {
			this.controller.setVolumeTo(volume);
		}
		if (this.status != null) {
			this.status.setVolume(this.controller.getVolume());
		}
	}

	public void increaseVolumeBy(int increase) {
		if (this.controller != null) {
			this.controller.increaseVolumeBy(increase);
		}
		if (this.status != null) {
			this.status.setVolume(this.controller.getVolume());
		}
	}

	public void decreaseVolumeBy(int decrease) {
		if (this.controller != null) {
			this.controller.decreaseVolumeBy(decrease);
		}
		if (this.status != null) {
			this.status.setVolume(this.controller.getVolume());
		}
	}

	public void mute() {
		if (this.controller != null) {
			this.controller.mute();
		}
		if (this.status != null) {
			this.status.setMuted(true);
		}
	}

	public void unmute() {
		if (this.controller != null) {
			this.controller.unmute();
		}
		if (this.status != null) {
			this.status.setMuted(false);
		}
	}

}
