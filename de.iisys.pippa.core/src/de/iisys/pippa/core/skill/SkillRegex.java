package de.iisys.pippa.core.skill;

import java.util.regex.Pattern;

import de.iisys.pippa.core.skill.Skill;

/**
 * Wrapping class for Java's Pattern class (which is final). Adds a reference to a Skill-object.
 * 
 * @author rpszabad
 *
 */
public class SkillRegex {

	/**
	 * the regular expression in Pattern form
	 */
	private Pattern pattern = null;
	
	/**
	 * reference to the skill this regular expression belongs to
	 */
	private Skill skillRef = null;

	/**	 * 
	 * @param skillRef skill this regular expression belongs to
	 * @param regex regular expression as a String, is compiled into a Java Pattern automatically
	 */
	public SkillRegex(Skill skillRef, String regex) {

		if (skillRef == null) {
			throw new NullPointerException("No Reference to a Skill was given when creating SkillRegex.");
		}

		if (!(skillRef instanceof Skill)) {
			throw new IllegalArgumentException("Given Reference not of Type ISkill when creating SkillRegex.");
		}

		if (regex == null || regex == "") {
			throw new IllegalArgumentException("Invalid regex in SkillRegex");
		}

		this.skillRef = skillRef;

		this.pattern = Pattern.compile(regex);
	}

	public Pattern getPattern() {
		return this.pattern;
	}

	public Skill getSkillRef() {
		return this.skillRef;
	}

}
