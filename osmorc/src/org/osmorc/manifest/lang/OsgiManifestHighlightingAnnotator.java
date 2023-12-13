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
package org.osmorc.manifest.lang;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;
import org.jetbrains.lang.manifest.psi.ManifestToken;
import org.jetbrains.lang.manifest.psi.ManifestTokenType;
import org.osmorc.manifest.lang.psi.AssignmentExpression;
import org.osmorc.manifest.lang.psi.Attribute;
import org.osmorc.manifest.lang.psi.Clause;
import org.osmorc.manifest.lang.psi.Directive;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public final class OsgiManifestHighlightingAnnotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (element instanceof HeaderValuePart) {
      PsiElement parent = element.getParent();
      if (parent instanceof AssignmentExpression) {
        HeaderValuePart nameElement = ((AssignmentExpression)parent).getNameElement();
        if (parent instanceof Attribute) {
          if (element == nameElement) {
            annotate(OsgiManifestColorsAndFonts.ATTRIBUTE_NAME_KEY, holder);
          }
          else {
            annotate(OsgiManifestColorsAndFonts.ATTRIBUTE_VALUE_KEY, holder);
          }
        }
        else if (parent instanceof Directive) {
          if (element == nameElement) {
            annotate(OsgiManifestColorsAndFonts.DIRECTIVE_NAME_KEY, holder);
          }
          else {
            annotate(OsgiManifestColorsAndFonts.DIRECTIVE_VALUE_KEY, holder);
          }
        }
      }
    }
    else if (element instanceof ManifestToken) {
      ManifestTokenType type = ((ManifestToken)element).getTokenType();
      if (element.getParent() instanceof Attribute && type == ManifestTokenType.EQUALS) {
        annotate(OsgiManifestColorsAndFonts.ATTRIBUTE_ASSIGNMENT_KEY, holder);
      }
      else if (element.getParent() instanceof Directive && (type == ManifestTokenType.COLON || type == ManifestTokenType.EQUALS)) {
        annotate(OsgiManifestColorsAndFonts.DIRECTIVE_ASSIGNMENT_KEY, holder);
      }
      else if (element.getParent() instanceof Clause && type == ManifestTokenType.SEMICOLON) {
        annotate(OsgiManifestColorsAndFonts.PARAMETER_SEPARATOR_KEY, holder);
      }
      else if (element.getParent() instanceof Header && type == ManifestTokenType.COMMA) {
        annotate(OsgiManifestColorsAndFonts.CLAUSE_SEPARATOR_KEY, holder);
      }
    }
  }

  private static void annotate(@NotNull TextAttributesKey key, @NotNull AnnotationHolder holder) {
    holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(key).create();
  }
}
