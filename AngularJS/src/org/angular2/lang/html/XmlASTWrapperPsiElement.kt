// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSEmbeddedContent;
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService;
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlElement;
import com.intellij.xml.util.XmlPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XmlASTWrapperPsiElement extends ASTWrapperPsiElement implements XmlElement, JSEmbeddedContent {
  public XmlASTWrapperPsiElement(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public boolean processElements(PsiElementProcessor processor, PsiElement place) {
    return XmlPsiUtil.processXmlElements(this, processor, false);
  }

  @Override
  public void subtreeChanged() {
    super.subtreeChanged();
    JSControlFlowService.getService(getProject()).resetControlFlow(this);
  }

  @Override
  public boolean skipValidation() {
    return true;
  }

  @Override
  public IElementType getElementType() {
    return getNode().getElementType();
  }

  @Override
  public @Nullable Character getQuoteChar() {
    return JSEmbeddedContentImpl.getQuoteChar(this);
  }
}
