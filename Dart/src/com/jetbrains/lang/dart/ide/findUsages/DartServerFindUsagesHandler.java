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
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Processor;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.dartlang.analysis.server.protocol.Location;
import org.dartlang.analysis.server.protocol.SearchResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartServerFindUsagesHandler extends FindUsagesHandler {
  public DartServerFindUsagesHandler(PsiElement element) {
    super(element);
  }

  @Override
  public boolean processElementUsages(@NotNull final PsiElement element,
                                      @NotNull final Processor<UsageInfo> processor,
                                      @NotNull final FindUsagesOptions options) {
    final ReadActionConsumer<SearchResult> searchResultProcessor = new ReadActionConsumer<SearchResult>() {
      @Override
      public void consumeInReadAction(SearchResult result) {
        final Location location = result.getLocation();
        final PsiElement locationPsiElement = findPsiElement(element, location);
        if (locationPsiElement != null) {
          int offset = location.getOffset();
          int length = location.getLength();
          offset -= locationPsiElement.getTextOffset();
          processor.process(new UsageInfo(locationPsiElement, offset, offset + length));
        }
      }
    };
    // Send the search request and wait for results.
    final String elementFilePath = readEnclosingFilePath(element);
    final int elementOffset = element.getTextOffset();
    DartAnalysisServerService.getInstance().search_findElementReferences(elementFilePath, elementOffset, searchResultProcessor);
    // OK
    return true;
  }

  @Nullable
  private static PsiElement findPsiElement(PsiElement context, Location location) {
    final String locationFilePath = location.getFile();
    final PsiFile psiFile = findPsiFile(context, locationFilePath);
    if (psiFile != null) {
      final int offset = location.getOffset();
      return psiFile.findElementAt(offset);
    }
    return null;
  }

  @Nullable
  private static PsiFile findPsiFile(@NotNull PsiElement element, @NotNull String path) {
    final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
    if (virtualFile != null) {
      return element.getManager().findFile(virtualFile);
    }
    return null;
  }

  @NotNull
  private static String readEnclosingFilePath(@NotNull final PsiElement element) {
    return ApplicationManager.getApplication().runReadAction(new Computable<String>() {
      @Override
      public String compute() {
        final VirtualFile elementFile = element.getContainingFile().getVirtualFile();
        return FileUtil.toSystemDependentName(elementFile.getPath());
      }
    });
  }
}
