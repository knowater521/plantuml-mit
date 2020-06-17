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
package net.sourceforge.plantuml.classdiagram.command;

import net.sourceforge.plantuml.LineLocation;
import net.sourceforge.plantuml.StringUtils;
import net.sourceforge.plantuml.UmlDiagram;
import net.sourceforge.plantuml.command.CommandExecutionResult;
import net.sourceforge.plantuml.command.SingleLineCommand2;
import net.sourceforge.plantuml.command.regex.IRegex;
import net.sourceforge.plantuml.command.regex.RegexConcat;
import net.sourceforge.plantuml.command.regex.RegexLeaf;
import net.sourceforge.plantuml.command.regex.RegexOptional;
import net.sourceforge.plantuml.command.regex.RegexResult;
import net.sourceforge.plantuml.cucadiagram.Code;
import net.sourceforge.plantuml.cucadiagram.EntityGender;
import net.sourceforge.plantuml.cucadiagram.EntityGenderUtils;
import net.sourceforge.plantuml.cucadiagram.EntityPortion;
import net.sourceforge.plantuml.cucadiagram.EntityUtils;
import net.sourceforge.plantuml.cucadiagram.IEntity;
import net.sourceforge.plantuml.cucadiagram.Ident;
import net.sourceforge.plantuml.cucadiagram.LeafType;
import net.sourceforge.plantuml.descdiagram.DescriptionDiagram;
import net.sourceforge.plantuml.objectdiagram.AbstractClassOrObjectDiagram;
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram;

public class CommandHideShowByGender extends SingleLineCommand2<UmlDiagram> {

	public CommandHideShowByGender() {
		super(getRegexConcat());
	}

	static IRegex getRegexConcat() {
		return RegexConcat
				.build(CommandHideShowByGender.class.getName(),
						RegexLeaf.start(), //
						new RegexLeaf("COMMAND", "(hide|show)"), //
						RegexLeaf.spaceOneOrMore(), //
						new RegexLeaf("GENDER",
								"(?:(class|object|interface|enum|annotation|abstract|[\\p{L}0-9_.]+|[%g][^%g]+[%g]|\\<\\<.*\\>\\>)[%s]+)*?"), //
						new RegexOptional( //
								new RegexConcat( //
										new RegexLeaf("EMPTY", "(empty)"), //
										RegexLeaf.spaceOneOrMore()) //
						), //
						new RegexLeaf("PORTION",
								"(members?|attributes?|fields?|methods?|circles?|circled?|stereotypes?)"), //
						RegexLeaf.end());
	}

	private final EntityGender emptyByGender(EntityPortion portion) {
		if (portion == EntityPortion.METHOD) {
			return EntityGenderUtils.emptyMethods();
		}
		if (portion == EntityPortion.FIELD) {
			return EntityGenderUtils.emptyFields();
		}
		if (portion == EntityPortion.MEMBER) {
			throw new IllegalArgumentException();
			// return EntityGenderUtils.emptyMembers();
		}
		return EntityGenderUtils.all();
	}

	@Override
	protected CommandExecutionResult executeArg(UmlDiagram diagram, LineLocation location, RegexResult arg) {
		if (diagram instanceof AbstractClassOrObjectDiagram) {
			return executeClassDiagram((AbstractClassOrObjectDiagram) diagram, arg);
		}
		if (diagram instanceof DescriptionDiagram) {
			return executeDescriptionDiagram((DescriptionDiagram) diagram, arg);
		}
		if (diagram instanceof SequenceDiagram) {
			return executeSequenceDiagram((SequenceDiagram) diagram, arg);
		}
		// Just ignored
		return CommandExecutionResult.ok();
	}

	private CommandExecutionResult executeSequenceDiagram(SequenceDiagram diagram, RegexResult arg) {
		final EntityPortion portion = getEntityPortion(arg.get("PORTION", 0));
		diagram.hideOrShow(portion.asSet(), arg.get("COMMAND", 0).equalsIgnoreCase("show"));
		return CommandExecutionResult.ok();
	}

	private CommandExecutionResult executeDescriptionDiagram(DescriptionDiagram diagram, RegexResult arg) {
		final EntityPortion portion = getEntityPortion(arg.get("PORTION", 0));
		final EntityGender gender;
		final String arg1 = arg.get("GENDER", 0);
		if (arg1 == null) {
			gender = EntityGenderUtils.all();
		} else if (arg1.equalsIgnoreCase("class")) {
			gender = EntityGenderUtils.byEntityType(LeafType.CLASS);
		} else if (arg1.equalsIgnoreCase("object")) {
			gender = EntityGenderUtils.byEntityType(LeafType.OBJECT);
		} else if (arg1.equalsIgnoreCase("interface")) {
			gender = EntityGenderUtils.byEntityType(LeafType.INTERFACE);
		} else if (arg1.equalsIgnoreCase("enum")) {
			gender = EntityGenderUtils.byEntityType(LeafType.ENUM);
		} else if (arg1.equalsIgnoreCase("abstract")) {
			gender = EntityGenderUtils.byEntityType(LeafType.ABSTRACT_CLASS);
		} else if (arg1.equalsIgnoreCase("annotation")) {
			gender = EntityGenderUtils.byEntityType(LeafType.ANNOTATION);
		} else if (arg1.startsWith("<<")) {
			gender = EntityGenderUtils.byStereotype(arg1);
		} else {
			final IEntity entity = diagram.getOrCreateLeaf(diagram.buildLeafIdent(arg1), diagram.buildCode(arg1), null,
					null);
			gender = EntityGenderUtils.byEntityAlone(entity);
		}

		diagram.hideOrShow(gender, portion, arg.get("COMMAND", 0).equalsIgnoreCase("show"));
		return CommandExecutionResult.ok();
	}

	private CommandExecutionResult executeClassDiagram(AbstractClassOrObjectDiagram diagram, RegexResult arg) {

		final EntityPortion portion = getEntityPortion(arg.get("PORTION", 0));

		EntityGender gender = null;
		final String arg1 = arg.get("GENDER", 0);
		if (arg1 == null) {
			gender = EntityGenderUtils.all();
		} else if (arg1.equalsIgnoreCase("class")) {
			gender = EntityGenderUtils.byEntityType(LeafType.CLASS);
		} else if (arg1.equalsIgnoreCase("object")) {
			gender = EntityGenderUtils.byEntityType(LeafType.OBJECT);
		} else if (arg1.equalsIgnoreCase("interface")) {
			gender = EntityGenderUtils.byEntityType(LeafType.INTERFACE);
		} else if (arg1.equalsIgnoreCase("enum")) {
			gender = EntityGenderUtils.byEntityType(LeafType.ENUM);
		} else if (arg1.equalsIgnoreCase("abstract")) {
			gender = EntityGenderUtils.byEntityType(LeafType.ABSTRACT_CLASS);
		} else if (arg1.equalsIgnoreCase("annotation")) {
			gender = EntityGenderUtils.byEntityType(LeafType.ANNOTATION);
		} else if (arg1.startsWith("<<")) {
			gender = EntityGenderUtils.byStereotype(arg1);
		} else {
			final Ident ident = diagram.buildLeafIdent(arg1);
			final Code code = diagram.V1972() ? ident : diagram.buildCode(arg1);
			final IEntity entity = diagram.getOrCreateLeaf(ident, code, null, null);
			gender = EntityGenderUtils.byEntityAlone(entity);
		}
		if (gender != null) {
			final boolean empty = arg.get("EMPTY", 0) != null;
			final boolean emptyMembers = empty && portion == EntityPortion.MEMBER;
			if (empty == true && emptyMembers == false) {
				gender = EntityGenderUtils.and(gender, emptyByGender(portion));
			}
			if (EntityUtils.groupRoot(diagram.getCurrentGroup()) == false) {
				gender = EntityGenderUtils.and(gender, EntityGenderUtils.byPackage(diagram.getCurrentGroup()));
			}

			if (emptyMembers) {
				diagram.hideOrShow(EntityGenderUtils.and(gender, emptyByGender(EntityPortion.FIELD)),
						EntityPortion.FIELD, arg.get("COMMAND", 0).equalsIgnoreCase("show"));
				diagram.hideOrShow(EntityGenderUtils.and(gender, emptyByGender(EntityPortion.METHOD)),
						EntityPortion.METHOD, arg.get("COMMAND", 0).equalsIgnoreCase("show"));
			} else {
				diagram.hideOrShow(gender, portion, arg.get("COMMAND", 0).equalsIgnoreCase("show"));
			}
		}
		return CommandExecutionResult.ok();
	}

	private EntityPortion getEntityPortion(String s) {
		final String sub = StringUtils.goLowerCase(s.substring(0, 3));
		if (sub.equals("met")) {
			return EntityPortion.METHOD;
		}
		if (sub.equals("mem")) {
			return EntityPortion.MEMBER;
		}
		if (sub.equals("att") || sub.equals("fie")) {
			return EntityPortion.FIELD;
		}
		if (sub.equals("cir")) {
			return EntityPortion.CIRCLED_CHARACTER;
		}
		if (sub.equals("ste")) {
			return EntityPortion.STEREOTYPE;
		}
		throw new IllegalArgumentException();
	}

}
