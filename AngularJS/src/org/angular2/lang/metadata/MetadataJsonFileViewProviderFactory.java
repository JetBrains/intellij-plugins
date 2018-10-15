// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata;

import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.metadata.Angular2MetadataLanguage;
import org.angular2.lang.metadata.psi.MetadataFileImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class MetadataJsonFileViewProviderFactory implements FileViewProviderFactory {

  @NotNull
  @Override
  public FileViewProvider createFileViewProvider(@NotNull VirtualFile file,
                                                 Language language,
                                                 @NotNull PsiManager manager,
                                                 boolean eventSystemEnabled) {
    // To handle other, non-Angular metadata stubs, provide extension to supply other metadata languages here
    return new MetadataFileViewProvider(manager, file, eventSystemEnabled, Angular2MetadataLanguage.INSTANCE);
  }

  private static class MetadataFileViewProvider extends MultiplePsiFilesPerDocumentFileViewProvider {

    private final Angular2MetadataLanguage myStubsLanguage;
    private final Set<Language> myLanguages;

    protected MetadataFileViewProvider(PsiManager manager,
                                       VirtualFile virtualFile,
                                       boolean physical, Angular2MetadataLanguage stubsLanguage) {
      super(manager, virtualFile, physical);
      myStubsLanguage = stubsLanguage;
      myLanguages = ContainerUtil.newHashSet(MetadataJsonLanguage.INSTANCE, myStubsLanguage);
    }

    @NotNull
    @Override
    public Language getBaseLanguage() {
      return MetadataJsonLanguage.INSTANCE;
    }

    @NotNull
    @Override
    public Set<Language> getLanguages() {
      return myLanguages;
    }

    @NotNull
    @Override
    protected MultiplePsiFilesPerDocumentFileViewProvider cloneInner(@NotNull VirtualFile fileCopy) {
      return new MetadataFileViewProvider(getManager(), fileCopy, false, myStubsLanguage);
    }

    @Nullable
    @Override
    protected PsiFile createFile(@NotNull Language lang) {
      if (lang == myStubsLanguage) {
        return new MetadataFileImpl(this, myStubsLanguage);
      }
      return super.createFile(lang);
    }
  }
}
