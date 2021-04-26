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
package com.intellij.protobuf.lang.resolve.directive;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.util.ProcessingContext;
import com.intellij.protobuf.lang.psi.PbTextFile;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/** A {@link CompletionContributor} that adds completions for the text format schema comments. */
public class SchemaDirectiveCompletionContributor extends CompletionContributor {

  private static final Pattern PREFIX_PATTERN = Pattern.compile("^#\\s*[^\\s]*$");

  public SchemaDirectiveCompletionContributor() {
    extend(
        CompletionType.BASIC,
        psiElement(PsiComment.class).withParent(PbTextFile.class),
        new CommentCompletionProvider());
  }

  private static class CommentCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(
        @NotNull CompletionParameters parameters,
        @NotNull ProcessingContext context,
        @NotNull CompletionResultSet result) {

      // Only contribute completions when the comment text up to the character contains either no
      // token, or the beginning of a token. For example
      //  # |        <- contribute
      //  # pro|     <- contribute
      //  # foo|     <- contribute (but IntelliJ filters based on the existing prefix)
      //  # foo |    <- do not contribute
      //  # foo pro| <- do not contribute
      TextRange rangeUpToCaret =
          TextRange.create(parameters.getPosition().getTextOffset(), parameters.getOffset());
      String textUpToCaret = rangeUpToCaret.substring(parameters.getOriginalFile().getText());
      if (!PREFIX_PATTERN.matcher(textUpToCaret).matches()) {
        return;
      }

      // Only add each suggestion if it's not already present in the file.
      SchemaDirective directive = SchemaDirective.find(parameters.getOriginalFile());
      if (directive == null || directive.getFileComment() == null) {
        result.addElement(commentLookupElement("proto-file"));
      }
      if (directive == null || directive.getMessageComment() == null) {
        result.addElement(commentLookupElement("proto-message"));
      }
      result.addElement(commentLookupElement("proto-import"));
    }

    private static LookupElement commentLookupElement(String commentPrefix) {
      return LookupElementBuilder.create(commentPrefix + ": ")
          .withPresentableText(commentPrefix)
          .withInsertHandler(
              (insertionContext, item) ->
                  AutoPopupController.getInstance(insertionContext.getProject())
                      .scheduleAutoPopup(insertionContext.getEditor()));
    }
  }
}
