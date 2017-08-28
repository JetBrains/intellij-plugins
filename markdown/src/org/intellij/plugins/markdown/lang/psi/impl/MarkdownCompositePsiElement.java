package org.intellij.plugins.markdown.lang.psi.impl;

import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement;

public interface MarkdownCompositePsiElement extends MarkdownPsiElement {
  String getPresentableTagName();
}