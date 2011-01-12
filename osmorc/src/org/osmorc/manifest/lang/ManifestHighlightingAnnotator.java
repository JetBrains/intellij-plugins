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

import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.osmorc.manifest.lang.psi.*;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ManifestHighlightingAnnotator implements Annotator {
    public void annotate(PsiElement psiElement, AnnotationHolder holder) {
        if (psiElement instanceof Attribute) {
            annotate(((Attribute) psiElement).getNamePsi(), ManifestColorsAndFonts.ATTRIBUTE_NAME_KEY, holder);
        } else if (psiElement instanceof Directive) {
            annotate(((Directive) psiElement).getNamePsi(), ManifestColorsAndFonts.DIRECTIVE_NAME_KEY, holder);
        } else if (psiElement instanceof ManifestToken) {
            ManifestToken manifestToken = (ManifestToken) psiElement;
            if (manifestToken.getParent() instanceof Attribute && manifestToken.getTokenType() == ManifestTokenType.EQUALS) {
                annotate(manifestToken, ManifestColorsAndFonts.ATTRIBUTE_ASSIGNMENT_KEY, holder);
            } else if (manifestToken.getParent() instanceof Directive && (manifestToken.getTokenType() == ManifestTokenType.COLON || manifestToken.getTokenType() == ManifestTokenType.EQUALS)) {
                annotate(manifestToken, ManifestColorsAndFonts.DIRECTIVE_ASSIGNMENT_KEY, holder);    
            } else if (manifestToken.getParent() instanceof Clause && manifestToken.getTokenType() == ManifestTokenType.SEMICOLON) {
                annotate(manifestToken, ManifestColorsAndFonts.PARAMETER_SEPARATOR_KEY, holder);
            } else if (manifestToken.getParent() instanceof Header && manifestToken.getTokenType() == ManifestTokenType.COMMA) {
                annotate(manifestToken, ManifestColorsAndFonts.CLAUSE_SEPARATOR_KEY, holder);    
            }
        }
    }

    private void annotate(PsiElement element, TextAttributesKey textAttributesKey, AnnotationHolder holder) {
        holder.createWeakWarningAnnotation(element, null).setTextAttributes(textAttributesKey);

    }
}
