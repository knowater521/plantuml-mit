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
 * Creator:  Hisashi Miyashita
 */
package net.sourceforge.plantuml.svek.extremity;

import java.awt.geom.Point2D;

import net.sourceforge.plantuml.graphic.UDrawable;
import net.sourceforge.plantuml.svek.AbstractExtremityFactory;
import net.sourceforge.plantuml.svek.Side;
import net.sourceforge.plantuml.ugraphic.color.HColor;

public class ExtremityFactoryExtendsLike extends AbstractExtremityFactory implements ExtremityFactory {

	private final HColor backgroundColor;
	private final boolean definedBy;

	public ExtremityFactoryExtendsLike(HColor backgroundColor, boolean definedBy) {
		this.backgroundColor = backgroundColor;
		this.definedBy = definedBy;
	}

	@Override
	public UDrawable createUDrawable(Point2D p0, double angle, Side side) {
		if (definedBy) {
			return new ExtremityExtendsLike.DefinedBy(p0, angle, backgroundColor);
		} else {
			return new ExtremityExtendsLike.Redefines(p0, angle, backgroundColor);
		}
	}

	public UDrawable createUDrawable(Point2D p0, Point2D p1, Point2D p2, Side side) {
		final double ortho = atan2(p0, p2) + (Math.PI / 2.0);
		if (definedBy) {
			return new ExtremityExtendsLike.DefinedBy(p1, ortho, backgroundColor);
		} else {
			return new ExtremityExtendsLike.Redefines(p1, ortho, backgroundColor);
		}
	}
}
