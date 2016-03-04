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
	private int nextId = 0;
	
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
			names.add(excludedNames[i]);
		}
	}
	
	/**
	 * Excludes the given name from the set of generatable names.
	 * @param name
	 */
	public void addNameToExclude(String name) {
		names.add(name);
	}
	
	/**
	 * Stops excluding the given name from the set of generatable names.
	 * @param name
	 */
	public void removeNameFromExcluded(String name) {
		names.remove(name);
	}
	
	/**
	 * Returns a name.
	 * @return
	 */
	public String getUniqueName() {
		while (names.contains(idToString(nextId))) {
			incrementNextId();
		}
		
		String name = idToString(nextId);
		addNameToExclude(name);
		incrementNextId();
		return name;
	}
	
	/**
	 * Returns a name that derives from the given name.
	 * @param name
	 * @return
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
	 * Increments the integer id, which cooresponds to the next name the NameGenerator will give.
	 */
	private void incrementNextId() {
		nextId++;
	}
	
	/**
	 * Converts an id (a positive integer or 0) to a string, by replacing each decimal digit in the integer with a character.
	 * 0 => 'a'
	 * 1 => 'b'
	 * 2 => 'c'
	 * ...
	 * 
	 * @return
	 */
	private String idToString(int id) {
		Integer integer = id;
		String s = integer.toString();
		String t = "";
		for (int i=0; i<s.length(); i++) {
			int digit = s.charAt(i) - '0';
			t += (char)('a' + digit);
		}
		return t;
	}
}