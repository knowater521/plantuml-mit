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
package net.sourceforge.plantuml.cucadiagram;

import net.sourceforge.plantuml.svek.extremity.MiddleCircleCircledMode;
import net.sourceforge.plantuml.svek.extremity.MiddleFactory;
import net.sourceforge.plantuml.svek.extremity.MiddleFactoryCircle;
import net.sourceforge.plantuml.svek.extremity.MiddleFactoryCircleCircled;
import net.sourceforge.plantuml.ugraphic.color.HColor;

public enum LinkMiddleDecor {

	NONE, CIRCLE, CIRCLE_CIRCLED, CIRCLE_CIRCLED1, CIRCLE_CIRCLED2;

	public MiddleFactory getMiddleFactory(HColor backColor) {
		if (this == CIRCLE) {
			return new MiddleFactoryCircle(backColor);
		}
		if (this == CIRCLE_CIRCLED) {
			return new MiddleFactoryCircleCircled(MiddleCircleCircledMode.BOTH, backColor);
		}
		if (this == CIRCLE_CIRCLED1) {
			return new MiddleFactoryCircleCircled(MiddleCircleCircledMode.MODE1, backColor);
		}
		if (this == CIRCLE_CIRCLED2) {
			return new MiddleFactoryCircleCircled(MiddleCircleCircledMode.MODE2, backColor);
		}
		throw new UnsupportedOperationException();
	}

}
