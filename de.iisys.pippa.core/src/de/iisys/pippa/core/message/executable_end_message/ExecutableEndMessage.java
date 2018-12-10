package de.iisys.pippa.core.message.executable_end_message;

import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.skill_executable.ASkillExecutable;

/**
 * Message that is sent from an ASkillExecutable to the Executor to signal the
 * end of it's execution function.
 * 
 * @author rpszabad
 *
 */
public class ExecutableEndMessage extends AMessage {

	/**
	 * sender executable
	 */
	private ASkillExecutable executableRef = null;

	public ExecutableEndMessage(ASkillExecutable executableRef) {
		super();
		this.executableRef = executableRef;
	}

	public ASkillExecutable getExecutableRef() {
		return executableRef;
	}

}
