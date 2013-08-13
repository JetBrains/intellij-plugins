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
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.lang.ManifestTokenType;
import org.osmorc.manifest.lang.headerparser.HeaderParser;
import org.osmorc.manifest.lang.headerparser.HeaderParserRepository;
import org.osmorc.manifest.lang.psi.HeaderValuePart;
import org.osmorc.manifest.lang.psi.ManifestToken;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class HeaderValuePartImpl extends ASTWrapperPsiElement implements HeaderValuePart {
  private final HeaderParserRepository myRepository;

  public HeaderValuePartImpl(ASTNode node) {
    super(node);
    myRepository = ServiceManager.getService(HeaderParserRepository.class);
  }

  @NotNull
  @Override
  public String getUnwrappedText() {
    StringBuilder builder = new StringBuilder();
    PsiElement element = getFirstChild();
    while (element != null) {
      boolean ignore = false;
      if (element instanceof ManifestToken) {
        ManifestToken manifestToken = (ManifestToken)element;
        if (manifestToken.getTokenType() == ManifestTokenType.NEWLINE ||
            manifestToken.getTokenType() == ManifestTokenType.SIGNIFICANT_SPACE) {
          ignore = true;
        }
      }
      if (!ignore) {
        builder.append(element.getText());
      }
      element = element.getNextSibling();
    }

    return builder.toString().trim();
  }

  @Override
  public Object getConvertedValue() {
    HeaderParser headerParser = myRepository.getHeaderParser(this);
    return headerParser != null ? headerParser.getValue(this) : null;
  }

  @NotNull
  @Override
  public PsiReference[] getReferences() {
    HeaderParser headerParser = myRepository.getHeaderParser(this);
    return headerParser != null ? headerParser.getReferences(this) : PsiReference.EMPTY_ARRAY;
  }

  @Override
  public String toString() {
    return "HeaderValuePart";
  }
}
