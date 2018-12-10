package de.iisys.pippa.system.executor;

import de.iisys.pippa.core.skill_executable.ASkillExecutable;
import de.iisys.pippa.core.speech_out.SpeechOutListener;

public class TestExecutable extends ASkillExecutable implements SpeechOutListener {

	public TestExecutable(String executableId) {
		super(executableId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doRun() {

		System.out.println(this.getExecutableId() + " enter doRun");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(this.getExecutableId() + " leave doRun");
	}

	@Override
	public void speechOutStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void speechOutPaused() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void speechOutResumed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void speechOutStopped() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void speechOutFinished() {
		// TODO Auto-generated method stub
		
	}

}
