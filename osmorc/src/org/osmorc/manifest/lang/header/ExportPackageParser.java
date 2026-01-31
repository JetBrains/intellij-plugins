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
package org.osmorc.manifest.lang.header;

import com.intellij.codeInsight.daemon.JavaErrorBundle;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.ManifestBundle;
import org.jetbrains.lang.manifest.header.HeaderParser;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValue;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;
import org.jetbrains.lang.manifest.psi.ManifestToken;
import org.jetbrains.lang.manifest.psi.ManifestTokenType;
import org.osgi.framework.Constants;
import org.osmorc.manifest.lang.psi.Attribute;
import org.osmorc.manifest.lang.psi.Clause;
import org.osmorc.manifest.lang.psi.Directive;
import org.osmorc.util.OsgiPsiUtil;

import java.util.List;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class ExportPackageParser extends BasePackageParser {
  public static final HeaderParser INSTANCE = new ExportPackageParser();

  private static final TokenSet TOKEN_FILTER = TokenSet.create(ManifestTokenType.HEADER_VALUE_PART);

  @Override
  public PsiReference @NotNull [] getReferences(@NotNull HeaderValuePart headerValuePart) {
    PsiElement parent = headerValuePart.getParent();
    if (parent instanceof Clause) {
      PsiElement element = headerValuePart.getOriginalElement().getPrevSibling();
      if (!(element instanceof ManifestToken) ||
          ((ManifestToken)element).getTokenType() != ManifestTokenType.SEMICOLON) {
        return getPackageReferences(headerValuePart);
      }
    }
    else if (parent instanceof Attribute attribute) {
      if (Constants.USES_DIRECTIVE.equals(attribute.getName())) {
        List<PsiReference> references = new SmartList<>();
        for (ASTNode astNode : headerValuePart.getNode().getChildren(TOKEN_FILTER)) {
          if (astNode instanceof ManifestToken manifestToken) {
            ContainerUtil.addAll(references, getPackageReferences(manifestToken));
          }
        }
        return references.toArray(PsiReference.EMPTY_ARRAY);
      }
    }

    return PsiReference.EMPTY_ARRAY;
  }

  @Override
  public boolean annotate(@NotNull Header header, @NotNull AnnotationHolder holder) {
    if (super.annotate(header, holder)) {
      return true;
    }

    boolean annotated = false;

    for (HeaderValue value : header.getHeaderValues()) {
      if (value instanceof Clause) {
        Directive uses = ((Clause)value).getDirective(Constants.USES_DIRECTIVE);
        if (uses != null) {
          HeaderValuePart valuePart = uses.getValueElement();
          if (valuePart != null) {
            String text = StringUtil.trimTrailing(valuePart.getText());
            int start = StringUtil.startsWithChar(text, '"') ? 1 : 0;
            int length = StringUtil.endsWithChar(text, '"') ? text.length() - 1 : text.length();
            int offset = valuePart.getTextOffset();

            while (start < length) {
              int end = text.indexOf(',', start);
              if (end < 0) end = length;
              TextRange range = new TextRange(start, end);
              start = end + 1;

              String packageName = range.substring(text).replaceAll("\\s", "");

              if (StringUtil.isEmptyOrSpaces(packageName)) {
                TextRange highlight = range.shiftRight(offset);
                holder.newAnnotation(HighlightSeverity.ERROR, ManifestBundle.message("header.reference.invalid")).range(highlight).create();
                annotated = true;
                continue;
              }

              PsiDirectory[] directories = OsgiPsiUtil.resolvePackage(header, packageName);
              if (directories.length == 0) {
                TextRange highlight = adjust(range, text).shiftRight(offset);
                holder.newAnnotation(HighlightSeverity.ERROR, JavaErrorBundle.message("cannot.resolve.package", packageName)).range(highlight).create();
                annotated = true;
              }
            }
          }
        }
      }
    }

    return annotated;
  }

  private static TextRange adjust(TextRange range, String text) {
    int end = range.getEndOffset(), start = range.getStartOffset();
    while (end > start && Character.isWhitespace(text.charAt(end - 1))) end--;
    while (start < end && Character.isWhitespace(text.charAt(start))) start++;
    return new TextRange(start, end);
  }
}