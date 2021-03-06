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
package net.sourceforge.plantuml.objectdiagram.command;

import net.sourceforge.plantuml.FontParam;
import net.sourceforge.plantuml.StringLocated;
import net.sourceforge.plantuml.UrlBuilder;
import net.sourceforge.plantuml.command.BlocLines;
import net.sourceforge.plantuml.command.CommandExecutionResult;
import net.sourceforge.plantuml.command.CommandMultilines2;
import net.sourceforge.plantuml.command.MultilinesStrategy;
import net.sourceforge.plantuml.command.regex.IRegex;
import net.sourceforge.plantuml.command.regex.RegexConcat;
import net.sourceforge.plantuml.command.regex.RegexLeaf;
import net.sourceforge.plantuml.command.regex.RegexResult;
import net.sourceforge.plantuml.cucadiagram.BodierMap;
import net.sourceforge.plantuml.cucadiagram.Code;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.IEntity;
import net.sourceforge.plantuml.cucadiagram.ILeaf;
import net.sourceforge.plantuml.cucadiagram.Ident;
import net.sourceforge.plantuml.cucadiagram.LeafType;
import net.sourceforge.plantuml.cucadiagram.Link;
import net.sourceforge.plantuml.cucadiagram.LinkDecor;
import net.sourceforge.plantuml.cucadiagram.LinkType;
import net.sourceforge.plantuml.cucadiagram.Stereotype;
import net.sourceforge.plantuml.graphic.color.ColorParser;
import net.sourceforge.plantuml.graphic.color.ColorType;
import net.sourceforge.plantuml.objectdiagram.AbstractClassOrObjectDiagram;

public class CommandCreateMap extends CommandMultilines2<AbstractClassOrObjectDiagram> {

	public CommandCreateMap() {
		super(getRegexConcat(), MultilinesStrategy.REMOVE_STARTING_QUOTE);
	}

	private static IRegex getRegexConcat() {
		return RegexConcat.build(CommandCreateMap.class.getName(), RegexLeaf.start(), //
				new RegexLeaf("TYPE", "map"), //
				RegexLeaf.spaceOneOrMore(), //
				new RegexLeaf("NAME", "(?:[%g]([^%g]+)[%g][%s]+as[%s]+)?([\\p{L}0-9_.]+)"), //
				RegexLeaf.spaceZeroOrMore(), //
				new RegexLeaf("STEREO", "(\\<\\<.+\\>\\>)?"), //
				RegexLeaf.spaceZeroOrMore(), //
				new RegexLeaf("URL", "(" + UrlBuilder.getRegexp() + ")?"), //
				RegexLeaf.spaceZeroOrMore(), //
				ColorParser.exp1(), //
				RegexLeaf.spaceZeroOrMore(), //
				new RegexLeaf("\\{"), //
				RegexLeaf.end());
	}

	@Override
	public String getPatternEnd() {
		return "(?i)^[%s]*\\}[%s]*$";
	}

	@Override
	protected CommandExecutionResult executeNow(AbstractClassOrObjectDiagram diagram, BlocLines lines) {
		lines = lines.trim().removeEmptyLines();
		final RegexResult line0 = getStartingPattern().matcher(lines.getFirst().getTrimmed().getString());
		final IEntity entity1 = executeArg0(diagram, line0);
		if (entity1 == null) {
			return CommandExecutionResult.error("No such entity");
		}
		lines = lines.subExtract(1, 1);
		for (StringLocated sl : lines) {
			final String line = sl.getString();
			assert line.length() > 0;
			entity1.getBodier().addFieldOrMethod(line);
			if (BodierMap.getLinkedEntry(line) != null) {
				final String linkStr = BodierMap.getLinkedEntry(line);
				final int x = line.indexOf(linkStr);
				final String key = line.substring(0, x).trim();
				final String dest = line.substring(x + linkStr.length()).trim();
				final Ident ident2 = diagram.buildLeafIdentSpecial(dest);
				final ILeaf entity2 = diagram.getEntityFactory().getLeafStrict(ident2);
				final LinkType linkType = new LinkType(LinkDecor.ARROW, LinkDecor.NONE);
				final int length = linkStr.length() - 2;
				final Link link = new Link(entity1, entity2, linkType, Display.NULL, length,
						diagram.getSkinParam().getCurrentStyleBuilder());
				link.setPortMembers(key, null);
				diagram.addLink(link);
			}
		}
		return CommandExecutionResult.ok();
	}

	private IEntity executeArg0(AbstractClassOrObjectDiagram diagram, RegexResult line0) {
		final String name = line0.get("NAME", 1);
		final Ident ident = diagram.buildLeafIdent(name);
		final Code code = diagram.V1972() ? ident : diagram.buildCode(name);
		final String display = line0.get("NAME", 0);
		final String stereotype = line0.get("STEREO", 0);
		final boolean leafExist = diagram.V1972() ? diagram.leafExistSmart(ident) : diagram.leafExist(code);
		if (leafExist) {
			return diagram.getOrCreateLeaf(diagram.buildLeafIdent(name), code, LeafType.MAP, null);
		}
		final IEntity entity = diagram.createLeaf(ident, code, Display.getWithNewlines(display), LeafType.MAP, null);
		if (stereotype != null) {
			entity.setStereotype(new Stereotype(stereotype, diagram.getSkinParam().getCircledCharacterRadius(),
					diagram.getSkinParam().getFont(null, false, FontParam.CIRCLED_CHARACTER),
					diagram.getSkinParam().getIHtmlColorSet()));
		}
		entity.setSpecificColorTOBEREMOVED(ColorType.BACK,
				diagram.getSkinParam().getIHtmlColorSet().getColorIfValid(line0.get("COLOR", 0)));
		return entity;
	}

}
