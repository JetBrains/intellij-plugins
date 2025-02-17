// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.plugins.jade.JadeLanguage;
import com.jetbrains.plugins.jade.js.JSInJadeEmbeddedStatementWrapperImpl;
import com.jetbrains.plugins.jade.js.JSInJadeMixinParametersImpl;
import com.jetbrains.plugins.jade.js.JavaScriptInJadeLanguageDialect;
import com.jetbrains.plugins.jade.psi.stubs.JadeStubElementTypes;
import org.jetbrains.annotations.NotNull;

public interface JadeElementTypes extends JadeStubElementTypes {

  IElementType DOCTYPE = new IElementType("DOCTYPE", JadeLanguage.INSTANCE);

  IElementType DOCUMENT = new IElementType("DOCUMENT", JadeLanguage.INSTANCE);

  IElementType TAG = new IElementType("TAG", JadeLanguage.INSTANCE);

  IElementType TAG_INTERP_NAME = new IElementType("TAG_INTERP_NAME", JadeLanguage.INSTANCE);

  IElementType MIXIN = new IElementType("MIXIN", JadeLanguage.INSTANCE);

  IElementType COMMENT = new IElementType("COMMENT", JadeLanguage.INSTANCE);

  IElementType ATTRIBUTE = new IElementType("ATTRIBUTE", JadeLanguage.INSTANCE);

  IElementType ATTRIBUTE_VALUE = new IElementType("ATTRIBUTE_VALUE", JadeLanguage.INSTANCE);

  IElementType FAKE_ATTR_NAME = new IElementType("FAKE_ATTR_NAME", JadeLanguage.INSTANCE);

  IElementType JS_INTERPOLATION = new IElementType("JS_INTERPOLATION", JadeLanguage.INSTANCE);

  IElementType JS_EXPR = new IElementType("JS_EXPR", JadeLanguage.INSTANCE);

  IElementType JS_CODE_BLOCK = new IElementType("JS_CODE_BLOCK", JadeLanguage.INSTANCE);

  IElementType JS_CODE_BLOCK_PATCHED = new IElementType("JS_CODE_BLOCK_PATCHED", JadeLanguage.INSTANCE);

  IElementType JS_CODE_LINE = new IElementType("JS_CODE_LINE", JadeLanguage.INSTANCE);

  IElementType JS_STATEMENT = new IElementType("JS_STATEMENT", JadeLanguage.INSTANCE);

  IElementType PIPED_TEXT = new IElementType("PIPED_TEXT", JadeLanguage.INSTANCE);

  IElementType FILE_PATH = new IElementType("FILE_PATH", JadeLanguage.INSTANCE);

  IElementType CLASS = new IElementType("CLASS", JadeLanguage.INSTANCE);

  IElementType FILTER = new IElementType("FILTER", JadeLanguage.INSTANCE);

  IElementType CASE_STATEMENT = new IElementType("CASE_STATEMENT", JadeLanguage.INSTANCE);

  IElementType FOR_STATEMENT = new IElementType("FOR_STATEMENT", JadeLanguage.INSTANCE);

  IElementType FOR_BODY = new IElementType("FOR_BODY", JadeLanguage.INSTANCE);

  IElementType FOR_ELSE = new IElementType("FOR_ELSE", JadeLanguage.INSTANCE);

  IElementType WHEN_STATEMENT = new IElementType("WHEN_STATEMENT", JadeLanguage.INSTANCE);

  IElementType INCLUDE_STATEMENT = new IElementType("INCLUDE_STATEMENT", JadeLanguage.INSTANCE);

  IElementType YIELD_STATEMENT = new IElementType("YIELD_STATEMENT", JadeLanguage.INSTANCE);

  IElementType BLOCK = new IElementType("BLOCK", JadeLanguage.INSTANCE);

  IElementType CONDITIONAL_STATEMENT = new IElementType("CONDITIONAL_STATEMENT", JadeLanguage.INSTANCE);

  IElementType CONDITIONAL_HEADER = new IElementType("CONDITIONAL_HEADER", JadeLanguage.INSTANCE);

  IElementType CONDITIONAL_BODY = new IElementType("CONDITIONAL_BODY", JadeLanguage.INSTANCE);

  IElementType CONDITIONAL_ELSE = new IElementType("CONDITIONAL_ELSE", JadeLanguage.INSTANCE);

  IElementType JADE_PSEUDO_WHITESPACE = new IElementType("JADE_PSEUDO_WHITESPACE", JadeLanguage.INSTANCE);

  // JSInJade
  IElementType MIXIN_PARAMETERS = new JSInJadeElementType("MIXIN_PARAMETERS") {
    @Override
    public @NotNull ASTNode createCompositeNode() {
      return new JSInJadeMixinParametersImpl(this);
    }
  };

  IElementType MIXIN_PARAMETERS_VALUES = new IElementType("MIXIN_PARAMETERS_VALUES", JavaScriptInJadeLanguageDialect.INSTANCE);

  IElementType EMBEDDED_STATEMENT_WRAPPER = new JSInJadeElementType("EMBEDDED_WRAPPER") {
    @Override
    public @NotNull ASTNode createCompositeNode() {
      return new JSInJadeEmbeddedStatementWrapperImpl(this);
    }
  };

  abstract class JSInJadeElementType extends IElementType implements ICompositeElementType {

    public JSInJadeElementType(@NotNull String debugName) {
      super(debugName, JavaScriptInJadeLanguageDialect.INSTANCE);
    }
  }
}
