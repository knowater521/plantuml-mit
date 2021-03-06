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
package net.sourceforge.plantuml.descdiagram;

import net.sourceforge.plantuml.FontParam;
import net.sourceforge.plantuml.LineLocation;
import net.sourceforge.plantuml.Url;
import net.sourceforge.plantuml.UrlBuilder;
import net.sourceforge.plantuml.UrlBuilder.ModeUrl;
import net.sourceforge.plantuml.classdiagram.command.GenericRegexProducer;
import net.sourceforge.plantuml.command.CommandExecutionResult;
import net.sourceforge.plantuml.command.SingleLineCommand2;
import net.sourceforge.plantuml.command.regex.IRegex;
import net.sourceforge.plantuml.command.regex.RegexConcat;
import net.sourceforge.plantuml.command.regex.RegexLeaf;
import net.sourceforge.plantuml.command.regex.RegexResult;
import net.sourceforge.plantuml.cucadiagram.Code;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.GroupType;
import net.sourceforge.plantuml.cucadiagram.IEntity;
import net.sourceforge.plantuml.cucadiagram.IGroup;
import net.sourceforge.plantuml.cucadiagram.Ident;
import net.sourceforge.plantuml.cucadiagram.LeafType;
import net.sourceforge.plantuml.cucadiagram.NamespaceStrategy;
import net.sourceforge.plantuml.cucadiagram.Stereotype;
import net.sourceforge.plantuml.graphic.USymbol;
import net.sourceforge.plantuml.graphic.color.ColorType;

public class CommandCreateDomain extends SingleLineCommand2<DescriptionDiagram> {
	public static final String DISPLAY_WITH_GENERIC = "[%g](.+?)(?:\\<(" + GenericRegexProducer.PATTERN + ")\\>)?[%g]";
	public static final String CODE = "[^%s{}%g<>]+";

	public CommandCreateDomain() {
		super(getRegexConcat());
	}

	private static IRegex getRegexConcat() {
		return RegexConcat.build(CommandCreateDomain.class.getName(), RegexLeaf.start(), //
				new RegexLeaf("TYPE", "(requirement|domain)"), //
				RegexLeaf.spaceOneOrMore(), //
				new RegexLeaf("DISPLAY", DISPLAY_WITH_GENERIC), //
				RegexLeaf.spaceOneOrMore(), //
				new RegexLeaf("as"), //
				RegexLeaf.spaceOneOrMore(), new RegexLeaf("CODE", "([a-zA-Z0-9]+)"), RegexLeaf.spaceZeroOrMore(), //
				new RegexLeaf("STEREO", "(\\<\\<.+\\>\\>)?"), //
				// domain: lexical, causal, biddable
				// requirement: FR, NFR, quality
				RegexLeaf.spaceZeroOrMore(), //
				new RegexLeaf("GROUP", "(\\{)?"), RegexLeaf.end());
	}

	@Override
	protected CommandExecutionResult executeArg(DescriptionDiagram diagram, LineLocation location, RegexResult arg) {
		String type = arg.get("TYPE", 0);
		String display = arg.getLazzy("DISPLAY", 0);
		String codeString = arg.getLazzy("CODE", 0);
		if (codeString == null) {
			codeString = display;
		}
		// final String genericOption = arg.getLazzy("DISPLAY", 1);
		// final String generic = genericOption != null ? genericOption : arg.get("GENERIC", 0);

		final String stereotype = arg.get("STEREO", 0);

		final Ident ident = diagram.buildLeafIdent(codeString);
		final Code code = diagram.V1972() ? ident : diagram.buildCode(codeString);
		if (diagram.V1972() && diagram.leafExistSmart(ident)) {
			return CommandExecutionResult.error("Object already exists : " + codeString);
		}
		if (!diagram.V1972() && diagram.leafExist(code)) {
			return CommandExecutionResult.error("Object already exists : " + codeString);
		}
		Display d = Display.getWithNewlines(display);
		final String urlString = arg.get("URL", 0);
		final String group = arg.get("GROUP", 0);
		IEntity entity;
		if (group != null) {
			final IGroup currentGroup = diagram.getCurrentGroup();
			diagram.gotoGroup(ident, code, d, type.equalsIgnoreCase("domain") ? GroupType.DOMAIN
					: GroupType.REQUIREMENT, currentGroup, NamespaceStrategy.SINGLE);
			entity = diagram.getCurrentGroup();
		} else {
			entity = diagram.createLeaf(ident, code, d, type.equalsIgnoreCase("domain") ? LeafType.DOMAIN
					: LeafType.REQUIREMENT, null);
		}
		if (stereotype != null) {
			entity.setStereotype(new Stereotype(stereotype, diagram.getSkinParam().getCircledCharacterRadius(), diagram
					.getSkinParam().getFont(null, false, FontParam.CIRCLED_CHARACTER), diagram.getSkinParam()
					.getIHtmlColorSet()));
		}
		if (urlString != null) {
			final UrlBuilder urlBuilder = new UrlBuilder(diagram.getSkinParam().getValue("topurl"), ModeUrl.STRICT);
			final Url url = urlBuilder.getUrl(urlString);
			entity.addUrl(url);
		}
		entity.setSpecificColorTOBEREMOVED(ColorType.BACK,
				diagram.getSkinParam().getIHtmlColorSet().getColorIfValid(arg.get("COLOR", 0)));
		if (type.equalsIgnoreCase("domain")) {
			if (stereotype != null && stereotype.equalsIgnoreCase("<<Machine>>")) {
				type = "machine";
			}
			if (stereotype != null && stereotype.equalsIgnoreCase("<<Causal>>")) {
				type = "causal";
			}
			if (stereotype != null && stereotype.equalsIgnoreCase("<<Designed>>")) {
				type = "designed";
			}
			if (stereotype != null && stereotype.equalsIgnoreCase("<<Lexical>>")) {
				type = "lexical";
			}
			if (stereotype != null && stereotype.equalsIgnoreCase("<<Biddable>>")) {
				type = "biddable";
			}
		}
		USymbol usymbol = USymbol.fromString(type, diagram.getSkinParam());
		entity.setUSymbol(usymbol);
		return CommandExecutionResult.ok();
	}

}
