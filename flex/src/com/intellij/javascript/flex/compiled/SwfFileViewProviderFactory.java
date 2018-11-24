package com.intellij.javascript.flex.compiled;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * User: Maxim.Mossienko
 * Date: 03.03.2009
 * Time: 21:20:02
 */
public class SwfFileViewProviderFactory implements FileViewProviderFactory {
  @NotNull
  public FileViewProvider createFileViewProvider(@NotNull final VirtualFile file, Language language, @NotNull final PsiManager manager, final boolean eventSystemEnabled) {
    return new SwfFileViewProvider(manager, file, eventSystemEnabled);
  }

  private static class SwfFileViewProvider extends SingleRootFileViewProvider {
    public SwfFileViewProvider(PsiManager manager, VirtualFile file, boolean physical) {
      super(manager, file, physical);
    }
    
    @Override
    protected PsiFile createFile(@NotNull Project project, @NotNull VirtualFile vFile, @NotNull FileType fileType) {
      return new CompiledJSFile(this);
    }

    @NotNull
    @Override
    public Language getBaseLanguage() {
      return FlexApplicationComponent.DECOMPILED_SWF;
    }

    @NotNull
    @Override
    public SingleRootFileViewProvider createCopy(@NotNull VirtualFile copy) {
      return new SwfFileViewProvider(getManager(), copy, false);
    }
  }

  static class CompiledJSFile extends JSFileImpl implements PsiCompiledFile {
    public CompiledJSFile(FileViewProvider fileViewProvider) {
      super(fileViewProvider, DialectDetector.getJSLanguage(fileViewProvider.getVirtualFile()));
    }

    public PsiElement getMirror() {
      return this;
    }

    @Override
    public boolean isWritable() {
      return true;
    }

    @Override
    public PsiFile getDecompiledPsiFile() {
      return this;
    }
  }
}
