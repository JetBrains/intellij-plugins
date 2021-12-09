/*----------------------------------------------------------------
 *  Copyright (c) ThoughtWorks, Inc.
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE.txt in the project root for license information.
 *----------------------------------------------------------------*/

package com.thoughtworks.gauge.language.token;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.thoughtworks.gauge.language.psi.impl.*;

public interface ConceptTokenTypes {

    IElementType CONCEPT = new ConceptElementType("CONCEPT");
    IElementType STATIC_ARG = new ConceptElementType("STATIC_ARG");
    IElementType TABLE = new ConceptElementType("TABLE");
    IElementType TABLE_BODY = new ConceptElementType("TABLE_BODY");

    IElementType ARG = new ConceptTokenType("ARG");
    IElementType ARG_END = new ConceptTokenType("ARG_END");
    IElementType ARG_START = new ConceptTokenType("ARG_START");
    IElementType COMMENT = new ConceptTokenType("COMMENT");
    IElementType CONCEPT_COMMENT = new ConceptTokenType("CONCEPT_COMMENT");
    IElementType CONCEPT_HEADING = new ConceptTokenType("CONCEPT_HEADING");
    IElementType CONCEPT_HEADING_IDENTIFIER = new ConceptTokenType("CONCEPT_HEADING_IDENTIFIER");
    IElementType DYNAMIC_ARG = new ConceptTokenType("DYNAMIC_ARG");
    IElementType DYNAMIC_ARG_END = new ConceptTokenType("DYNAMIC_ARG_END");
    IElementType DYNAMIC_ARG_START = new ConceptTokenType("DYNAMIC_ARG_START");
    IElementType NEW_LINE = new ConceptTokenType("NEW_LINE");
    IElementType STEP = new ConceptTokenType("STEP");
    IElementType STEP_IDENTIFIER = new ConceptTokenType("STEP_IDENTIFIER");
    IElementType TABLE_BORDER = new ConceptTokenType("TABLE_BORDER");
    IElementType TABLE_HEADER = new ConceptTokenType("TABLE_HEADER");
    IElementType TABLE_ROW_VALUE = new ConceptTokenType("TABLE_ROW_VALUE");
    IElementType WHITESPACE = new ConceptTokenType("WHITESPACE");

    class Factory {
        public static PsiElement createElement(ASTNode node) {
            IElementType type = node.getElementType();
            if (type == ARG) {
                return new ConceptArgImpl(node);
            } else if (type == CONCEPT) {
                return new ConceptConceptImpl(node);
            } else if (type == CONCEPT_HEADING) {
                return new ConceptConceptHeadingImpl(node);
            } else if (type == DYNAMIC_ARG) {
                return new ConceptDynamicArgImpl(node);
            } else if (type == STATIC_ARG) {
                return new ConceptStaticArgImpl(node);
            } else if (type == STEP) {
                return new ConceptStepImpl(node);
            } else if (type == TABLE) {
                return new ConceptTableImpl(node);
            } else if (type == TABLE_BODY) {
                return new ConceptTableBodyImpl(node);
            } else if (type == TABLE_HEADER) {
                return new ConceptTableHeaderImpl(node);
            } else if (type == TABLE_ROW_VALUE) {
                return new ConceptTableRowValueImpl(node);
            }
            throw new AssertionError("Unknown element type: " + type);
        }
    }
}
