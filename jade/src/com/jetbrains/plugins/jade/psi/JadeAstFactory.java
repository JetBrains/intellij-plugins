// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi;

import com.intellij.lang.ASTFactory;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.plugins.jade.psi.impl.JadeAttributeImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeAttributeValueImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeBlockImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeCaseStatementImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeClassImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeClassNameImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeCommentImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeConditionalBodyImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeConditionalElseImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeConditionalHeaderImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeConditionalStatementImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeDoctypeImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeDocumentImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeFakeXmlNameElement;
import com.jetbrains.plugins.jade.psi.impl.JadeFilePathImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeFilterImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeForStatementImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeIncludeStatementImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeInterpolatedTagNameImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeJSCodeLineImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeJSStatementImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeJsCodeBlockImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeJsExpressionImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeJsInterpolationImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeMixinImpl;
import com.jetbrains.plugins.jade.psi.impl.JadePipedTextImpl;
import com.jetbrains.plugins.jade.psi.impl.JadePseudoWhitespaceImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeTagIdImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeTagImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeWhenStatementImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeYieldStatementImpl;
import com.jetbrains.plugins.jade.psi.stubs.JadeStubElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JadeAstFactory extends ASTFactory implements JadeTokenTypes {

  @Override
  public @Nullable LeafElement createLeaf(@NotNull IElementType type, @NotNull CharSequence text) {
    if (type == TAG_CLASS) {
      return new JadeClassNameImpl(type, text);
    }
    else if (type == TAG_ID) {
      return new JadeTagIdImpl(type, text);
    }

    return new LeafPsiElement(type, text);
  }

  @Override
  public CompositeElement createComposite(final @NotNull IElementType type) {
    if (type == JadeElementTypes.DOCUMENT) {
      return new JadeDocumentImpl();
    }
    if (type == JadeElementTypes.TAG) {
      return new JadeTagImpl();
    }
    if (type == JadeElementTypes.MIXIN) {
      return new JadeMixinImpl();
    }
    if (type == JadeElementTypes.DOCTYPE) {
      return new JadeDoctypeImpl();
    }
    if (type == JadeElementTypes.COMMENT) {
      return new JadeCommentImpl();
    }
    if (type == JadeElementTypes.ATTRIBUTE) {
      return new JadeAttributeImpl();
    }
    if (type == JadeElementTypes.ATTRIBUTE_VALUE) {
      return new JadeAttributeValueImpl();
    }
    if (type == JadeElementTypes.FAKE_ATTR_NAME) {
      return new JadeFakeXmlNameElement();
    }
    if (type == JadeElementTypes.JS_INTERPOLATION) {
      return new JadeJsInterpolationImpl();
    }
    if (type == JadeElementTypes.JS_EXPR) {
      return new JadeJsExpressionImpl();
    }
    if (type == JadeElementTypes.JS_CODE_BLOCK) {
      return new JadeJsCodeBlockImpl();
    }
    //if (type == JadeElementTypes.JS_CODE_BLOCK_PATCHED) {
    //  return new JadeJsCodeBlockPatchedImpl();
    //}
    //if (type == JadeElementTypes.JS_EACH_EXPR) {
    //  return new JadeJsEachExpressionImpl();
    //}
    if (type == JadeElementTypes.FILE_PATH) {
      return new JadeFilePathImpl();
    }
    if (type == JadeElementTypes.CLASS) {
      return new JadeClassImpl();
    }
    if (type == JadeElementTypes.TAG_INTERP_NAME) {
      return new JadeInterpolatedTagNameImpl();
    }
    if (type == JadeElementTypes.BLOCK) {
      return new JadeBlockImpl();
    }
    if (type == JadeElementTypes.JS_CODE_LINE) {
      return new JadeJSCodeLineImpl();
    }
    if (type == JadeElementTypes.JS_STATEMENT) {
      return new JadeJSStatementImpl();
    }
    if (type == JadeElementTypes.CASE_STATEMENT) {
      return new JadeCaseStatementImpl();
    }
    if (type == JadeElementTypes.WHEN_STATEMENT) {
      return new JadeWhenStatementImpl();
    }
    if (type == JadeElementTypes.INCLUDE_STATEMENT) {
      return new JadeIncludeStatementImpl();
    }
    if (type == JadeElementTypes.YIELD_STATEMENT) {
      return new JadeYieldStatementImpl();
    }
    if (type == JadeElementTypes.FILTER) {
      return new JadeFilterImpl();
    }
    if (type == JadeElementTypes.PIPED_TEXT) {
      return new JadePipedTextImpl();
    }
    if (type == JadeStubElementTypes.MIXIN_DECLARATION) {
      return new CompositeElement(type);
    }
    if (type == JadeElementTypes.CONDITIONAL_STATEMENT) {
      return new JadeConditionalStatementImpl();
    }
    if (type == JadeElementTypes.FOR_STATEMENT) {
      return new JadeForStatementImpl();
    }
    if (type == JadeElementTypes.CONDITIONAL_HEADER) {
      return new JadeConditionalHeaderImpl();
    }
    if (type == JadeElementTypes.CONDITIONAL_BODY
      || type == JadeElementTypes.FOR_BODY) {
      return new JadeConditionalBodyImpl(type);
    }
    if (type == JadeElementTypes.CONDITIONAL_ELSE
      || type == JadeElementTypes.FOR_ELSE) {
      return new JadeConditionalElseImpl(type);
    }
    if (type == JadeElementTypes.JADE_PSEUDO_WHITESPACE) {
      return new JadePseudoWhitespaceImpl();
    }

    return super.createComposite(type);
  }
}
