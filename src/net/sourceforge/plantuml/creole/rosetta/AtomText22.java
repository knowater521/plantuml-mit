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

import java.awt.font.LineMetrics;
import java.awt.geom.Dimension2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import net.sourceforge.plantuml.BackSlash;
import net.sourceforge.plantuml.Dimension2DDouble;
import net.sourceforge.plantuml.LineBreakStrategy;
import net.sourceforge.plantuml.Log;
import net.sourceforge.plantuml.Url;
import net.sourceforge.plantuml.creole.atom.AbstractAtom;
import net.sourceforge.plantuml.creole.atom.Atom;
import net.sourceforge.plantuml.graphic.FontConfiguration;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.graphic.TextBlockUtils;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.UText;
import net.sourceforge.plantuml.ugraphic.UTranslate;
import net.sourceforge.plantuml.ugraphic.color.HColor;
import net.sourceforge.plantuml.ugraphic.color.HColorAutomatic;
import net.sourceforge.plantuml.ugraphic.color.HColorSimple;
import net.sourceforge.plantuml.utils.CharHidder;

public class AtomText22 extends AbstractAtom implements Atom {

	interface DelayedDouble {
		public double getDouble(StringBounder stringBounder);
	}

	private static DelayedDouble ZERO = new DelayedDouble() {
		public double getDouble(StringBounder stringBounder) {
			return 0;
		}
	};

	private final FontConfiguration fontConfiguration;
	private final String text;
	private final DelayedDouble marginLeft;
	private final DelayedDouble marginRight;
	private final Url url;

	public static Atom create(String text, FontConfiguration fontConfiguration) {
		return new AtomText22(text, fontConfiguration, null, ZERO, ZERO);
	}

	@Override
	public String toString() {
		return text + " " + fontConfiguration;
	}

	private AtomText22(String text, FontConfiguration style, Url url, DelayedDouble marginLeft,
			DelayedDouble marginRight) {
		if (text.contains("" + BackSlash.hiddenNewLine())) {
			throw new IllegalArgumentException(text);
		}
		this.marginLeft = marginLeft;
		this.marginRight = marginRight;
		this.text = CharHidder.unhide(text);
//		this.text = StringUtils.manageTildeArobaseStart(StringUtils.manageUnicodeNotationUplus(
//				StringUtils.manageAmpDiese(StringUtils.showComparatorCharacters(CharHidder.unhide(text)))));
		this.fontConfiguration = style;
		this.url = url;
	}

	public FontConfiguration getFontConfiguration() {
		return fontConfiguration;
	}

	public Dimension2D calculateDimension(StringBounder stringBounder) {
		final Dimension2D rect = stringBounder.calculateDimension(fontConfiguration.getFont(), text);
		Log.debug("g2d=" + rect);
		Log.debug("Size for " + text + " is " + rect);
		double h = rect.getHeight();
		if (h < 10) {
			h = 10;
		}
		final double width = text.indexOf('\t') == -1 ? rect.getWidth() : getWidth(stringBounder);
		final double left = marginLeft.getDouble(stringBounder);
		final double right = marginRight.getDouble(stringBounder);

		return new Dimension2DDouble(width + left + right, h);
	}

	private double getDescent() {
		final LineMetrics fm = TextBlockUtils.getLineMetrics(fontConfiguration.getFont(), text);
		final double descent = fm.getDescent();
		return descent;
	}

	public double getFontSize2D() {
		return fontConfiguration.getFont().getSize2D();
	}

	public double getStartingAltitude(StringBounder stringBounder) {
		return fontConfiguration.getSpace();
	}

	private double getTabSize(StringBounder stringBounder) {
		return stringBounder.calculateDimension(fontConfiguration.getFont(), tabString()).getWidth();
	}

	private String tabString() {
		final int nb = fontConfiguration.getTabSize();
		if (nb >= 1 && nb < 7) {
			return "        ".substring(0, nb);
		}
		return "        ";
	}

	public void drawU(UGraphic ug) {
		if (url != null) {
			ug.startUrl(url);
		}
		if (ug.matchesProperty("SPECIALTXT")) {
			ug.draw(this);
			throw new UnsupportedOperationException("SPECIALTXT");
		} else {
			HColor textColor = fontConfiguration.getColor();
			FontConfiguration useFontConfiguration = fontConfiguration;
			if (textColor instanceof HColorAutomatic && ug.getParam().getBackcolor() != null) {
				textColor = ((HColorSimple) ug.getParam().getBackcolor()).opposite();
				useFontConfiguration = fontConfiguration.changeColor(textColor);
			}
			if (marginLeft != ZERO) {
				ug = ug.apply(UTranslate.dx(marginLeft.getDouble(ug.getStringBounder())));
			}

			final StringTokenizer tokenizer = new StringTokenizer(text, "\t", true);

			double x = 0;
			// final int ypos = fontConfiguration.getSpace();
			final Dimension2D rect = ug.getStringBounder().calculateDimension(fontConfiguration.getFont(), text);
			final double descent = getDescent();
			final double ypos = rect.getHeight() - descent;
			if (tokenizer.hasMoreTokens()) {
				final double tabSize = getTabSize(ug.getStringBounder());
				while (tokenizer.hasMoreTokens()) {
					final String s = tokenizer.nextToken();
					if (s.equals("\t")) {
						final double remainder = x % tabSize;
						x += tabSize - remainder;
					} else {
						final UText utext = new UText(s, useFontConfiguration);
						final Dimension2D dim = ug.getStringBounder().calculateDimension(fontConfiguration.getFont(),
								s);
						ug.apply(new UTranslate(x, ypos)).draw(utext);
						x += dim.getWidth();
					}
				}
			}
		}
		if (url != null) {
			ug.closeUrl();
		}
	}

	private double getWidth(StringBounder stringBounder) {
		return getWidth(stringBounder, text);
	}

	private double getWidth(StringBounder stringBounder, String text) {
		final StringTokenizer tokenizer = new StringTokenizer(text, "\t", true);
		final double tabSize = getTabSize(stringBounder);
		double x = 0;
		while (tokenizer.hasMoreTokens()) {
			final String s = tokenizer.nextToken();
			if (s.equals("\t")) {
				final double remainder = x % tabSize;
				x += tabSize - remainder;
			} else {
				final Dimension2D dim = stringBounder.calculateDimension(fontConfiguration.getFont(), s);
				x += dim.getWidth();
			}
		}
		return x;
	}

	public List<AtomText22> getSplitted(StringBounder stringBounder, LineBreakStrategy maxWidthAsString) {
		final double maxWidth = maxWidthAsString.getMaxWidth();
		if (maxWidth == 0) {
			throw new IllegalStateException();
		}
		final List<AtomText22> result = new ArrayList<AtomText22>();
		final StringTokenizer st = new StringTokenizer(text, " ", true);
		final StringBuilder currentLine = new StringBuilder();
		while (st.hasMoreTokens()) {
			final String token1 = st.nextToken();
			for (String tmp : Arrays.asList(token1)) {
				final double w = getWidth(stringBounder, currentLine + tmp);
				if (w > maxWidth) {
					result.add(new AtomText22(currentLine.toString(), fontConfiguration, url, marginLeft, marginRight));
					currentLine.setLength(0);
					if (tmp.startsWith(" ") == false) {
						currentLine.append(tmp);
					}
				} else {
					currentLine.append(tmp);
				}
			}
		}
		result.add(new AtomText22(currentLine.toString(), fontConfiguration, url, marginLeft, marginRight));
		return Collections.unmodifiableList(result);

	}

	@Override
	public List<Atom> splitInTwo(StringBounder stringBounder, double width) {
		final StringBuilder tmp = new StringBuilder();
		for (String token : splitted()) {
			if (tmp.length() > 0 && getWidth(stringBounder, tmp.toString() + token) > width) {
				final Atom part1 = new AtomText22(tmp.toString(), fontConfiguration, url, marginLeft, marginRight);
				String remain = text.substring(tmp.length());
				while (remain.startsWith(" ")) {
					remain = remain.substring(1);
				}

				final Atom part2 = new AtomText22(remain, fontConfiguration, url, marginLeft, marginRight);
				return Arrays.asList(part1, part2);
			}
			tmp.append(token);
		}
		return Collections.singletonList((Atom) this);
	}

	private Collection<String> splitted() {
		final List<String> result = new ArrayList<String>();
		for (int i = 0; i < text.length(); i++) {
			final char ch = text.charAt(i);
			if (Character.isLetter(ch)) {
				final StringBuilder tmp = new StringBuilder();
				tmp.append(ch);
				while (i + 1 < text.length() && Character.isLetter(text.charAt(i + 1))) {
					i++;
					tmp.append(text.charAt(i));
				}
				result.add(tmp.toString());
			} else {
				result.add("" + text.charAt(i));
			}
		}
		return result;
	}

	public final String getText() {
		return text;
	}

}
