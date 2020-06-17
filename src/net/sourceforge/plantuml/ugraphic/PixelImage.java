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
package net.sourceforge.plantuml.ugraphic;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import net.sourceforge.plantuml.ugraphic.color.ColorChangerMonochrome;

public class PixelImage implements MutableImage {

	private final BufferedImage bufferedImageScale1;
	private final double scale;
	private final AffineTransformType type;
	private BufferedImage cache = null;

	public PixelImage(BufferedImage bufferedImage, AffineTransformType type) {
		this(bufferedImage, type, 1);
	}

	private PixelImage(BufferedImage bufferedImage, AffineTransformType type, double scale) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		this.bufferedImageScale1 = bufferedImage;
		this.scale = scale;
		this.type = type;
	}

	public MutableImage withScale(double scale) {
		return new PixelImage(bufferedImageScale1, type, this.scale * scale);
	}

	public final BufferedImage getImage() {
		if (scale == 1) {
			return bufferedImageScale1;
		}
		if (cache == null) {
			final int w = (int) Math.round(bufferedImageScale1.getWidth() * scale);
			final int h = (int) Math.round(bufferedImageScale1.getHeight() * scale);
			final BufferedImage after = new BufferedImage(w, h, bufferedImageScale1.getType());
			final AffineTransform at = new AffineTransform();
			at.scale(scale, scale);
			final AffineTransformOp scaleOp = new AffineTransformOp(at, type.toLegacyInt());
			this.cache = scaleOp.filter(bufferedImageScale1, after);
		}
		return cache;
	}

	public MutableImage muteColor(Color newColor) {
		if (newColor == null) {
			return this;
		}
		int darkerRgb = getDarkerRgb();
		final BufferedImage copy = deepCopy();
		for (int i = 0; i < bufferedImageScale1.getWidth(); i++) {
			for (int j = 0; j < bufferedImageScale1.getHeight(); j++) {
				final int color = bufferedImageScale1.getRGB(i, j);
				final int rgb = getRgb(color);
				final int a = getA(color);
				if (a != 0 && rgb == darkerRgb) {
					copy.setRGB(i, j, newColor.getRGB() + a);
				}
			}
		}
		return new PixelImage(copy, type, scale);
	}

	public MutableImage muteTransparentColor(Color newColor) {
		if (newColor == null) {
			newColor = Color.WHITE;
		}
		final BufferedImage copy = deepCopy();
		for (int i = 0; i < bufferedImageScale1.getWidth(); i++) {
			for (int j = 0; j < bufferedImageScale1.getHeight(); j++) {
				final int color = bufferedImageScale1.getRGB(i, j);
				final int a = getA(color);
				if (a == 0) {
					copy.setRGB(i, j, newColor.getRGB());
				}
			}
		}
		return new PixelImage(copy, type, scale);
	}

	private int getDarkerRgb() {
		int darkerRgb = -1;
		for (int i = 0; i < bufferedImageScale1.getWidth(); i++) {
			for (int j = 0; j < bufferedImageScale1.getHeight(); j++) {
				final int color = bufferedImageScale1.getRGB(i, j);
				final int rgb = getRgb(color);
				final int a = getA(color);
				if (a != mask_a__) {
					continue;
				}
				// if (isTransparent(color)) {
				// continue;
				// }
				final int grey = ColorChangerMonochrome.getGrayScale(rgb);
				if (darkerRgb == -1 || grey < ColorChangerMonochrome.getGrayScale(darkerRgb)) {
					darkerRgb = rgb;
				}
			}
		}
		return darkerRgb;
	}

	private static final int mask_a__ = 0xFF000000;
	private static final int mask_rgb = 0x00FFFFFF;

	private int getRgb(int color) {
		return color & mask_rgb;
	}

	private int getA(int color) {
		return color & mask_a__;
	}

	// From
	// https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
//	private static BufferedImage deepCopyOld(BufferedImage bi) {
//		final ColorModel cm = bi.getColorModel();
//		final boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
//		final WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
//		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
//	}

	private BufferedImage deepCopy() {
		final BufferedImage result = new BufferedImage(bufferedImageScale1.getWidth(), bufferedImageScale1.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < this.bufferedImageScale1.getWidth(); i++) {
			for (int j = 0; j < this.bufferedImageScale1.getHeight(); j++) {
				result.setRGB(i, j, bufferedImageScale1.getRGB(i, j));
			}
		}
		return result;
	}

}
