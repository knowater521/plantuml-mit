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
package net.sourceforge.plantuml.svek;

import java.awt.geom.Dimension2D;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.plantuml.ColorParam;
import net.sourceforge.plantuml.Dimension2DDouble;
import net.sourceforge.plantuml.SkinParam;
import net.sourceforge.plantuml.UmlDiagramType;
import net.sourceforge.plantuml.cucadiagram.dot.DotData;
import net.sourceforge.plantuml.graphic.AbstractTextBlock;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.graphic.TextBlockUtils;
import net.sourceforge.plantuml.skin.rose.Rose;
import net.sourceforge.plantuml.style.PName;
import net.sourceforge.plantuml.style.SName;
import net.sourceforge.plantuml.style.Style;
import net.sourceforge.plantuml.style.StyleSignature;
import net.sourceforge.plantuml.ugraphic.MinMax;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.UHidden;
import net.sourceforge.plantuml.ugraphic.UStroke;
import net.sourceforge.plantuml.ugraphic.UTranslate;
import net.sourceforge.plantuml.ugraphic.color.HColor;
import net.sourceforge.plantuml.ugraphic.color.HColorUtils;

public final class SvekResult extends AbstractTextBlock implements IEntityImage {

	private final Rose rose = new Rose();

	private final DotData dotData;
	private final DotStringFactory dotStringFactory;

	public SvekResult(DotData dotData, DotStringFactory dotStringFactory) {
		this.dotData = dotData;
		this.dotStringFactory = dotStringFactory;
	}

	public void drawU(UGraphic ug) {

		for (Cluster cluster : dotStringFactory.getBibliotekon().allCluster()) {
			cluster.drawU(ug, new UStroke(1.5), dotData.getUmlDiagramType(), dotData.getSkinParam());
		}

		HColor color = rose.getHtmlColor(dotData.getSkinParam(), null, getArrowColorParam());
		if (SkinParam.USE_STYLES()) {
			final Style style = getDefaultStyleDefinition()
					.getMergedStyle(dotData.getSkinParam().getCurrentStyleBuilder());
			color = style.value(PName.LineColor).asColor(dotData.getSkinParam().getIHtmlColorSet());
		}
		color = HColorUtils.noGradient(color);

		for (Node node : dotStringFactory.getBibliotekon().allNodes()) {
			final double minX = node.getMinX();
			final double minY = node.getMinY();
			final UGraphic ug2 = node.isHidden() ? ug.apply(UHidden.HIDDEN) : ug;
			final IEntityImage image = node.getImage();
			image.drawU(ug2.apply(new UTranslate(minX, minY)));
			if (image instanceof Untranslated) {
				((Untranslated) image).drawUntranslated(ug.apply(color), minX, minY);
			}
			// shape.getImage().drawNeighborhood(ug2, minX, minY);
		}

		final Set<String> ids = new HashSet<String>();

		for (Line line : dotStringFactory.getBibliotekon().allLines()) {
			final UGraphic ug2 = line.isHidden() ? ug.apply(UHidden.HIDDEN) : ug;
			line.drawU(ug2, color, ids);
		}

	}

	private ColorParam getArrowColorParam() {
		if (dotData.getUmlDiagramType() == UmlDiagramType.CLASS) {
			return ColorParam.arrow;
		} else if (dotData.getUmlDiagramType() == UmlDiagramType.OBJECT) {
			return ColorParam.arrow;
		} else if (dotData.getUmlDiagramType() == UmlDiagramType.DESCRIPTION) {
			return ColorParam.arrow;
		} else if (dotData.getUmlDiagramType() == UmlDiagramType.ACTIVITY) {
			return ColorParam.arrow;
		} else if (dotData.getUmlDiagramType() == UmlDiagramType.STATE) {
			return ColorParam.arrow;
		}
		throw new IllegalStateException();
	}

	private StyleSignature getDefaultStyleDefinition() {
		return StyleSignature.of(SName.root, SName.element, dotData.getUmlDiagramType().getStyleName(), SName.arrow);
	}

	// Duplicate SvekResult / GeneralImageBuilder
	public HColor getBackcolor() {
		if (SkinParam.USE_STYLES()) {
			final Style style = StyleSignature.of(SName.root, SName.document)
					.getMergedStyle(dotData.getSkinParam().getCurrentStyleBuilder());
			return style.value(PName.BackGroundColor).asColor(dotData.getSkinParam().getIHtmlColorSet());
		}
		return dotData.getSkinParam().getBackgroundColor(false);
	}

	private MinMax minMax;

	public Dimension2D calculateDimension(StringBounder stringBounder) {
		if (minMax == null) {
			minMax = TextBlockUtils.getMinMax(this, stringBounder, false);
			dotStringFactory.moveSvek(6 - minMax.getMinX(), 6 - minMax.getMinY());
		}
		return Dimension2DDouble.delta(minMax.getDimension(), 0, 12);
	}

	public ShapeType getShapeType() {
		return ShapeType.RECTANGLE;
	}

	public Margins getShield(StringBounder stringBounder) {
		return Margins.NONE;
	}

	public boolean isHidden() {
		return false;
	}

	public double getOverscanX(StringBounder stringBounder) {
		return 0;
	}

}
