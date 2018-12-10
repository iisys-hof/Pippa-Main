package de.iisys.pippa.core.volume_controller;

public interface VolumeController {
	
	public void setVolumeTo(int volume);
	
	public void increaseVolumeBy(int increase);

	public void decreaseVolumeBy(int decrease);
	
	public void mute();
	
	public void unmute();
}
