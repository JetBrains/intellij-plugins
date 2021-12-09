// This is a generated file. Not intended for manual editing.
package com.thoughtworks.gauge.language.token;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.thoughtworks.gauge.language.psi.impl.*;

public interface SpecTokenTypes {

    IElementType SCENARIO = new SpecElementType("SCENARIO");
    IElementType SPEC_DETAIL = new SpecElementType("SPEC_DETAIL");
    IElementType STATIC_ARG = new SpecElementType("STATIC_ARG");
    IElementType TABLE = new SpecElementType("TABLE");
    IElementType TABLE_BODY = new SpecElementType("TABLE_BODY");
    IElementType TEARDOWN = new SpecElementType("TEARDOWN");

    IElementType ARG = new SpecTokenType("ARG");
    IElementType ARG_END = new SpecTokenType("ARG_END");
    IElementType ARG_START = new SpecTokenType("ARG_START");
    IElementType COMMENT = new SpecTokenType("COMMENT");
    IElementType DYNAMIC_ARG = new SpecTokenType("DYNAMIC_ARG");
    IElementType DYNAMIC_ARG_END = new SpecTokenType("DYNAMIC_ARG_END");
    IElementType DYNAMIC_ARG_START = new SpecTokenType("DYNAMIC_ARG_START");
    IElementType KEYWORD = new SpecTokenType("KEYWORD");
    IElementType NEW_LINE = new SpecTokenType("NEW_LINE");
    IElementType SCENARIO_HEADING = new SpecTokenType("SCENARIO_HEADING");
    IElementType SPEC_COMMENT = new SpecTokenType("SPEC_COMMENT");
    IElementType SPEC_HEADING = new SpecTokenType("SPEC_HEADING");
    IElementType STEP = new SpecTokenType("STEP");
    IElementType STEP_IDENTIFIER = new SpecTokenType("STEP_IDENTIFIER");
    IElementType TABLE_BORDER = new SpecTokenType("TABLE_BORDER");
    IElementType TABLE_HEADER = new SpecTokenType("TABLE_HEADER");
    IElementType TABLE_ROW_VALUE = new SpecTokenType("TABLE_ROW_VALUE");
    IElementType TAGS = new SpecTokenType("TAGS");
    IElementType TEARDOWN_IDENTIFIER = new SpecTokenType("TEARDOWN_IDENTIFIER");
    IElementType WHITESPACE = new SpecTokenType("WHITESPACE");

    class Factory {
        public static PsiElement createElement(ASTNode node) {
            IElementType type = node.getElementType();
            if (type == ARG) {
                return new SpecArgImpl(node);
            } else if (type == DYNAMIC_ARG) {
                return new SpecDynamicArgImpl(node);
            } else if (type == KEYWORD) {
                return new SpecKeywordImpl(node);
            } else if (type == SCENARIO) {
                return new SpecScenarioImpl(node);
            } else if (type == SPEC_DETAIL) {
                return new SpecDetailImpl(node);
            } else if (type == STATIC_ARG) {
                return new SpecStaticArgImpl(node);
            } else if (type == STEP) {
                return new SpecStepImpl(node);
            } else if (type == TABLE) {
                return new SpecTableImpl(node);
            } else if (type == TABLE_BODY) {
                return new SpecTableBodyImpl(node);
            } else if (type == TABLE_HEADER) {
                return new SpecTableHeaderImpl(node);
            } else if (type == TABLE_ROW_VALUE) {
                return new SpecTableRowValueImpl(node);
            } else if (type == TAGS) {
                return new SpecTagsImpl(node);
            } else if (type == TEARDOWN) {
                return new SpecTeardownImpl(node);
            }
            throw new AssertionError("Unknown element type: " + type);
        }
    }
}
