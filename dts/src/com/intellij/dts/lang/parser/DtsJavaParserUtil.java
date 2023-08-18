package com.intellij.dts.lang.parser;

import com.intellij.dts.lang.DtsPpTokenTypes;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.dts.pp.lang.parser.PpBuildAdapter;
import com.intellij.dts.pp.lang.parser.PpParserUtilBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import java.util.Arrays;

public class DtsJavaParserUtil extends PpParserUtilBase {
    public static PsiBuilder adapt_builder_(IElementType root, PsiBuilder builder, PsiParser parser, TokenSet[] extendsSets) {
        final var state = new GeneratedParserUtilBase.ErrorState();
        ErrorState.initState(state, builder, root, extendsSets);

        return new PpBuildAdapter(
                builder,
                state,
                parser,
                DtsPpTokenTypes.INSTANCE,
                Arrays.asList(new DtsIncludeParser(), new DtsPpParser())
        );
    }
}
