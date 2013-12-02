package org.angularjs.codeInsight;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;

/**
 * Created by denofevil on 26/11/13.
 */
public class AngularAttributeDescriptor extends AnyXmlAttributeDescriptor {
  private final Project myProject;
  private final VirtualFile myFile;
  private final int myOffset;

  public AngularAttributeDescriptor(String attributeName) {
    this(null, attributeName, null, -1);
  }

  public AngularAttributeDescriptor(Project project, String attributeName, VirtualFile file, int offset) {
    super(attributeName);
    myProject = project;
    myFile = file;
    myOffset = offset;
  }

  @Override
  public PsiElement getDeclaration() {
    if (myProject != null) {
      PsiFile psiFile = PsiManager.getInstance(myProject).findFile(myFile);
      return psiFile != null ? psiFile.findElementAt(myOffset) : null;
    }
    return super.getDeclaration();
  }
}
