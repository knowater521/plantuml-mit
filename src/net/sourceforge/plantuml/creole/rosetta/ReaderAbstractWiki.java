/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2020, Arnaud Roques
 *
 * Project Info:  https://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * https://plantuml.com/patreon (only 1$ per month!)
 * https://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * Licensed under The MIT License (Massachusetts Institute of Technology License)
 * 
 * See http://opensource.org/licenses/MIT
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 *
 * Original Author:  Arnaud Roques
 */
package net.sourceforge.plantuml.creole.rosetta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ReaderAbstractWiki implements ReaderWiki {

	static protected final String PNG_OR_GIF = "([-_\\w]+\\.(?:png|gif))";

	static protected final String BRACKET1 = "\\[([^\\[\\]]+?)\\]";
	static protected final String BRACKET0 = "\\[([^\\[\\]]*?)\\]";

	final public List<String> transform(String... data) {
		return transform(Arrays.asList(data));
	}

	// final protected String protectURL(String s) {
	// return WikiLanguage.protectURL(s);
	// }

	final protected String rawCode(String s, String start, String end) {
		final StringBuilder sb = new StringBuilder(s.length());
		boolean rawMode = false;
		boolean rawInRaw = false;
		for (int i = 0; i < s.length(); i++) {
			if (rawMode == false && s.substring(i).startsWith(start)) {
				rawMode = true;
				rawInRaw = false;
				i += start.length() - 1;
				sb.append(WikiLanguage.UNICODE.tag("code"));
				if (s.substring(i + 1).startsWith(start)) {
					i += start.length();
					sb.append(WikiLanguage.hideCharsF7(start));
					rawInRaw = true;
				}
				continue;
			}
			if (rawMode == true && s.substring(i).startsWith(end) && s.substring(i + 1).startsWith(end) == false) {
				rawMode = false;
				i += end.length() - 1;
				if (rawInRaw && s.substring(i + 1).startsWith(end)) {
					i += end.length();
					sb.append(WikiLanguage.hideCharsF7(end));
				}
				sb.append(WikiLanguage.UNICODE.slashTag("code"));
				rawInRaw = false;
				continue;
			}
			char ch = s.charAt(i);
			if (rawMode) {
				ch = WikiLanguage.toF7(ch);
			}
			sb.append(ch);
		}
		return sb.toString();
	}

	protected final String cleanAndHideBackslashSeparator(String s, String sep, String unicodeSep) {
		s = s.trim();
		if (s.startsWith(sep)) {
			s = s.substring(1);
		}
		if (s.endsWith(sep)) {
			s = s.substring(0, s.length() - 1);
		}
		s = s.trim();
		final String regex;
		if (sep.equals("^") || sep.equals("|")) {
			regex = "\\\\\\" + sep;
		} else {
			throw new IllegalArgumentException();
		}
		s = s.replaceAll(regex, unicodeSep);
		s = singleLineFormat(s.trim());
		s = s.replace(sep, " " + sep + " ");
		return s;
	}

	protected abstract String singleLineFormat(String wiki);

	final protected void exportCodeInternal(List<String> uhtml, String tag, List<String> lines) {
		uhtml.add(WikiLanguage.UNICODE.tag(tag));
		for (String s : lines) {
			uhtml.add(s);
		}
		uhtml.add(WikiLanguage.UNICODE.slashTag(tag));
	}

	final protected void exportCodeInternalEnsureStartuml(List<String> uhtml, String tag, List<String> lines) {
		exportCodeInternal(uhtml, tag, ensureStartuml(lines));
	}

	private List<String> ensureStartuml(List<String> lines) {
		final String first = lines.get(0);
		if (first.startsWith("@start")) {
			return lines;
		}
		final List<String> copy = new ArrayList<String>(lines);
		copy.add(0, "@startuml");
		copy.add("@enduml");
		return copy;
	}

}
