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
package net.sourceforge.plantuml.ugraphic.g2d;

import java.awt.GradientPaint;
import java.awt.Graphics2D;

import net.sourceforge.plantuml.ugraphic.MinMax;
import net.sourceforge.plantuml.ugraphic.UDriver;
import net.sourceforge.plantuml.ugraphic.UParam;
import net.sourceforge.plantuml.ugraphic.UPath;
import net.sourceforge.plantuml.ugraphic.USegment;
import net.sourceforge.plantuml.ugraphic.USegmentType;
import net.sourceforge.plantuml.ugraphic.UShape;
import net.sourceforge.plantuml.ugraphic.arc.ExtendedGeneralPath;
import net.sourceforge.plantuml.ugraphic.color.ColorMapper;
import net.sourceforge.plantuml.ugraphic.color.HColor;
import net.sourceforge.plantuml.ugraphic.color.HColorGradient;
import net.sourceforge.plantuml.ugraphic.color.HColorUtils;

public class DriverPathG2d extends DriverShadowedG2d implements UDriver<Graphics2D> {

	private final double dpiFactor;

	public DriverPathG2d(double dpiFactor) {
		this.dpiFactor = dpiFactor;
	}

	public void draw(UShape ushape, final double x, final double y, ColorMapper mapper, UParam param, Graphics2D g2d) {
		final UPath shape = (UPath) ushape;
		DriverLineG2d.manageStroke(param, g2d);

		final HColor back = param.getBackcolor();

		final ExtendedGeneralPath p = new ExtendedGeneralPath();
		MinMax minMax = MinMax.getEmpty(false);
		minMax = minMax.addPoint(x, y);
		for (USegment seg : shape) {
			final USegmentType type = seg.getSegmentType();
			final double coord[] = seg.getCoord();
			if (type == USegmentType.SEG_MOVETO) {
				p.moveTo(x + coord[0], y + coord[1]);
				minMax = minMax.addPoint(x + coord[0], y + coord[1]);
			} else if (type == USegmentType.SEG_LINETO) {
				p.lineTo(x + coord[0], y + coord[1]);
				minMax = minMax.addPoint(x + coord[0], y + coord[1]);
			} else if (type == USegmentType.SEG_CUBICTO) {
				p.curveTo(x + coord[0], y + coord[1], x + coord[2], y + coord[3], x + coord[4], y + coord[5]);
				minMax = minMax.addPoint(x + coord[4], y + coord[5]);
			} else if (type == USegmentType.SEG_ARCTO) {
				p.arcTo(coord[0], coord[1], coord[2], coord[3] != 0, coord[4] != 0, x + coord[5], y + coord[6]);
			} else {
				throw new UnsupportedOperationException();
			}
		}

		if (shape.isOpenIconic()) {
			p.closePath();
			g2d.setColor(mapper.toColor(param.getColor()));
			g2d.fill(p);
			return;
		}

		// Shadow
		if (shape.getDeltaShadow() != 0) {
			if (back == null || HColorUtils.isTransparent(back)) {
				drawOnlyLineShadowSpecial(g2d, p, shape.getDeltaShadow(), dpiFactor);
			} else {
				drawShadow(g2d, p, shape.getDeltaShadow(), dpiFactor);
			}
		}

		if (back instanceof HColorGradient) {
			final HColorGradient gr = (HColorGradient) back;
			final char policy = gr.getPolicy();
			final GradientPaint paint;
			if (policy == '|') {
				paint = new GradientPaint((float) minMax.getMinX(), (float) minMax.getMaxY() / 2,
						mapper.toColor(gr.getColor1()), (float) minMax.getMaxX(), (float) minMax.getMaxY() / 2,
						mapper.toColor(gr.getColor2()));
			} else if (policy == '\\') {
				paint = new GradientPaint((float) minMax.getMinX(), (float) minMax.getMaxY(),
						mapper.toColor(gr.getColor1()), (float) minMax.getMaxX(), (float) minMax.getMinY(),
						mapper.toColor(gr.getColor2()));
			} else if (policy == '-') {
				paint = new GradientPaint((float) minMax.getMaxX() / 2, (float) minMax.getMinY(),
						mapper.toColor(gr.getColor1()), (float) minMax.getMaxX() / 2, (float) minMax.getMaxY(),
						mapper.toColor(gr.getColor2()));
			} else {
				// for /
				paint = new GradientPaint((float) x, (float) y, mapper.toColor(gr.getColor1()),
						(float) minMax.getMaxX(), (float) minMax.getMaxY(), mapper.toColor(gr.getColor2()));
			}
			g2d.setPaint(paint);
			g2d.fill(p);
		} else if (back != null) {
			g2d.setColor(mapper.toColor(back));
			g2d.fill(p);
		}

		if (param.getColor() != null) {
			g2d.setColor(mapper.toColor(param.getColor()));
			g2d.draw(p);
		}
	}

}
