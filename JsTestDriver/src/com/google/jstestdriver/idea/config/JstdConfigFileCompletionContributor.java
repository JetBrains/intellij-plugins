/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequence;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;

public class JstdConfigFileCompletionContributor extends CompletionContributor {

  public JstdConfigFileCompletionContributor() {
    extend(CompletionType.BASIC, getElementPattern(), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition();
        String prefix = element.getText().substring(0, parameters.getOffset() - element.getTextRange().getStartOffset());
        YAMLDocument document = JstdConfigFileUtils.getVerifiedHierarchyHead(
            element.getParent(),
            new Class[] {
                YAMLSequence.class,
                YAMLCompoundValue.class,
                YAMLKeyValue.class
            },
            YAMLDocument.class
        );
        if (document != null) {
          VirtualFile basePath = JstdConfigFileUtils.extractBasePath(document);
          if (basePath != null) {
            addPathCompletions(result, prefix, basePath);
          }
        }
        document = JstdConfigFileUtils.getVerifiedHierarchyHead(
            element.getParent(),
            new Class[] {YAMLKeyValue.class},
            YAMLDocument.class
        );
        if (document != null) {
          addTopLevelKeysCompletions(result, prefix);
        }
      }
    });
  }

  private void addTopLevelKeysCompletions(CompletionResultSet result, String prefix) {
    for (String key : JstdConfigFileUtils.VALID_TOP_LEVEL_KEYS) {
      if (key.startsWith(prefix)) {
        result = result.withPrefixMatcher(prefix);
        result.addElement(LookupItem.fromString(key));
      }
    }
  }

  private void addPathCompletions(CompletionResultSet result, @NotNull String pathPrefix, @NotNull VirtualFile basePath) {
    char separator = '/';
    String fakePathPrefix = separator + pathPrefix;
    int lastSlashInd = fakePathPrefix.lastIndexOf(separator);
    String lastPathComponent = fakePathPrefix.substring(lastSlashInd + 1, fakePathPrefix.length());
    String pathComponentsExceptLast = fakePathPrefix.substring(Math.min(1, lastSlashInd), lastSlashInd);

    VirtualFile parentFile = basePath.findFileByRelativePath(pathComponentsExceptLast);
    if (parentFile != null) {
      VirtualFile[] children = parentFile.getChildren();
      for (VirtualFile child : children) {
        if (child.getName().startsWith(lastPathComponent)) {
          result = result.withPrefixMatcher(lastPathComponent);
          result.addElement(LookupItem.fromString(child.getName()));
        }
      }
    }
  }

  private static class Holder<T extends PsiElement> extends PsiElementPattern<T, Holder<T>> {

    protected Holder(Class<T> tClass) {
      super(tClass);
    }

    private static <K extends PsiElement> Holder<K> newCapture(Class<K> tClass) {
      return new Holder<K>(tClass);
    }
  }

  private static Holder<PsiElement> getElementPattern() {
    return Holder.newCapture(PsiElement.class);
  }

}
