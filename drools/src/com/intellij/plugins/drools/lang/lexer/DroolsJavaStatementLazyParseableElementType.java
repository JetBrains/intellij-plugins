// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.lexer;

import com.intellij.java.syntax.parser.JavaParser;
import com.intellij.lang.ASTNode;
import com.intellij.lang.java.parser.JavaParserUtil;
import com.intellij.lang.java.parser.PsiSyntaxBuilderWithLanguageLevel;
import com.intellij.platform.syntax.SyntaxElementType;
import com.intellij.platform.syntax.SyntaxElementTypeKt;
import com.intellij.platform.syntax.parser.SyntaxTreeBuilder;
import com.intellij.platform.syntax.psi.ElementTypeConverter;
import com.intellij.platform.syntax.psi.ElementTypeConverterKt;
import com.intellij.platform.syntax.psi.ParsingDiagnostics;
import com.intellij.platform.syntax.psi.PsiSyntaxBuilder;
import com.intellij.plugins.drools.DroolsLanguage;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.tree.ILazyParseableElementType;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

public class DroolsJavaStatementLazyParseableElementType extends ILazyParseableElementType {
  private static final SyntaxElementType JAVA_STATEMENT_SYNTAX = SyntaxElementTypeKt.SyntaxElementType("DROOLS_JAVA_STATEMENT");

  private final ElementTypeConverter converter;

  public DroolsJavaStatementLazyParseableElementType() {
    super("JAVA_STATEMENT", DroolsLanguage.INSTANCE);
    converter = ElementTypeConverterKt.elementTypeConverterOf(
      new Pair<>(JAVA_STATEMENT_SYNTAX, this)
    );
  }

  @Override
  public ASTNode parseContents(@NotNull ASTNode chameleon) {
    PsiSyntaxBuilderWithLanguageLevel builderAndLevel = JavaParserUtil.createSyntaxBuilder(chameleon, converter);
    PsiSyntaxBuilder psiSyntaxBuilder = builderAndLevel.getBuilder();
    LanguageLevel level = builderAndLevel.getLanguageLevel();
    long startTime = System.nanoTime();
    SyntaxTreeBuilder builder = psiSyntaxBuilder.getSyntaxTreeBuilder();
    SyntaxTreeBuilder.Marker root = builder.mark();
    new JavaParser(level).getStatementParser().parseStatements(builder);
    while (!builder.eof()) builder.advanceLexer();
    root.done(JAVA_STATEMENT_SYNTAX);
    ASTNode node = psiSyntaxBuilder.getTreeBuilt().getFirstChildNode();
    ParsingDiagnostics.registerParse(builder, getLanguage(), System.nanoTime() - startTime);
    return node;
  }
}
