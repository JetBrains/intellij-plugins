package com.intellij.javascript.flex.compiled;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.Language;
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
  public FileViewProvider createFileViewProvider(final VirtualFile file, Language language, final PsiManager manager, final boolean physical) {
    return new SwfFileViewProvider(manager, file, physical);
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

  static class CompiledJSFile extends JSFileImpl implements PsiCompiledElement {
    public CompiledJSFile(FileViewProvider fileViewProvider) {
      super(fileViewProvider);
    }

    public PsiElement getMirror() {
      return this;
    }

    @Override
    public boolean isWritable() {
      return false;
    }
  }
}
