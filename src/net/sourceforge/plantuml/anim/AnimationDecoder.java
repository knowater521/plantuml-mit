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
package net.sourceforge.plantuml.anim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.script.ScriptException;

public class AnimationDecoder {

	private final List<String> result = new ArrayList<String>();

	public AnimationDecoder(Iterable<CharSequence> data) throws ScriptException {
		
		for (final Iterator<CharSequence> it = data.iterator(); it.hasNext();) {
			String line = it.next().toString();
			if (line.matches("^\\s*\\[script\\]\\s*$")) {
				final StringBuilder scriptText = new StringBuilder();
				while (true) {
					line = it.next().toString();
					if (line.matches("^\\s*\\[/script\\]\\s*$")) {
						final AnimationScript script = new AnimationScript();
						final String out = script.eval(scriptText.toString());
						for (final StringTokenizer st = new StringTokenizer(out, "\n"); st.hasMoreTokens();) {
							result.add(st.nextToken());
						}
						break;
					} else {
						scriptText.append(line);
						scriptText.append("\n");
					}
				}
			} else {
				result.add(line);
			}
		}
	}

	public List<String> decode() {
		return Collections.unmodifiableList(result);
	}

}
