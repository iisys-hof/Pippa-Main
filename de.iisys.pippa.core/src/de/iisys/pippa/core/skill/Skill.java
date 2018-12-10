package de.iisys.pippa.core.skill;

/**
 * Interface utilized by skills.
 * 
 * @author rpszabad
 *
 */
public interface Skill {

	/**
	 * 
	 * @return an array of the skills regular expressions that it want's to be
	 *         associated with
	 */
	public SkillRegex[] getRegexes();

}
