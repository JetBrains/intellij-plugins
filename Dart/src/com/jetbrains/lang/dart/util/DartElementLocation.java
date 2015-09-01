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
package com.jetbrains.lang.dart.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class DartElementLocation {
  public final String file;
  public final int offset;

  private DartElementLocation(String file, int offset) {
    this.file = file;
    this.offset = offset;
  }

  public static DartElementLocation of(PsiElement element) {
    String file = readEnclosingFilePath(element);
    int offset = element.getTextOffset();
    return new DartElementLocation(file, offset);
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