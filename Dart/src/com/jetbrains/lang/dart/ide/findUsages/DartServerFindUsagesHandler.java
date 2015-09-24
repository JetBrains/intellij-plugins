/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.ide.findUsages;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.PsiSearchScopeUtil;
import com.intellij.psi.search.SearchScope;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Processor;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.util.DartElementLocation;
import org.dartlang.analysis.server.protocol.Location;
import org.dartlang.analysis.server.protocol.SearchResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartServerFindUsagesHandler extends FindUsagesHandler {
  public DartServerFindUsagesHandler(PsiElement element) {
    super(element);
  }

  @Override
  public boolean processElementUsages(@NotNull final PsiElement elementToSearch,
                                      @NotNull final Processor<UsageInfo> processor,
                                      @NotNull final FindUsagesOptions options) {
    final ReadActionConsumer<SearchResult> searchResultProcessor = new ReadActionConsumer<SearchResult>() {
      @Override
      public void consumeInReadAction(SearchResult result) {
        final SearchScope scope = options.searchScope;

        final Location location = result.getLocation();
        final VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(FileUtil.toSystemIndependentName(location.getFile()));
        if (vFile == null) return;

        if (scope instanceof LocalSearchScope && !((LocalSearchScope)scope).isInScope(vFile)) return;
        if (scope instanceof GlobalSearchScope && !((GlobalSearchScope)scope).contains(vFile)) return;

        final PsiFile psiFile = elementToSearch.getManager().findFile(vFile);
        if (psiFile == null) return;

        final PsiElement usageElement = getUsagePsiElement(psiFile, TextRange.create(location.getOffset(), location.getOffset() + location
          .getLength()));
        if (usageElement == null) return;

        if (scope instanceof LocalSearchScope && !PsiSearchScopeUtil.isInScope((LocalSearchScope)scope, usageElement)) return;

        int offset = location.getOffset();
        int length = location.getLength();
        offset -= usageElement.getTextOffset();
        // todo do we want to mark usages in doc comments as nonCodeUsage?
        final UsageInfo usageInfo = new UsageInfo(usageElement, offset, offset + length);
        usageInfo.setDynamicUsage(result.isPotential());
        processor.process(usageInfo);
      }
    };

    // Send the search request and wait for results.
    final DartElementLocation elementLocation = DartElementLocation.of(elementToSearch);
    DartAnalysisServerService.getInstance()
      .search_findElementReferences(elementLocation.file, elementLocation.offset, searchResultProcessor);
    // OK
    return true;
  }

  @Nullable
  private static PsiElement getUsagePsiElement(@NotNull final PsiFile psiFile, @NotNull final TextRange textRange) {
    // try to find Dartreference matching textRange. If not possible then return the topmost element matching textRange.
    // If neither found then return minimal element that includes the textRange.
    PsiElement element = psiFile.findElementAt(textRange.getStartOffset());
    if (element == null) return null;

    boolean rangeOk = element.getTextRange().contains(textRange);
    if (rangeOk && element instanceof DartReference) return element;

    TextRange previousRange = element.getTextRange();
    PsiElement parent;
    while ((parent = element.getParent()) != null) {
      final TextRange parentRange = parent.getTextRange();
      if (rangeOk) {
        if (!parentRange.equals(previousRange)) {
          return element; // range became bigger, return previous that matched better
        }

        if (parent instanceof DartReference) {
          return parent;
        }
        else {
          previousRange = parentRange;
          element = parent;
        }
      }
      else {
        rangeOk = parent.getTextRange().contains(textRange);
        if (rangeOk && parent instanceof DartReference) {
          return parent;
        }
        else {
          previousRange = parentRange;
          element = parent;
        }
      }
    }

    return null;
  }
}
