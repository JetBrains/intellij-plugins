package com.intellij.dts.pp.test.impl;

import com.intellij.dts.pp.lang.parser.PpBuildAdapter;
import com.intellij.dts.pp.lang.parser.PpParserUtilBase;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import java.util.Collections;

public class TestParserUtil extends PpParserUtilBase {
    public static PsiBuilder adapt_builder_(IElementType root, PsiBuilder builder, PsiParser parser, TokenSet[] extendsSets) {
      final var state = new ErrorState();
      ErrorState.initState(state, builder, root, extendsSets);

      return new PpBuildAdapter(
        builder,
        state,
        parser,
        TestPpTokenTypes.INSTANCE,
        Collections.emptyList()
      );
    }
}
