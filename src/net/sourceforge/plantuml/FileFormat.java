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
package net.sourceforge.plantuml;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import net.sourceforge.plantuml.braille.BrailleCharFactory;
import net.sourceforge.plantuml.braille.UGraphicBraille;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.png.MetadataTag;
import net.sourceforge.plantuml.security.SFile;
import net.sourceforge.plantuml.svg.SvgGraphics;
import net.sourceforge.plantuml.ugraphic.UFont;

/**
 * Format for output files generated by PlantUML.
 * 
 * @author Arnaud Roques
 * 
 */
public enum FileFormat {
	PNG, SVG, EPS, EPS_TEXT, ATXT, UTXT, XMI_STANDARD, XMI_STAR, XMI_ARGO, SCXML, PDF, MJPEG, ANIMATED_GIF, HTML, HTML5,
	VDX, LATEX, LATEX_NO_PREAMBLE, BASE64, BRAILLE_PNG, PREPROC;

	/**
	 * Returns the file format to be used for that format.
	 * 
	 * @return a string starting by a point.
	 */
	public String getFileSuffix() {
		if (name().startsWith("XMI")) {
			return ".xmi";
		}
		if (this == MJPEG) {
			return ".avi";
		}
		if (this == LATEX_NO_PREAMBLE) {
			return ".latex";
		}
		if (this == ANIMATED_GIF) {
			return ".gif";
		}
		if (this == BRAILLE_PNG) {
			return ".braille.png";
		}
		if (this == EPS_TEXT) {
			return EPS.getFileSuffix();
		}
		return "." + StringUtils.goLowerCase(name());
	}

	final static private BufferedImage imDummy = new BufferedImage(800, 100, BufferedImage.TYPE_INT_RGB);
	final static public Graphics2D gg = imDummy.createGraphics();
	static {
		// KEY_FRACTIONALMETRICS
		gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	public StringBounder getDefaultStringBounder(TikzFontDistortion tikzFontDistortion) {
		if (this == LATEX || this == LATEX_NO_PREAMBLE) {
			return getTikzStringBounder(tikzFontDistortion);
		}
		if (this == BRAILLE_PNG) {
			return getBrailleStringBounder();

		}
		return getNormalStringBounder();
	}

	private StringBounder getNormalStringBounder() {
		return new StringBounder() {
			@Override
			public String toString() {
				return "FileFormat::getNormalStringBounder";
			}

			public Dimension2D calculateDimension(UFont font, String text) {
				return getJavaDimension(font, text);
			}

		};
	}

	private Dimension2DDouble getJavaDimension(UFont font, String text) {
		final Font javaFont = font.getFont();
		final FontMetrics fm = gg.getFontMetrics(javaFont);
		final Rectangle2D rect = fm.getStringBounds(text, gg);
		return new Dimension2DDouble(rect.getWidth(), rect.getHeight());
	}

	private StringBounder getBrailleStringBounder() {
		return new StringBounder() {
			@Override
			public String toString() {
				return "FileFormat::getBrailleStringBounder";
			}

			public Dimension2D calculateDimension(UFont font, String text) {
				final int nb = BrailleCharFactory.build(text).size();
				final double quanta = UGraphicBraille.QUANTA;
				final double height = 5 * quanta;
				final double width = 3 * nb * quanta + 1;
				return new Dimension2DDouble(width, height);
			}
		};
	}

	private StringBounder getTikzStringBounder(final TikzFontDistortion tikzFontDistortion) {
		return new StringBounder() {
			@Override
			public String toString() {
				return "FileFormat::getTikzStringBounder";
			}

			public Dimension2D calculateDimension(UFont font, String text) {
				final Dimension2DDouble w1 = getJavaDimension(font.goTikz(-1), text);
				final Dimension2DDouble w2 = getJavaDimension(font.goTikz(0), text);
				final Dimension2DDouble w3 = getJavaDimension(font.goTikz(1), text);
				final double factor = (w3.getWidth() - w1.getWidth()) / w2.getWidth();
				final double distortion = tikzFontDistortion.getDistortion();
				final double magnify = tikzFontDistortion.getMagnify();
				final double delta = (w2.getWidth() - w1.getWidth()) * factor * distortion;
				return w2.withWidth(Math.max(w1.getWidth(), magnify * w2.getWidth() - delta));
			}
		};
	}

	/**
	 * Check if this file format is Encapsulated PostScript.
	 * 
	 * @return <code>true</code> for EPS.
	 */
	public boolean isEps() {
		if (this == EPS) {
			return true;
		}
		if (this == EPS_TEXT) {
			return true;
		}
		return false;
	}

	public String changeName(String fileName, int cpt) {
		if (cpt == 0) {
			return changeName(fileName, getFileSuffix());
		}
		return changeName(fileName,
				OptionFlags.getInstance().getFileSeparator() + String.format("%03d", cpt) + getFileSuffix());
	}

	private SFile computeFilename(SFile pngFile, int i) {
		if (i == 0) {
			return pngFile;
		}
		final SFile dir = pngFile.getParentFile();
		return dir.file(computeFilenameInternal(pngFile.getName(), i));
	}

	private String changeName(String fileName, String replacement) {
		String result = fileName.replaceAll("\\.\\w+$", replacement);
		if (result.equals(fileName)) {
			result = fileName + replacement;
		}
		return result;
	}

	private String computeFilenameInternal(String name, int i) {
		if (i == 0) {
			return name;
		}
		return name.replaceAll("\\" + getFileSuffix() + "$",
				OptionFlags.getInstance().getFileSeparator() + String.format("%03d", i) + getFileSuffix());
	}

	public boolean doesSupportMetadata() {
		return this == PNG || this == SVG;
	}

	public boolean equalsMetadata(String currentMetadata, SFile existingFile) {
		try {
			if (this == PNG) {
				final MetadataTag tag = new MetadataTag(existingFile, "plantuml");
				final String previousMetadata = tag.getData();
				final boolean sameMetadata = currentMetadata.equals(previousMetadata);
				return sameMetadata;
			}
			if (this == SVG) {
				final String svg = FileUtils.readSvg(existingFile);
				if (svg == null) {
					return false;
				}
				final String currentSignature = SvgGraphics.getMD5Hex(currentMetadata);
				final int idx = svg.lastIndexOf(SvgGraphics.MD5_HEADER);
				if (idx != -1) {
					final String part = svg.substring(idx + SvgGraphics.MD5_HEADER.length());
					return part.startsWith(currentSignature);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
