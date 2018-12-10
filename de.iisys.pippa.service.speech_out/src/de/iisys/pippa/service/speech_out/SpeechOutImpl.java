package de.iisys.pippa.service.speech_out;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.iisys.pippa.core.logger.PippaLogger;
import de.iisys.pippa.core.pippa_service.PippaService;
import de.iisys.pippa.core.speech_out.SpeechOut;
import de.iisys.pippa.core.speech_out.SpeechOutListener;
import de.iisys.pippa.core.speech_out.SpeechOutSystem;
import de.iisys.pippa.core.speech_out.TTS.TTS;
import de.iisys.pippa.service.speech_out.TTS.Flite.FliteTTS;

public class SpeechOutImpl implements Runnable, LineListener, SpeechOut, SpeechOutSystem, PippaService {

	boolean isClosed = false;

	private static SpeechOutImpl instance;

	public static synchronized SpeechOutImpl getInstance() {
		if (SpeechOutImpl.instance == null) {
			SpeechOutImpl.instance = new SpeechOutImpl();
		}
		return SpeechOutImpl.instance;
	}

	public static synchronized SpeechOutImpl getInstance(Logger log) {
		if (SpeechOutImpl.instance == null) {
			SpeechOutImpl.instance = new SpeechOutImpl();
			SpeechOutImpl.instance.log = log;
		}
		return SpeechOutImpl.instance;
	}

	TTS tts = new FliteTTS();

	File shortFile = null;
	File longFile = null;

	SpeechOutListener shortExecutable = null;
	SpeechOutListener longExecutable = null;

	Clip shortClip = null;
	Clip longClip = null;

	Line shortLine = null;
	Line longLine = null;

	FloatControl shortGain = null;
	FloatControl longGain = null;

	boolean startShort = false;
	boolean stopShort = false;

	boolean startLong = false;
	boolean stopLong = false;
	boolean pauseLong = false;

	float underlayGain = (float) 0.2; // between 0 and 1
	float underlayDB = (float) (Math.log(underlayGain) / Math.log(10.0) * 20.0);
	float standardDB = (float) (Math.log(1.0) / Math.log(10.0) * 20.0);

	Logger log = null;

	public SpeechOutImpl() {
	}

	public synchronized void setOutputText(SpeechOutListener skillExecutable, String text, boolean hasMarkup) {

		if (skillExecutable == this.shortExecutable) {

			this.log.fine(skillExecutable.getClass().getSimpleName() + " - " + text + " - hasMarkup: " + hasMarkup
					+ " - is short executable");

			this.shortFile = null;
			this.shortFile = this.tts.convert(text, hasMarkup);
			this.setOutputFile(skillExecutable, this.shortFile);
		}

		else if (skillExecutable == this.longExecutable) {

			this.log.fine(skillExecutable.getClass().getSimpleName() + " - " + text + " - hasMarkup: " + hasMarkup
					+ " - is long executable");

			this.longFile = null;
			this.longFile = this.tts.convert(text, hasMarkup);
			this.setOutputFile(skillExecutable, this.longFile);
		}

	}

	private synchronized void setOutputFile(SpeechOutListener skillExecutable, File audioFile) {

		try {
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

			AudioFormat format = audioStream.getFormat();

			DataLine.Info info = new DataLine.Info(Clip.class, format);

			if (skillExecutable == this.shortExecutable) {

				if (this.shortClip != null) {
					this.shortClip.stop();
					this.shortClip.close();
				}

				this.shortLine = AudioSystem.getLine(info);

				this.shortClip = (Clip) this.shortLine;
				this.shortClip.addLineListener(this);
				this.shortClip.open(audioStream);
				this.shortGain = (FloatControl) this.shortClip.getControl(FloatControl.Type.MASTER_GAIN);

			}

			else if (skillExecutable == this.longExecutable) {

				if (this.longClip != null) {
					this.longClip.stop();
					this.longClip.close();
				}

				this.longLine = AudioSystem.getLine(info);

				this.longClip = (Clip) this.longLine;
				this.longClip.addLineListener(this);
				this.longClip.open(audioStream);
				this.longGain = (FloatControl) this.longClip.getControl(FloatControl.Type.MASTER_GAIN);
			}

			audioStream.close();

		} catch (UnsupportedAudioFileException e) {
			this.log.severe("The specified audio file is not supported.");
		} catch (LineUnavailableException e) {
			this.log.severe("Audio line for playing back is unavailable.");
		} catch (IOException e) {
			this.log.severe("Error playing the audio file.");
		}

	}

	@Override
	public synchronized void play(SpeechOutListener skillExecutable) {

		if (this.shortExecutable == skillExecutable) {

			this.log.fine(skillExecutable.getClass().getSimpleName() + " - play short");

			this.startShort = true;
			notify();
		}

		else if (this.longExecutable == skillExecutable) {

			this.log.fine(skillExecutable.getClass().getSimpleName() + " - play long");

			this.startLong = true;
			notify();
		}

	}

	@Override
	public synchronized void pause(SpeechOutListener skillExecutable) {

		// not supported by pippa
		/*
		 * if(this.shortExecutable == skillExecutable) { this.pauseShort = true;
		 * notify(); }
		 */

		if (this.longExecutable == skillExecutable) {

			this.log.fine(skillExecutable.getClass().getSimpleName() + " - pause long");

			this.pauseLong = true;
			notify();
		}

	}

	@Override
	public synchronized void stop(SpeechOutListener skillExecutable) {

		if (this.shortExecutable == skillExecutable) {

			this.log.fine(skillExecutable.getClass().getSimpleName() + " - stop short");

			this.stopShort = true;
			notify();
		}

		else if (this.longExecutable == skillExecutable) {

			this.log.fine(skillExecutable.getClass().getSimpleName() + " - stop long");

			this.stopLong = true;
			notify();
		}

	}

	@Override
	public synchronized void run() {

		this.getLogger();

		while (!isClosed) {

			try {

				wait();

				if (this.startShort) {

					this.startShort = false;

					if (this.longGain != null) {
						this.longGain.setValue(underlayDB);
						// TODO setValue is rather unresponsive...
						Thread.sleep(1000);
					}

					this.shortClip.start();

				}

				else if (this.stopShort) {

					this.stopShort = false;
					this.shortClip.stop();
				}

				else if (this.startLong) {

					this.startLong = false;

					this.longClip.start();
				}

				else if (this.pauseLong) {

					this.pauseLong = false;

					this.longClip.stop();
				}

				else if (this.stopLong) {

					this.stopLong = false;
					this.longClip.stop();
				}

			} catch (InterruptedException e) {
				this.log.warning("interrupted");
			}

		}
	}

	private void getLogger() {
		if (this.log == null) {

			Collection<ServiceReference<PippaLogger>> serviceReferences = null;

			try {

				BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

				serviceReferences = context.getServiceReferences(PippaLogger.class, "(name=PippaLogger)");

				PippaLogger service = context
						.getService(((List<ServiceReference<PippaLogger>>) serviceReferences).get(0));

				this.log = service.getLogger();

			} catch (Exception e) {
				System.err.println("Could not load PippaLogger, using Standard Logger");
				e.printStackTrace();
				this.log = Logger.getLogger(this.getClass().getSimpleName());
			}

		}

	}

	@Override
	public void update(LineEvent event) {

		this.log.fine("Line Event received - " + event.getType().toString());

		// get event Type
		LineEvent.Type type = event.getType();

		// Start / Resume playing of a clip
		if (type == LineEvent.Type.START) {

			// tell provider of short output about the start
			if (event.getLine() == this.shortLine) {

				this.log.fine("short started");

				this.shortExecutable.speechOutStarted();
			}
			// tell provider of long output about the start/resume
			else if (event.getLine() == this.longLine) {

				if ((this.longClip.getMicrosecondPosition() > 0)) {

					this.log.fine("long resumed");

					this.longExecutable.speechOutResumed();
				}

				else {

					this.log.fine("long started");

					this.longExecutable.speechOutStarted();
				}

			}

		}

		// Finish / Pause of a clip
		else if (type == LineEvent.Type.STOP) {

			// if the short output stopped it finished definitely, since it cannot be paused
			// by design
			if (event.getLine() == this.shortLine) {

				// reset the long-clip volume back to standard
				if (this.longGain != null) {
					this.longGain.setValue(standardDB);
				}

				if (this.shortClip.getMicrosecondPosition() < this.shortClip.getMicrosecondLength()) {
					// tell provider of short output that clip was stopped

					this.shortLine.close();
					this.shortClip.close();
					System.gc();
					//this.shortFile.delete();
					this.shortFile = null;

					this.log.fine("short stopped");

					this.shortExecutable.speechOutStopped();
				}

				else {
					// tell provider of short output about the finished clip

					this.shortLine.close();
					this.shortClip.close();
					System.gc();
					this.shortFile.delete();
					this.shortFile = null;

					this.log.fine("short finished");

					this.shortExecutable.speechOutFinished();
				}

			}

			// if the long output stopped it might be paused or have finished playing
			else if (event.getLine() == this.longLine) {

				if (this.longClip.getMicrosecondPosition() < this.longClip.getMicrosecondLength() && this.stopLong) {
					this.stopLong = false;

					this.longLine.close();
					this.longClip.close();
					System.gc();
					this.longFile.delete();
					this.longFile = null;

					this.log.fine("long stopped");

					this.longExecutable.speechOutStopped();
				}

				else if (this.longClip.getMicrosecondPosition() >= this.longClip.getMicrosecondLength()) {
					// tell provider of long output about the finished clip

					this.longLine.close();
					this.longClip.close();
					System.gc();
					this.longFile.delete();
					this.longFile = null;

					this.log.fine("long finished");

					this.longExecutable.speechOutFinished();
				}

				else {

					// tell provider of long output about the paused clip

					this.log.fine("long paused");

					this.longExecutable.speechOutPaused();
				}

			}

		}

		else if (type == LineEvent.Type.CLOSE) {
			this.log.fine("line closed");
		}

		else if (type == LineEvent.Type.OPEN) {
			this.log.fine("line opened");
		}

	}

	@Override
	public void registerShort(SpeechOutListener executable) {

		if (this.shortExecutable != executable) {
			if (this.shortClip != null) {
				shortClip.stop();
				shortClip.close();

				System.gc();
				if (this.shortFile != null) {
					this.shortFile.delete();
					this.shortFile = null;
				}
			}
		}

		this.shortExecutable = executable;

		if (executable != null) {
			this.log.fine(executable.getClass().getSimpleName() + " - registered as short executable");
		} else {
			this.log.fine("short executable set to null");
		}
	}

	@Override
	public void registerLong(SpeechOutListener executable) {

		if (this.longExecutable != executable) {
			if (this.longClip != null) {
				this.longClip.stop();
				this.longClip.close();

			}

			System.gc();
			if (this.longFile != null) {
				this.longFile.delete();
				this.longFile = null;
			}
		}

		this.longExecutable = executable;

		if (executable != null) {
			this.log.fine(executable.getClass().getSimpleName() + " - registered as long executable");
		} else {
			this.log.fine("long executable set to null");
		}
	}

}
