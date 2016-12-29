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
package org.osmorc.manifest.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.lang.manifest.psi.ManifestElementType;
import org.osmorc.manifest.lang.psi.impl.AttributeImpl;
import org.osmorc.manifest.lang.psi.impl.ClauseImpl;
import org.osmorc.manifest.lang.psi.impl.DirectiveImpl;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public abstract class OsgiManifestElementType extends ManifestElementType {
  public static final IElementType ATTRIBUTE = new OsgiManifestElementType("ATTRIBUTE") {
    @Override
    public PsiElement createPsi(ASTNode node) {
      return new AttributeImpl(node);
    }
  };

  public static final IElementType DIRECTIVE = new OsgiManifestElementType("DIRECTIVE") {
    @Override
    public PsiElement createPsi(ASTNode node) {
      return new DirectiveImpl(node);
    }
  };

  public static final IElementType CLAUSE = new OsgiManifestElementType("CLAUSE") {
    @Override
    public PsiElement createPsi(ASTNode node) {
      return new ClauseImpl(node);
    }
  };

  private OsgiManifestElementType(String name) {
    super(name);
  }
}
