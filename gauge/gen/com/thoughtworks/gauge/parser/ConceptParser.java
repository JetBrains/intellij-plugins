// This is a generated file. Not intended for manual editing.
package com.thoughtworks.gauge.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import static com.thoughtworks.gauge.language.token.ConceptTokenTypes.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class ConceptParser implements PsiParser, LightPsiParser {

    public ASTNode parse(IElementType t, PsiBuilder b) {
        parseLight(t, b);
        return b.getTreeBuilt();
    }

    public void parseLight(IElementType t, PsiBuilder b) {
        boolean r;
        b = adapt_builder_(t, b, this, null);
        Marker m = enter_section_(b, 0, _COLLAPSE_, null);
        if (t == ARG) {
            r = arg(b, 0);
        } else if (t == CONCEPT) {
            r = concept(b, 0);
        } else if (t == CONCEPT_HEADING) {
            r = conceptHeading(b, 0);
        } else if (t == DYNAMIC_ARG) {
            r = dynamicArg(b, 0);
        } else if (t == STATIC_ARG) {
            r = staticArg(b, 0);
        } else if (t == STEP) {
            r = step(b, 0);
        } else if (t == TABLE) {
            r = table(b, 0);
        } else if (t == TABLE_BODY) {
            r = tableBody(b, 0);
        } else if (t == TABLE_HEADER) {
            r = tableHeader(b, 0);
        } else if (t == TABLE_ROW_VALUE) {
            r = tableRowValue(b, 0);
        } else {
            r = parse_root_(t, b, 0);
        }
        exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
    }

    protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
        return conceptFile(b, l + 1);
    }

    /* ********************************************************** */
    // dynamicArg | staticArg
    public static boolean arg(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "arg")) return false;
        if (!nextTokenIs(b, "<arg>", ARG_START, DYNAMIC_ARG_START)) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, ARG, "<arg>");
        r = dynamicArg(b, l + 1);
        if (!r) r = staticArg(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // COMMENT
    static boolean comment(PsiBuilder b, int l) {
        return consumeToken(b, COMMENT);
    }

    /* ********************************************************** */
    // conceptHeading NEW_LINE* (step NEW_LINE* | comment) +
    public static boolean concept(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "concept")) return false;
        if (!nextTokenIs(b, "<concept>", CONCEPT_HEADING, CONCEPT_HEADING_IDENTIFIER)) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, CONCEPT, "<concept>");
        r = conceptHeading(b, l + 1);
        r = r && concept_1(b, l + 1);
        r = r && concept_2(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // NEW_LINE*
    private static boolean concept_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "concept_1")) return false;
        int c = current_position_(b);
        while (true) {
            if (!consumeToken(b, NEW_LINE)) break;
            if (!empty_element_parsed_guard_(b, "concept_1", c)) break;
            c = current_position_(b);
        }
        return true;
    }

    // (step NEW_LINE* | comment) +
    private static boolean concept_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "concept_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = concept_2_0(b, l + 1);
        int c = current_position_(b);
        while (r) {
            if (!concept_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "concept_2", c)) break;
            c = current_position_(b);
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // step NEW_LINE* | comment
    private static boolean concept_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "concept_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = concept_2_0_0(b, l + 1);
        if (!r) r = comment(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // step NEW_LINE*
    private static boolean concept_2_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "concept_2_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = step(b, l + 1);
        r = r && concept_2_0_0_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // NEW_LINE*
    private static boolean concept_2_0_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "concept_2_0_0_1")) return false;
        int c = current_position_(b);
        while (true) {
            if (!consumeToken(b, NEW_LINE)) break;
            if (!empty_element_parsed_guard_(b, "concept_2_0_0_1", c)) break;
            c = current_position_(b);
        }
        return true;
    }

    /* ********************************************************** */
    // CONCEPT_COMMENT
    static boolean conceptComment(PsiBuilder b, int l) {
        return consumeToken(b, CONCEPT_COMMENT);
    }

    /* ********************************************************** */
    // (comment)* concept+
    static boolean conceptFile(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "conceptFile")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = conceptFile_0(b, l + 1);
        r = r && conceptFile_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (comment)*
    private static boolean conceptFile_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "conceptFile_0")) return false;
        int c = current_position_(b);
        while (true) {
            if (!conceptFile_0_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "conceptFile_0", c)) break;
            c = current_position_(b);
        }
        return true;
    }

    // (comment)
    private static boolean conceptFile_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "conceptFile_0_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = comment(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // concept+
    private static boolean conceptFile_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "conceptFile_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = concept(b, l + 1);
        int c = current_position_(b);
        while (r) {
            if (!concept(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "conceptFile_1", c)) break;
            c = current_position_(b);
        }
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // (CONCEPT_HEADING_IDENTIFIER (CONCEPT_HEADING|dynamicArg)* NEW_LINE) | (CONCEPT_HEADING)
    public static boolean conceptHeading(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "conceptHeading")) return false;
        if (!nextTokenIs(b, "<concept heading>", CONCEPT_HEADING, CONCEPT_HEADING_IDENTIFIER)) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, CONCEPT_HEADING, "<concept heading>");
        r = conceptHeading_0(b, l + 1);
        if (!r) r = consumeToken(b, CONCEPT_HEADING);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // CONCEPT_HEADING_IDENTIFIER (CONCEPT_HEADING|dynamicArg)* NEW_LINE
    private static boolean conceptHeading_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "conceptHeading_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, CONCEPT_HEADING_IDENTIFIER);
        r = r && conceptHeading_0_1(b, l + 1);
        r = r && consumeToken(b, NEW_LINE);
        exit_section_(b, m, null, r);
        return r;
    }

    // (CONCEPT_HEADING|dynamicArg)*
    private static boolean conceptHeading_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "conceptHeading_0_1")) return false;
        int c = current_position_(b);
        while (true) {
            if (!conceptHeading_0_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "conceptHeading_0_1", c)) break;
            c = current_position_(b);
        }
        return true;
    }

    // CONCEPT_HEADING|dynamicArg
    private static boolean conceptHeading_0_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "conceptHeading_0_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, CONCEPT_HEADING);
        if (!r) r = dynamicArg(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // DYNAMIC_ARG_START DYNAMIC_ARG DYNAMIC_ARG_END
    public static boolean dynamicArg(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "dynamicArg")) return false;
        if (!nextTokenIs(b, DYNAMIC_ARG_START)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeTokens(b, 0, DYNAMIC_ARG_START, DYNAMIC_ARG, DYNAMIC_ARG_END);
        exit_section_(b, m, DYNAMIC_ARG, r);
        return r;
    }

    /* ********************************************************** */
    // ARG_START ARG? ARG_END
    public static boolean staticArg(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "staticArg")) return false;
        if (!nextTokenIs(b, ARG_START)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, ARG_START);
        r = r && staticArg_1(b, l + 1);
        r = r && consumeToken(b, ARG_END);
        exit_section_(b, m, STATIC_ARG, r);
        return r;
    }

    // ARG?
    private static boolean staticArg_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "staticArg_1")) return false;
        consumeToken(b, ARG);
        return true;
    }

    /* ********************************************************** */
    // STEP_IDENTIFIER (arg|STEP)+ (comment | NEW_LINE)* table?
    public static boolean step(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "step")) return false;
        if (!nextTokenIs(b, STEP_IDENTIFIER)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, STEP_IDENTIFIER);
        r = r && step_1(b, l + 1);
        r = r && step_2(b, l + 1);
        r = r && step_3(b, l + 1);
        exit_section_(b, m, STEP, r);
        return r;
    }

    // (arg|STEP)+
    private static boolean step_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "step_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = step_1_0(b, l + 1);
        int c = current_position_(b);
        while (r) {
            if (!step_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "step_1", c)) break;
            c = current_position_(b);
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // arg|STEP
    private static boolean step_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "step_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = arg(b, l + 1);
        if (!r) r = consumeToken(b, STEP);
        exit_section_(b, m, null, r);
        return r;
    }

    // (comment | NEW_LINE)*
    private static boolean step_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "step_2")) return false;
        int c = current_position_(b);
        while (true) {
            if (!step_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "step_2", c)) break;
            c = current_position_(b);
        }
        return true;
    }

    // comment | NEW_LINE
    private static boolean step_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "step_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = comment(b, l + 1);
        if (!r) r = consumeToken(b, NEW_LINE);
        exit_section_(b, m, null, r);
        return r;
    }

    // table?
    private static boolean step_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "step_3")) return false;
        table(b, l + 1);
        return true;
    }

    /* ********************************************************** */
    // tableHeader tableBody
    public static boolean table(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "table")) return false;
        if (!nextTokenIs(b, TABLE_BORDER)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = tableHeader(b, l + 1);
        r = r && tableBody(b, l + 1);
        exit_section_(b, m, TABLE, r);
        return r;
    }

    /* ********************************************************** */
    // (TABLE_BORDER (WHITESPACE* tableRowValue? WHITESPACE* TABLE_BORDER)+ NEW_LINE?)*
    public static boolean tableBody(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableBody")) return false;
        Marker m = enter_section_(b, l, _NONE_, TABLE_BODY, "<table body>");
        int c = current_position_(b);
        while (true) {
            if (!tableBody_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "tableBody", c)) break;
            c = current_position_(b);
        }
        exit_section_(b, l, m, true, false, null);
        return true;
    }

    // TABLE_BORDER (WHITESPACE* tableRowValue? WHITESPACE* TABLE_BORDER)+ NEW_LINE?
    private static boolean tableBody_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableBody_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, TABLE_BORDER);
        r = r && tableBody_0_1(b, l + 1);
        r = r && tableBody_0_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (WHITESPACE* tableRowValue? WHITESPACE* TABLE_BORDER)+
    private static boolean tableBody_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableBody_0_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = tableBody_0_1_0(b, l + 1);
        int c = current_position_(b);
        while (r) {
            if (!tableBody_0_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "tableBody_0_1", c)) break;
            c = current_position_(b);
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // WHITESPACE* tableRowValue? WHITESPACE* TABLE_BORDER
    private static boolean tableBody_0_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableBody_0_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = tableBody_0_1_0_0(b, l + 1);
        r = r && tableBody_0_1_0_1(b, l + 1);
        r = r && tableBody_0_1_0_2(b, l + 1);
        r = r && consumeToken(b, TABLE_BORDER);
        exit_section_(b, m, null, r);
        return r;
    }

    // WHITESPACE*
    private static boolean tableBody_0_1_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableBody_0_1_0_0")) return false;
        int c = current_position_(b);
        while (true) {
            if (!consumeToken(b, WHITESPACE)) break;
            if (!empty_element_parsed_guard_(b, "tableBody_0_1_0_0", c)) break;
            c = current_position_(b);
        }
        return true;
    }

    // tableRowValue?
    private static boolean tableBody_0_1_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableBody_0_1_0_1")) return false;
        tableRowValue(b, l + 1);
        return true;
    }

    // WHITESPACE*
    private static boolean tableBody_0_1_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableBody_0_1_0_2")) return false;
        int c = current_position_(b);
        while (true) {
            if (!consumeToken(b, WHITESPACE)) break;
            if (!empty_element_parsed_guard_(b, "tableBody_0_1_0_2", c)) break;
            c = current_position_(b);
        }
        return true;
    }

    // NEW_LINE?
    private static boolean tableBody_0_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableBody_0_2")) return false;
        consumeToken(b, NEW_LINE);
        return true;
    }

    /* ********************************************************** */
    // TABLE_BORDER (TABLE_HEADER TABLE_BORDER)+ NEW_LINE ((TABLE_BORDER)* NEW_LINE)?
    public static boolean tableHeader(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableHeader")) return false;
        if (!nextTokenIs(b, TABLE_BORDER)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, TABLE_BORDER);
        r = r && tableHeader_1(b, l + 1);
        r = r && consumeToken(b, NEW_LINE);
        r = r && tableHeader_3(b, l + 1);
        exit_section_(b, m, TABLE_HEADER, r);
        return r;
    }

    // (TABLE_HEADER TABLE_BORDER)+
    private static boolean tableHeader_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableHeader_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = tableHeader_1_0(b, l + 1);
        int c = current_position_(b);
        while (r) {
            if (!tableHeader_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "tableHeader_1", c)) break;
            c = current_position_(b);
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // TABLE_HEADER TABLE_BORDER
    private static boolean tableHeader_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableHeader_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeTokens(b, 0, TABLE_HEADER, TABLE_BORDER);
        exit_section_(b, m, null, r);
        return r;
    }

    // ((TABLE_BORDER)* NEW_LINE)?
    private static boolean tableHeader_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableHeader_3")) return false;
        tableHeader_3_0(b, l + 1);
        return true;
    }

    // (TABLE_BORDER)* NEW_LINE
    private static boolean tableHeader_3_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableHeader_3_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = tableHeader_3_0_0(b, l + 1);
        r = r && consumeToken(b, NEW_LINE);
        exit_section_(b, m, null, r);
        return r;
    }

    // (TABLE_BORDER)*
    private static boolean tableHeader_3_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableHeader_3_0_0")) return false;
        int c = current_position_(b);
        while (true) {
            if (!consumeToken(b, TABLE_BORDER)) break;
            if (!empty_element_parsed_guard_(b, "tableHeader_3_0_0", c)) break;
            c = current_position_(b);
        }
        return true;
    }

    /* ********************************************************** */
    // TABLE_ROW_VALUE+ | (DYNAMIC_ARG_START DYNAMIC_ARG+ DYNAMIC_ARG_END WHITESPACE* TABLE_ROW_VALUE* WHITESPACE*)+
    public static boolean tableRowValue(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableRowValue")) return false;
        if (!nextTokenIs(b, "<table row value>", DYNAMIC_ARG_START, TABLE_ROW_VALUE)) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, TABLE_ROW_VALUE, "<table row value>");
        r = tableRowValue_0(b, l + 1);
        if (!r) r = tableRowValue_1(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // TABLE_ROW_VALUE+
    private static boolean tableRowValue_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableRowValue_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, TABLE_ROW_VALUE);
        int c = current_position_(b);
        while (r) {
            if (!consumeToken(b, TABLE_ROW_VALUE)) break;
            if (!empty_element_parsed_guard_(b, "tableRowValue_0", c)) break;
            c = current_position_(b);
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // (DYNAMIC_ARG_START DYNAMIC_ARG+ DYNAMIC_ARG_END WHITESPACE* TABLE_ROW_VALUE* WHITESPACE*)+
    private static boolean tableRowValue_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableRowValue_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = tableRowValue_1_0(b, l + 1);
        int c = current_position_(b);
        while (r) {
            if (!tableRowValue_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "tableRowValue_1", c)) break;
            c = current_position_(b);
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // DYNAMIC_ARG_START DYNAMIC_ARG+ DYNAMIC_ARG_END WHITESPACE* TABLE_ROW_VALUE* WHITESPACE*
    private static boolean tableRowValue_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableRowValue_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, DYNAMIC_ARG_START);
        r = r && tableRowValue_1_0_1(b, l + 1);
        r = r && consumeToken(b, DYNAMIC_ARG_END);
        r = r && tableRowValue_1_0_3(b, l + 1);
        r = r && tableRowValue_1_0_4(b, l + 1);
        r = r && tableRowValue_1_0_5(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // DYNAMIC_ARG+
    private static boolean tableRowValue_1_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableRowValue_1_0_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, DYNAMIC_ARG);
        int c = current_position_(b);
        while (r) {
            if (!consumeToken(b, DYNAMIC_ARG)) break;
            if (!empty_element_parsed_guard_(b, "tableRowValue_1_0_1", c)) break;
            c = current_position_(b);
        }
        exit_section_(b, m, null, r);
        return r;
    }

    // WHITESPACE*
    private static boolean tableRowValue_1_0_3(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableRowValue_1_0_3")) return false;
        int c = current_position_(b);
        while (true) {
            if (!consumeToken(b, WHITESPACE)) break;
            if (!empty_element_parsed_guard_(b, "tableRowValue_1_0_3", c)) break;
            c = current_position_(b);
        }
        return true;
    }

    // TABLE_ROW_VALUE*
    private static boolean tableRowValue_1_0_4(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableRowValue_1_0_4")) return false;
        int c = current_position_(b);
        while (true) {
            if (!consumeToken(b, TABLE_ROW_VALUE)) break;
            if (!empty_element_parsed_guard_(b, "tableRowValue_1_0_4", c)) break;
            c = current_position_(b);
        }
        return true;
    }

    // WHITESPACE*
    private static boolean tableRowValue_1_0_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "tableRowValue_1_0_5")) return false;
        int c = current_position_(b);
        while (true) {
            if (!consumeToken(b, WHITESPACE)) break;
            if (!empty_element_parsed_guard_(b, "tableRowValue_1_0_5", c)) break;
            c = current_position_(b);
        }
        return true;
    }

}
