/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij.coldFusion.model.files;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 06.10.2008
 */
public class CfmlFileViewProviderFactory implements FileViewProviderFactory {
  public FileViewProvider createFileViewProvider(@NotNull VirtualFile file,
                                                 com.intellij.lang.Language language,
                                                 @NotNull PsiManager manager,
                                                 boolean physical) {
    return new CfmlFileViewProvider(manager, file, physical);
  }
}
