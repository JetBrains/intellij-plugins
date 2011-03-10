package com.intellij.javascript.flex.compiled;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewBuilderProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.lang.javascript.structureView.JSStructureViewBuilderFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: 07.03.2009
 * Time: 18:55:58
 * To change this template use File | Settings | File Templates.
 */
public class SwfStructureViewBuilderProvider implements StructureViewBuilderProvider {
  private final JSStructureViewBuilderFactory myFactory = new JSStructureViewBuilderFactory();
  public StructureViewBuilder getStructureViewBuilder(@NotNull FileType fileType, @NotNull VirtualFile file, @NotNull Project project) {
    if (/*TODO: no performance problems*/ true) return null;
    final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    if (psiFile == null) return null;
    return myFactory.getStructureViewBuilder(psiFile);
  }
}
