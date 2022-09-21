package com.intellij.javascript.flex.mxml.schema;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public final class MxmlBackedElementDescriptor extends ClassBackedElementDescriptor {
  private final VirtualFile file;

  MxmlBackedElementDescriptor(String _classname, CodeContext _context, Project _project, VirtualFile _file) {
    super(_classname,_context,_project, false);
    file = _file;
  }

  @Override
  @Nullable
  public PsiElement getDeclaration() {
    return file.isValid() ? PsiManager.getInstance(project).findFile(file) : null;
  }

  @Override
  @NonNls
  public String getName() {
    return file.getNameWithoutExtension();
  }
}