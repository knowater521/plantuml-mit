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
package net.sourceforge.plantuml.cute;

import java.awt.geom.Point2D;

// http://rosettacode.org/wiki/Problem_of_Apollonius#Java
public class ApolloniusSolver2 {

	/**
	 * Solves the Problem of Apollonius (finding a circle tangent to three other circles in the plane). The method uses
	 * approximately 68 heavy operations (multiplication, division, square-roots).
	 * 
	 * @param c1
	 *            One of the circles in the problem
	 * @param c2
	 *            One of the circles in the problem
	 * @param c3
	 *            One of the circles in the problem
	 * @param s1
	 *            An indication if the solution should be externally or internally tangent (+1/-1) to c1
	 * @param s2
	 *            An indication if the solution should be externally or internally tangent (+1/-1) to c2
	 * @param s3
	 *            An indication if the solution should be externally or internally tangent (+1/-1) to c3
	 * @return The circle that is tangent to c1, c2 and c3.
	 */
	public static Balloon solveApollonius(Balloon c1, Balloon c2, Balloon c3, int s1, int s2, int s3) {
		double x1 = c1.getCenter().getX();
		double y1 = c1.getCenter().getY();
		double r1 = c1.getRadius();
		double x2 = c2.getCenter().getX();
		double y2 = c2.getCenter().getY();
		double r2 = c2.getRadius();
		double x3 = c3.getCenter().getX();
		double y3 = c3.getCenter().getY();
		double r3 = c3.getRadius();

		// Currently optimized for fewest multiplications. Should be optimized for
		// readability
		double v11 = 2 * x2 - 2 * x1;
		double v12 = 2 * y2 - 2 * y1;
		double v13 = x1 * x1 - x2 * x2 + y1 * y1 - y2 * y2 - r1 * r1 + r2 * r2;
		double v14 = 2 * s2 * r2 - 2 * s1 * r1;

		double v21 = 2 * x3 - 2 * x2;
		double v22 = 2 * y3 - 2 * y2;
		double v23 = x2 * x2 - x3 * x3 + y2 * y2 - y3 * y3 - r2 * r2 + r3 * r3;
		double v24 = 2 * s3 * r3 - 2 * s2 * r2;

		double w12 = v12 / v11;
		double w13 = v13 / v11;
		double w14 = v14 / v11;

		double w22 = v22 / v21 - w12;
		double w23 = v23 / v21 - w13;
		double w24 = v24 / v21 - w14;

		double P = -w23 / w22;
		double Q = w24 / w22;
		double M = -w12 * P - w13;
		double N = w14 - w12 * Q;

		double a = N * N + Q * Q - 1;
		double b = 2 * M * N - 2 * N * x1 + 2 * P * Q - 2 * Q * y1 + 2 * s1 * r1;
		double c = x1 * x1 + M * M - 2 * M * x1 + P * P + y1 * y1 - 2 * P * y1 - r1 * r1;

		// Find a root of a quadratic equation. This requires the circle centers not
		// to be e.g. colinear
		double D = b * b - 4 * a * c;
		double rs = (-b - Math.sqrt(D)) / (2 * a);
		double xs = M + N * rs;
		double ys = P + Q * rs;
		return new Balloon(new Point2D.Double(xs, ys), rs);
	}


}
