package com.intellij.plugins.drools.lang.lexer;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.java.parser.JavaParser;
import com.intellij.lang.java.parser.JavaParserUtil;
import com.intellij.plugins.drools.DroolsLanguage;
import com.intellij.psi.tree.ILazyParseableElementType;
import org.jetbrains.annotations.NotNull;

public class DroolsJavaStatementLazyParseableElementType extends ILazyParseableElementType {
  public DroolsJavaStatementLazyParseableElementType() {
    super("JAVA_STATEMENT", DroolsLanguage.INSTANCE);
  }

  @Override
  public ASTNode parseContents(@NotNull ASTNode chameleon) {
    PsiBuilder builder = JavaParserUtil.createBuilder(chameleon);
    PsiBuilder.Marker root = builder.mark();
    JavaParser.INSTANCE.getStatementParser().parseStatements(builder);
    while (!builder.eof()) builder.advanceLexer();
    root.done(this);
    return builder.getTreeBuilt().getFirstChildNode();
   }
}
