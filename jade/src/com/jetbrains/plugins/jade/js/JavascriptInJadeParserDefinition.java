// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.js;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lang.javascript.parsing.JavaScriptParser;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IFileElementType;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;

public final class JavascriptInJadeParserDefinition extends JavascriptParserDefinition {
  static final IFileElementType JS_IN_JADE_FILE = JSFileElementType.create(JavaScriptInJadeLanguageDialect.INSTANCE);

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return JS_IN_JADE_FILE;
  }

  @Override
  public @NotNull JavaScriptParser createJSParser(@NotNull PsiBuilder builder) {
    return new JavaScriptInJadeParser(builder);
  }

  @Override
  public @NotNull Lexer createLexer(Project project) {
    return new JavaScriptInJadeLexer();
  }

  @Override
  public @NotNull PsiElement createElement(@NotNull ASTNode node) {
    if (node.getElementType() == JadeTokenTypes.JS_CODE_BLOCK_PATCHED
        || node.getElementType() == JadeTokenTypes.JS_CODE_BLOCK
        || node.getElementType() == JadeTokenTypes.JS_META_CODE) {
      return new JSInJadeEmbeddedContentImpl(node);
    }
    if (node.getElementType() == JadeTokenTypes.JS_EXPR) {
      return new JSInJadeEmbeddedContentImpl(node);
    }
    if (node.getElementType() == JadeTokenTypes.JS_MIXIN_PARAMS) {
      return new JSInJadeEmbeddedContentImpl(node);
    }
    if (node.getElementType() == JadeTokenTypes.JS_MIXIN_PARAMS_VALUES) {
      return new JSInJadeEmbeddedContentImpl(node);
    }
    if (node.getElementType() == JadeTokenTypes.JS_EACH_EXPR) {
      return new JSInJadeEmbeddedContentImpl(node);
    }

    return super.createElement(node);
  }
}
