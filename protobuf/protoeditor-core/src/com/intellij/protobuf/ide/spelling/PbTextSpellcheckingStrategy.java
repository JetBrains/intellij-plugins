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
package com.intellij.protobuf.ide.spelling;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.intellij.protobuf.lang.psi.ProtoStringPart;
import com.intellij.protobuf.lang.resolve.directive.SchemaDirective;
import org.jetbrains.annotations.NotNull;

/** A {@link SpellcheckingStrategy} for proto text format files */
public class PbTextSpellcheckingStrategy extends SpellcheckingStrategy {
  @NotNull
  @Override
  public Tokenizer<?> getTokenizer(PsiElement element) {
    if (element instanceof ProtoStringPart) {
      return StringPartTokenizer.INSTANCE;
    }
    if (element instanceof PsiComment) {
      SchemaDirective directive = SchemaDirective.find(element.getContainingFile());
      if (directive != null && directive.getSchemaComment((PsiComment) element) != null) {
        // No spell checking for the special "# proto-file" and "# proto-message" comments.
        return EMPTY_TOKENIZER;
      }
    }
    return super.getTokenizer(element);
  }
}
