/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.manifest.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;
import org.osmorc.manifest.lang.psi.Attribute;
import org.osmorc.manifest.lang.psi.Clause;
import org.osmorc.manifest.lang.psi.Directive;

import java.util.List;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class ClauseImpl extends ASTWrapperPsiElement implements Clause {
  public ClauseImpl(ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull String getUnwrappedText() {
    return getText().replaceAll("(?s)\\s*\n\\s*", "").trim();
  }

  @Override
  public HeaderValuePart getValue() {
    return findChildByClass(HeaderValuePart.class);
  }

  @Override
  public @NotNull List<Attribute> getAttributes() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, Attribute.class);
  }

  @Override
  public @Nullable Attribute getAttribute(@NotNull String name) {
    for (Attribute child = PsiTreeUtil.findChildOfType(this, Attribute.class);
         child != null;
         child = PsiTreeUtil.getNextSiblingOfType(child, Attribute.class)) {
      if (name.equals(child.getName())) {
        return child;
      }
    }

    return null;
  }

  @Override
  public @NotNull List<Directive> getDirectives() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, Directive.class);
  }

  @Override
  public Directive getDirective(@NotNull String name) {
    for (Directive child = PsiTreeUtil.findChildOfType(this, Directive.class);
         child != null;
         child = PsiTreeUtil.getNextSiblingOfType(child, Directive.class)) {
      if (name.equals(child.getName())) {
        return child;
      }
    }

    return null;
  }

  @Override
  public String toString() {
    return "Clause";
  }
}
