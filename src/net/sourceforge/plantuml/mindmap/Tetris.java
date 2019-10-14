/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2020, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * http://plantuml.com/patreon (only 1$ per month!)
 * http://plantuml.com/paypal
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
package net.sourceforge.plantuml.mindmap;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tetris {

	private final StripeFrontier frontier = new StripeFrontier();
	private final List<SymetricalTeePositioned> elements = new ArrayList<SymetricalTeePositioned>();
	private double minY = Double.MAX_VALUE;
	private double maxY = -Double.MAX_VALUE;
	private String name;

	public Tetris(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name + "(" + elements.size() + ")";
	}

	public void balance() {
		if (elements.size() == 0) {
			return;
		}
		if (minY != Double.MAX_VALUE) {
			throw new IllegalStateException();
		}
		for (SymetricalTeePositioned element : elements) {
			minY = Math.min(minY, element.getMinY());
			maxY = Math.max(maxY, element.getMaxY());
		}
		final double mean = (minY + maxY) / 2;
		for (SymetricalTeePositioned stp : elements) {
			stp.move(-mean);
		}
		// System.err.println("Balanced=" + this + " " + elements);
	}

	public double getHeight() {
		if (elements.size() == 0) {
			return 0;
		}
		return maxY - minY;
	}

	public double getWidth() {
		double result = 0;
		for (SymetricalTeePositioned tee : elements) {
			result = Math.max(result, tee.getMaxX());
		}
		return result;
	}

	public void add(SymetricalTee tee) {
		// System.err.println("Adding in " + this + " " + tee);

		if (frontier.isEmpty()) {
			final SymetricalTeePositioned p1 = new SymetricalTeePositioned(tee);
			addInternal(p1);
			return;
		}

		// System.err.println("frontier=" + frontier);

		final double c1 = frontier.getContact(0, tee.getElongation1());
		final double c2 = frontier.getContact(tee.getElongation1(), tee.getElongation1() + tee.getElongation2());

		// System.err.println("c1=" + c1 + " c2=" + c2);

		final SymetricalTeePositioned p1 = new SymetricalTeePositioned(tee);
		p1.moveSoThatSegmentA1isOn(c1);

		final SymetricalTeePositioned p2 = new SymetricalTeePositioned(tee);
		p2.moveSoThatSegmentA2isOn(c2);

		final SymetricalTeePositioned result = p1.getMax(p2);

		// System.err.println("p1=" + p1.getY() + " p2=" + p2.getY());
		// System.err.println("result=" + result.getY());
		addInternal(result);
	}

	private void addInternal(SymetricalTeePositioned result) {
		this.elements.add(result);
		final Line2D b1 = result.getSegmentB1();
		frontier.addSegment(b1.getX1(), b1.getX2(), b1.getY1());
		assert b1.getY1() == b1.getY2();

		final Line2D b2 = result.getSegmentB2();
		if (b2.getX1() != b2.getX2()) {
			frontier.addSegment(b2.getX1(), b2.getX2(), b2.getY1());
		}
		assert b2.getY1() == b2.getY2();
	}

	public List<SymetricalTeePositioned> getElements() {
		return Collections.unmodifiableList(elements);
	}

}