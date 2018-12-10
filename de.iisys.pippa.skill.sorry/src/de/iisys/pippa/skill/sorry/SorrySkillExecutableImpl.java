package de.iisys.pippa.skill.sorry;

import de.iisys.pippa.core.skill_executable.ASkillExecutable;
import de.iisys.pippa.core.speech_out.SpeechOut;
import de.iisys.pippa.core.speech_out.SpeechOutListener;

public class SorrySkillExecutableImpl extends ASkillExecutable implements SpeechOutListener {

	static String skillId = "SorrySkillExecutable_0.1";

	protected SpeechOut speechOut = null;

	private final Object lockObject = new Object();
	
	public SorrySkillExecutableImpl(SpeechOut speechOut) {
		super(skillId);

		this.speechOut = speechOut;
		
	}

	String sorry = "Sorry, I can not help you.";

	@Override
	public synchronized void doRun() {

		this.speechOut.setOutputText(this, this.sorry, false);

		this.speechOut.play(this);
		
		synchronized (lockObject) {
			try {
				lockObject.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void speechOutStarted() {
		System.out.println("sorry speechOutStarted");

	}

	@Override
	public void speechOutPaused() {
		System.out.println("sorry speechOutPaused");
	}

	@Override
	public void speechOutResumed() {
		System.out.println("sorry speechOutResumed");
	}

	@Override
	public void speechOutStopped() {
		System.out.println("sorry speechOutStopped");
		synchronized (lockObject) {
			lockObject.notify();
		}
	}

	@Override
	public void speechOutFinished() {
		System.out.println("sorry speechOutFinished");
		synchronized (lockObject) {
			lockObject.notify();
		}
	}

}
