/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.protobuf.lang.psi.PbCommentOwner;
import com.intellij.protobuf.lang.psi.util.PbCommentUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A {@link com.intellij.lang.documentation.DocumentationProvider} for proto elements. */
public class PbDocumentationProvider extends AbstractDocumentationProvider {

  @Override
  public @Nullable @Nls String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
    if (!(element instanceof PbCommentOwner owner)) {
      return null;
    }

    List<PsiComment> comments = owner.getComments();
    if (comments.isEmpty()) {
      return null;
    }

    @NlsSafe
    StringBuilder commentBuilder = new StringBuilder("<pre>");
    for (String line : PbCommentUtil.extractText(comments)) {
      commentBuilder.append(StringUtil.escapeXmlEntities(line));
      commentBuilder.append("\n");
    }
    commentBuilder.append("</pre>");

    return commentBuilder.toString();
  }
}
