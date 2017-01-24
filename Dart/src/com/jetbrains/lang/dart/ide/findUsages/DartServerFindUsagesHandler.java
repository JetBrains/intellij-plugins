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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.PsiSearchScopeUtil;
import com.intellij.psi.search.SearchScope;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Processor;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.psi.DartReference;
import org.dartlang.analysis.server.protocol.Location;
import org.dartlang.analysis.server.protocol.SearchResult;
import org.dartlang.analysis.server.protocol.SearchResultKind;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartServerFindUsagesHandler extends FindUsagesHandler {
  public DartServerFindUsagesHandler(@NotNull final PsiElement element) {
    super(mayBeChangeToNameIdentifier(element));
  }

  @NotNull
  private static PsiElement mayBeChangeToNameIdentifier(@NotNull final PsiElement element) {
    if (element instanceof PsiNameIdentifierOwner) {
      final PsiElement nameIdentifier = ((PsiNameIdentifierOwner)element).getNameIdentifier();
      if (nameIdentifier != null) return nameIdentifier;
    }
    return element;
  }

  @Override
  public boolean processElementUsages(@NotNull final PsiElement elementToSearch,
                                      @NotNull final Processor<UsageInfo> processor,
                                      @NotNull final FindUsagesOptions options) {
    final SearchScope scope = options.searchScope;
    final Project project = ApplicationManager.getApplication().runReadAction((Computable<Project>)() -> getProject());
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(project);

    final ReadActionConsumer<SearchResult> searchResultProcessor = new ReadActionConsumer<SearchResult>() {
      @Override
      public void consumeInReadAction(SearchResult result) {
        if (result.getKind().equals(SearchResultKind.DECLARATION)) return;

        final Location location = result.getLocation();
        final VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(FileUtil.toSystemIndependentName(location.getFile()));
        if (vFile == null) return;

        if (!scope.contains(vFile)) return;

        final PsiFile psiFile = elementToSearch.getManager().findFile(vFile);
        if (psiFile == null) return;

        final int offset = service.getConvertedOffset(vFile, location.getOffset());
        final int length = service.getConvertedOffset(vFile, location.getOffset() + location.getLength()) - offset;
        final TextRange range = TextRange.create(offset, offset + length);

        final boolean potentialUsage = result.isPotential();
        final PsiElement usageElement = getUsagePsiElement(psiFile, range);
        final UsageInfo usageInfo = usageElement == null ? null : getUsageInfo(usageElement, range, potentialUsage);

        if (usageInfo != null &&
            usageInfo.getElement() != null &&
            (!(scope instanceof LocalSearchScope) || PsiSearchScopeUtil.isInScope((LocalSearchScope)scope, usageInfo.getElement()))) {
          processor.process(usageInfo);
        }
      }
    };

    final VirtualFile file = ApplicationManager.getApplication()
      .runReadAction((Computable<VirtualFile>)() -> elementToSearch.getContainingFile().getVirtualFile());

    final int offset = elementToSearch.getTextRange().getStartOffset();
    service.search_findElementReferences(file, offset, searchResultProcessor);

    return true;
  }

  @Nullable
  public static UsageInfo getUsageInfo(@NotNull final PsiElement usageElement,
                                       @NotNull final TextRange range,
                                       final boolean potentialUsage) {
    final int offset = range.getStartOffset() - usageElement.getTextRange().getStartOffset();
    boolean nonCodeUsage = usageElement instanceof PsiComment || usageElement.getParent() instanceof PsiComment;

    final UsageInfo usageInfo = new UsageInfo(usageElement, offset, offset + range.getLength(), nonCodeUsage);
    usageInfo.setDynamicUsage(potentialUsage);

    return usageInfo;
  }

  @Nullable
  public static PsiElement getUsagePsiElement(@NotNull final PsiFile psiFile, @NotNull final TextRange textRange) {
    // try to find DartReference matching textRange. If not possible then return the topmost element matching textRange.
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
