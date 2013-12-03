package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.index.AngularDirectivesIndex;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import org.angularjs.index.AngularIndexUtil;

/**
 * @author Dennis.Ushakov
 */
public class AngularAttributeDescriptor extends AnyXmlAttributeDescriptor {
  protected final Project myProject;

  public AngularAttributeDescriptor(final Project project, String attributeName) {
    super(attributeName);
    myProject = project;
  }

  @Override
  public PsiElement getDeclaration() {
    return AngularIndexUtil.resolve(myProject, AngularDirectivesIndex.INDEX_ID, getName());
  }

  public PsiReference[] getReferences(PsiElement element) {
    return PsiReference.EMPTY_ARRAY;
  }
}
