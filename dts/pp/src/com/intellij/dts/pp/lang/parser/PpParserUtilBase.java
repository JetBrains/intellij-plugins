package com.intellij.dts.pp.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;

public class PpParserUtilBase extends GeneratedParserUtilBase {
    public static void exit_section_(PsiBuilder builder, int level, PsiBuilder.Marker marker, boolean result, boolean pinned, Parser eatMore) {
        exit_section_(builder, level, marker, null, result, pinned, eatMore);
    }

    public static void exit_section_(PsiBuilder builder, int level, PsiBuilder.Marker marker, IElementType elementType, boolean result, boolean pinned, Parser eatMore) {
        if (!result && pinned && builder instanceof PpBuildAdapter ppBuilder) {
            PpParserUtil.INSTANCE.rollbackPreprocessorStatements(ppBuilder, marker);
        }

        GeneratedParserUtilBase.exit_section_(builder, level, marker, elementType, result, pinned, eatMore);
    }
}
