package com.intellij.dts.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public class DtsJavaParserUtil extends GeneratedParserUtilBase {
    public static PsiBuilder adapt_builder_(IElementType root, PsiBuilder builder, PsiParser parser, TokenSet[] extendsSets) {
        ErrorState state = new ErrorState();
        ErrorState.initState(state, builder, root, extendsSets);

        return new DtsBuildAdapter(builder, state, parser);
    }

    public static void exit_section_(PsiBuilder builder, int level, PsiBuilder.Marker marker, boolean result, boolean pinned, Parser eatMore) {
        exit_section_(builder, level, marker, null, result, pinned, eatMore);
    }

    public static void exit_section_(PsiBuilder builder, int level, PsiBuilder.Marker marker, IElementType elementType, boolean result, boolean pinned, Parser eatMore) {
        if (!result && pinned && DtsParserUtil.INSTANCE.couldContainPreprocessorStatement(builder, marker)) {
            DtsParserUtil.INSTANCE.rollbackPreprocessorStatements(builder, marker);
        }

        GeneratedParserUtilBase.exit_section_(builder, level, marker, elementType, result, pinned, eatMore);
    }
}
