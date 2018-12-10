package de.iisys.pippa.skill.system_access;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import de.iisys.pippa.core.config_manager.Config;
import de.iisys.pippa.core.config_manager.ConfigManager;
import de.iisys.pippa.core.database_manager.DatabaseManager;
import de.iisys.pippa.core.message.AMessage;
import de.iisys.pippa.core.message.speech_message.SkillSpeechMessage;
import de.iisys.pippa.core.message.stop_message.StopMessage;
import de.iisys.pippa.core.message_processor.AMessageProcessor;
import de.iisys.pippa.core.numeral_converter.NumeralConverter;
import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill.SkillRegex;
import de.iisys.pippa.core.skill_executable.ASkillExecutable;
import de.iisys.pippa.core.status.StatusAccess;
import de.iisys.pippa.core.status.StatusReader;
import de.iisys.pippa.core.volume_controller.VolumeController;

public class SystemAccessSkillImpl extends AMessageProcessor implements Skill {

	private boolean isClosed = false;

	/**
	 * reference to the system's status object
	 */
	protected StatusReader status = null;

	/**
	 * the users main config which can be loaded through the config manager
	 */
	protected Config config = null;

	/**
	 * the users database connection which can be loaded through the database
	 * manager
	 */
	protected Connection databaseConnection = null;

	/**
	 * reference to the system's numeral converter object
	 */
	protected NumeralConverter numeralConverter = null;

	/**
	 * reference to the system's volume control object
	 */
	protected VolumeController volumeController = null;

	/**
	 * reference to the bundle's context
	 */
	protected BundleContext context = null;

	SkillRegex muteRegex = new SkillRegex(this, "(mute)");
	SkillRegex unmuteRegex = new SkillRegex(this, "(unmute)");
	SkillRegex volumeRegex = new SkillRegex(this, "(volume)");
	SkillRegex louderRegex = new SkillRegex(this, "(louder)|(up)");
	SkillRegex softerRegex = new SkillRegex(this, "(softer)|(down)");

	SkillRegex[] skillRegexes = new SkillRegex[] { muteRegex, unmuteRegex, volumeRegex, louderRegex, softerRegex };

	ASkillExecutable sorrySkillExecutable = null;

	@Override
	public SkillRegex[] getRegexes() {
		return this.skillRegexes;
	}

	public SystemAccessSkillImpl() {
		try {
			this.context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		} catch (Exception e) {
			this.context = null;
		}
	}

	@Override
	public void run() {

		this.getStatus();
		this.getNumeralConverter();
		this.getConfig();
		this.getDatabase();
		this.getVolumeController();

		while (!this.isClosed) {

			AMessage nextMessage = null;

			try {

				nextMessage = this.getIncomingQueue().take();

				if (nextMessage != null) {

					if (nextMessage instanceof StopMessage) {
						StopMessage stopMessage = (StopMessage) nextMessage;
						if (stopMessage.isStopAndClose()) {
							this.isClosed = true;
						}
						this.getOutgoingQueue().put(nextMessage);
					}

					else if (nextMessage instanceof SkillSpeechMessage) {

						System.out.println("SystemAccessSkill Received SkillSpeechMessage");

						SkillSpeechMessage speechMessage = (SkillSpeechMessage) nextMessage;

						String convertedMessage = this.numeralConverter.numeralsToNumbers(speechMessage.getAudioText());

						if (speechMessage.getMatchedRegexes(this) != null
								&& speechMessage.getMatchedRegexes(this).length > 0) {

							List<SkillRegex> matchedRegexes = new ArrayList<>(
									Arrays.asList(speechMessage.getMatchedRegexes(this)));

							if (matchedRegexes.contains(this.muteRegex)) {
								if (!this.status.isMuted()) {
									System.out.println("muting sound");
									this.volumeController.mute();
								}
							}

							else if (matchedRegexes.contains(this.unmuteRegex)) {
								if (this.status.isMuted()) {
									System.out.println("unmuting sound");
									this.volumeController.unmute();
								}
							}

							if (matchedRegexes.contains(this.louderRegex)) {
								System.out.print("louder sound");
								if (this.containsNumber(convertedMessage)) {
									int louderBy = this.extractFirstNumber(convertedMessage);
									System.out.println(" by " + louderBy);
									this.volumeController.increaseVolumeBy(louderBy);
								} else {
									System.out.println(" by one");
									this.volumeController.increaseVolumeBy(1);
								}

							}

							else if (matchedRegexes.contains(this.softerRegex)) {
								System.out.print("softer sound");
								if (this.containsNumber(convertedMessage)) {
									int softerBy = this.extractFirstNumber(convertedMessage);
									System.out.println(" by " + softerBy);
									this.volumeController.decreaseVolumeBy(softerBy);
								} else {
									System.out.println(" by one");
									this.volumeController.decreaseVolumeBy(1);
								}
							}

							else if (matchedRegexes.contains(this.volumeRegex)) {
								if (this.containsNumber(convertedMessage)) {
									int volumeTo = this.extractFirstNumber(convertedMessage);
									System.out.println("setting volume " + volumeTo);
									this.volumeController.setVolumeTo(volumeTo);
								}
							}

						}
						// there was no matched regex, do nothing then

					}

				}
			} catch (InterruptedException e) {
				// TODO
			}

		}

		// some cleaning up before returning

		return;

	}

	private boolean containsNumber(String input) {

		return input.matches(".*\\d+.*");

	}

	private void getStatus() {
		if (this.status == null) {
			Collection<ServiceReference<StatusAccess>> serviceReferencesStatus = null;

			try {
				// get all service references registered under the given name (which should be
				// 1)
				serviceReferencesStatus = context.getServiceReferences(StatusAccess.class, "(name=Status)");

				// get actual object from reference
				StatusAccess service = context
						.getService(((List<ServiceReference<StatusAccess>>) serviceReferencesStatus).get(0));

				this.status = service;

			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getNumeralConverter() {
		if (this.numeralConverter == null) {
			Collection<ServiceReference<NumeralConverter>> serviceReferencesStatus = null;

			try {
				// get all service references registered under the given name (which should be
				// 1)
				serviceReferencesStatus = context.getServiceReferences(NumeralConverter.class,
						"(name=NumeralConverter)");

				// get actual object from reference
				NumeralConverter service = context
						.getService(((List<ServiceReference<NumeralConverter>>) serviceReferencesStatus).get(0));

				this.numeralConverter = service;

			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getConfig() {
		if (this.config == null) {
			Collection<ServiceReference<ConfigManager>> serviceReferencesStatus = null;

			try {
				// get all service references registered under the given name (which should be
				// 1)
				serviceReferencesStatus = context.getServiceReferences(ConfigManager.class, "(name=ConfigManager)");

				// get actual object from reference
				ConfigManager configManager = context
						.getService(((List<ServiceReference<ConfigManager>>) serviceReferencesStatus).get(0));

				this.config = configManager.loadMainConfig();

			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getDatabase() {
		if (this.databaseConnection == null) {
			Collection<ServiceReference<DatabaseManager>> serviceReferencesStatus = null;

			try {
				// get all service references registered under the given name (which should be
				// 1)
				serviceReferencesStatus = context.getServiceReferences(DatabaseManager.class, "(name=DatabaseManager)");

				// get actual object from reference
				DatabaseManager databaseManager = context
						.getService(((List<ServiceReference<DatabaseManager>>) serviceReferencesStatus).get(0));

				try {
					this.databaseConnection = databaseManager.createConnection(this);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getVolumeController() {
		if (this.volumeController == null) {
			Collection<ServiceReference<VolumeController>> serviceReferencesStatus = null;

			try {
				// get all service references registered under the given name (which should be
				// 1)
				serviceReferencesStatus = context.getServiceReferences(VolumeController.class,
						"(name=VolumeController)");

				// get actual object from reference
				VolumeController service = context
						.getService(((List<ServiceReference<VolumeController>>) serviceReferencesStatus).get(0));

				this.volumeController = service;

			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private int extractFirstNumber(String str) {

		StringBuilder sb = new StringBuilder();
		boolean isDigit = false;

		for (char c : str.toCharArray()) {

			if (Character.isDigit(c)) {
				sb.append(c);
				isDigit = true;
			}

			else if (isDigit) {
				break;
			}

		}

		if (sb.length() > 0) {
			return Integer.parseInt(sb.toString());
		} else {
			return -1;
		}

	}

}
