// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.angular2.lang.metadata.psi.MetadataFileImpl;
import org.jetbrains.annotations.NotNull;

public class MetadataJsonFileViewProviderFactory implements FileViewProviderFactory {

  @Override
  public @NotNull FileViewProvider createFileViewProvider(@NotNull VirtualFile file,
                                                          Language language,
                                                          @NotNull PsiManager manager,
                                                          boolean eventSystemEnabled) {
    return new MetadataFileViewProvider(manager, file, eventSystemEnabled);
  }

  public static final class MetadataFileViewProvider extends SingleRootFileViewProvider {
    private MetadataFileViewProvider(@NotNull PsiManager manager,
                                     @NotNull VirtualFile file,
                                     boolean eventSystemEnabled) {
      super(manager, file, eventSystemEnabled, MetadataJsonLanguage.INSTANCE);
      assert file.getFileType() instanceof MetadataJsonFileType;
    }

    @Override
    protected PsiFile createFile(@NotNull Project project, @NotNull VirtualFile file, @NotNull FileType fileType) {
      return new MetadataFileImpl(this, (MetadataJsonFileType)fileType);
    }

    @Override
    public @NotNull SingleRootFileViewProvider createCopy(@NotNull VirtualFile copy) {
      return new MetadataFileViewProvider(getManager(), copy, false);
    }
  }
}
