// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.lang.dart.ide.completion;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartFileInfo;
import com.jetbrains.lang.dart.analyzer.DartFileInfoKt;
import com.jetbrains.lang.dart.analyzer.DartLocalFileInfo;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartId;
import org.dartlang.analysis.server.protocol.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartLookupObject {
  @NotNull private final Project myProject;
  @Nullable private final Location myLocation;
  private final int myRelevance;

  public DartLookupObject(@NotNull final Project project, @Nullable final Location location, final int relevance) {
    myProject = project;
    myLocation = location;
    myRelevance = relevance;
  }

  public int getRelevance() {
    return myRelevance;
  }

  /**
   * This method may parse source code, so use it responsibly: do not call it for all DartLookupObjects from the completion list.
   */
  public @Nullable PsiElement findPsiElement() {
    if (myLocation == null) return null;

    String filePathOrUri = myLocation.getFile();
    DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(filePathOrUri);
    VirtualFile vFile = fileInfo instanceof DartLocalFileInfo localFileInfo ? localFileInfo.findFile() : null;
    final PsiFile psiFile = vFile == null ? null : PsiManager.getInstance(myProject).findFile(vFile);
    if (psiFile != null) {
      final int offset = DartAnalysisServerService.getInstance(myProject).getConvertedOffset(vFile, myLocation.getOffset());
      final PsiElement elementAtOffset = psiFile.findElementAt(offset);
      if (elementAtOffset != null) {
        final DartComponentName componentName = PsiTreeUtil.getParentOfType(elementAtOffset, DartComponentName.class);
        if (componentName != null) {
          return componentName;
        }
        if (elementAtOffset.getParent() instanceof DartId && elementAtOffset.getTextRange().getStartOffset() == offset) {
          return elementAtOffset; // example in WEB-25478 (https://github.com/flutter/flutter-intellij/issues/385#issuecomment-278826063)
        }
      }
    }
    return null;
  }
}
