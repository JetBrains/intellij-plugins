// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.compiled;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageUtil;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSFileCachedData;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiCompiledFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class SwfFileViewProviderFactory implements FileViewProviderFactory {
  @Override
  public @NotNull FileViewProvider createFileViewProvider(final @NotNull VirtualFile file, Language language, final @NotNull PsiManager manager, final boolean eventSystemEnabled) {
    return new SwfFileViewProvider(manager, file, eventSystemEnabled);
  }

  private static class SwfFileViewProvider extends SingleRootFileViewProvider {
    SwfFileViewProvider(PsiManager manager, VirtualFile file, boolean physical) {
      super(manager, file, physical);
    }

    @Override
    protected PsiFile createFile(@NotNull Project project, @NotNull VirtualFile vFile, @NotNull FileType fileType) {
      return new CompiledJSFile(this);
    }

    @Override
    public @NotNull Language getBaseLanguage() {
      return FlexApplicationComponent.DECOMPILED_SWF;
    }

    @Override
    public @NotNull SingleRootFileViewProvider createCopy(@NotNull VirtualFile copy) {
      return new SwfFileViewProvider(getManager(), copy, false);
    }
  }

  static class CompiledJSFile extends JSFileImpl implements PsiCompiledFile {
    private static final JSFileCachedData EMPTY = new JSFileCachedData();

    CompiledJSFile(FileViewProvider fileViewProvider) {
      super(fileViewProvider, Objects.requireNonNullElse(
        ObjectUtils.tryCast(LanguageUtil.getFileTypeLanguage(fileViewProvider.getFileType()), JSLanguageDialect.class),
        FlexSupportLoader.ECMA_SCRIPT_L4));
    }

    @Override
    public @Nullable PsiElement getCachedMirror() {
      return getMirror();
    }

    @Override
    public PsiElement getMirror() {
      return this;
    }

    @Override
    public @NotNull PsiFile getDecompiledPsiFile() {
      return this;
    }

    @Override
    public @NotNull JSFileCachedData getCachedData() {
      return EMPTY;
    }
  }
}
