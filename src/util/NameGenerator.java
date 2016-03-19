package util;

import java.util.Set;
import java.util.TreeSet;

import processing.core.PApplet;

/**
 * Generates unique names, ensuring that they don't collide with each other.
 * 
 * @author James Morrow
 *
 */
public class NameGenerator {
	private final Set<String> names = new TreeSet<String>();
	private String nextName = "a";
	
	/**
	 * Constructs a NameGenerator.
	 */
	public NameGenerator() {}
	
	/**
	 * Constructs a NameGenerator that won't generate the excluded names. 
	 * @param excludedNames
	 */
	public NameGenerator(String[] excludedNames) {
		for (int i=0; i<excludedNames.length; i++) {
			addNameToExclude(excludedNames[i]);
		}
	}
	
	/**
	 * Excludes the given name from the set of generatable names.
	 * @param name The name to exclude.
	 */
	public void addNameToExclude(String name) {
		names.add(name);
	}
	
	/**
	 * Stops excluding the given name from the set of generatable names.
	 * @param name The name to stop excluding.
	 */
	public void removeNameFromExcluded(String name) {
		names.remove(name);
	}
	
	/**
	 * Returns a name.
	 * @return The name.
	 */
	public String getUniqueName() {
		while (names.contains(nextName)) {
			incrementNextName();
		}
		
		addNameToExclude(nextName);
		String uniqueName = nextName;
		incrementNextName();
		return uniqueName;
	}
	
	/**
	 * Tells whether or not this NameGenerator excludes the given name.
	 * 
	 * @param name The name to test.
	 * @return True, if the name is unique, false otherwise.
	 */
	public boolean isUnique(String name) {
		return !names.contains(name);
	}
	
	/**
	 * Returns a name that derives from the given name.
	 * @param The given name.
	 * @return The derivative name.
	 */
	public String getUniqueNameFrom(String name) {
		int i = 1;
		while (names.contains(name + i)) {
			i++;
		}
		addNameToExclude(name + i);
		return name + i;
	}
	
	/**
	 * Increments the next name.
	 */
	private void incrementNextName() {
		int i = nextName.length() - 1;
		while (i >= 0) {
			char c = nextName.charAt(i);
			if (c == 'z') {
				nextName = replaceCharAt(nextName, i, 'a');
			}
			else {
				nextName = replaceCharAt(nextName, i, (char)(c+1));
				return;
			}
			i--;
		}
		
		nextName = 'a' + nextName;
	}
	
	/**
	 * Returns a new String that is the given String with the character at i replaced with c.
	 * @param s The given String.
	 * @param i The index.
	 * @param c The character.
	 * @return The new String.
	 */
	private static String replaceCharAt(String s, int i, char c) {
		if (i == s.length() - 1) {
			return s.substring(0, i) + c;
		}
		else {
			return s.substring(0, i) + c + s.substring(i+1, s.length());
		}
	}
}