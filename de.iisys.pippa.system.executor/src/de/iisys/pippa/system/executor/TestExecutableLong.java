package de.iisys.pippa.system.executor;

import de.iisys.pippa.core.skill_executable.ASkillExecutable;
import de.iisys.pippa.core.speech_out.SpeechOutListener;

public class TestExecutableLong extends ASkillExecutable implements SpeechOutListener {

	public TestExecutableLong(String executableId) {
		super(executableId);
		// TODO Auto-generated constructor stub
	}

	
	public void doRun() {

		System.out.println(this.getExecutableId() + " enter doRun");

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(this.getExecutableId() + " leave doRun");
	}

	
	public void speechOutStarted() {
		// TODO Auto-generated method stub
		
	}

	
	public void speechOutPaused() {
		// TODO Auto-generated method stub
		
	}

	
	public void speechOutResumed() {
		// TODO Auto-generated method stub
		
	}

	
	public void speechOutStopped() {
		// TODO Auto-generated method stub
		
	}

	
	public void speechOutFinished() {
		// TODO Auto-generated method stub
		
	}

}
