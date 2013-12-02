package org.angularjs.codeInsight.attributes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;

/**
 * @author Dennis.Ushakov
 */
public class AngularAttributeDescriptor extends AnyXmlAttributeDescriptor {
  protected final Project myProject;
  private final VirtualFile myFile;
  private final int myOffset;

  public AngularAttributeDescriptor(Project project, String attributeName, VirtualFile file, int offset) {
    super(attributeName);
    myProject = project;
    myFile = file;
    myOffset = offset;
  }

  @Override
  public PsiElement getDeclaration() {
    if (myProject != null && myFile != null) {
      PsiFile psiFile = PsiManager.getInstance(myProject).findFile(myFile);
      return psiFile != null ? psiFile.findElementAt(myOffset) : null;
    }
    return super.getDeclaration();
  }

  public PsiReference[] getReferences(PsiElement element) {
    return PsiReference.EMPTY_ARRAY;
  }
}
