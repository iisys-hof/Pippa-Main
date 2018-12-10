package de.iisys.pippa.skill.clock;

import java.time.LocalDateTime;

import de.iisys.pippa.core.skill_executable.ASkillExecutable;
import de.iisys.pippa.core.speech_out.SpeechOut;
import de.iisys.pippa.core.speech_out.SpeechOutListener;

public class ClockSkillExecutableImpl extends ASkillExecutable implements SpeechOutListener {

	static String skillId = "ClockSkillExecutable_0.1";

	protected SpeechOut speechOut = null;

	private final Object lockObject = new Object();

	public ClockSkillExecutableImpl(SpeechOut speechOut) {
		super(skillId);
		this.speechOut = speechOut;
	}

	final String[] nums = { "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
			"eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen",
			"twenty", "twenty one", "twenty two", "twenty three", "twenty four", "twenty five", "twenty six",
			"twenty seven", "twenty eight", "twenty nine", };

	LocalDateTime ldt = null;
	String timeInWords = "";

	@Override
	public void doRun() {

		this.ldt = LocalDateTime.now();

		this.setTimeString(ldt.getHour(), ldt.getMinute());

		this.speechOut.setOutputText(this, this.timeInWords, false);

		this.speechOut.play(this);

		System.out.println("before wait");

		synchronized (lockObject) {
			try {
				lockObject.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("after wait");
	}

	private void setTimeString(int h, int m) {

		if (m == 0)
			this.timeInWords = (nums[h] + " o' clock");

		else if (m == 1)
			this.timeInWords = ("one minute past " + nums[h]);

		else if (m == 59)
			this.timeInWords = ("one minute to " + nums[(h % 12) + 1]);

		else if (m == 15)
			this.timeInWords = ("quarter past " + nums[h]);

		else if (m == 30)
			this.timeInWords = ("half past " + nums[h]);

		else if (m == 45)
			this.timeInWords = ("quarter to " + nums[(h % 12) + 1]);

		else if (m <= 30)
			this.timeInWords = (nums[m] + " minutes past " + nums[h]);

		else if (m > 30)
			this.timeInWords = (nums[60 - m] + " minutes to " + nums[(h % 12) + 1]);

		this.timeInWords = "It is " + this.timeInWords + ".";
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
