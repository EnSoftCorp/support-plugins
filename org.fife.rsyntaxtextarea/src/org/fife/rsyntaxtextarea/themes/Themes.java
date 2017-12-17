package org.fife.rsyntaxtextarea.themes;

import org.fife.rsyntaxtextarea.Activator;
import org.fife.rsyntaxtextarea.log.Log;
import org.fife.ui.rsyntaxtextarea.Theme;

/**
 * Added Theme loader class
 * 
 * @author Ben Holland
 */
public class Themes {

	public static Theme DARK;
	public static Theme DEFAULT_ALT;
	public static Theme DEFAULT;
	public static Theme ECLIPSE;
	public static Theme IDEA;
	public static Theme MONOKAI;
	public static Theme VS;
	
	static {
		DARK = getTheme("dark.xml");
		DEFAULT_ALT = getTheme("default-alt.xml");
		DEFAULT = getTheme("default.xml");
		ECLIPSE = getTheme("eclipse.xml");
		IDEA = getTheme("idea.xml");
		MONOKAI = getTheme("monokai.xml");
		VS = getTheme("vs.xml");
	}

	private static Theme getTheme(String xml) {
		try {
			return Theme.load(Activator.class.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/" + xml));
		} catch (Exception e){
			Log.warning("Failed to load " + xml + " syntax highlighting theme.");
		}
		return null;
	}
	
}
