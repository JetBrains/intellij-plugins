package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.indexing.ID;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.index.AngularDirectivesDocIndex;
import org.angularjs.index.AngularDirectivesIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularAttributeDescriptor extends BasicXmlAttributeDescriptor implements XmlAttributeDescriptorEx {
  protected final Project myProject;
  private final String myAttributeName;
  private final ID<String, Void> myIndex;

  public AngularAttributeDescriptor(final Project project, String attributeName, final ID<String, Void> index) {
    myProject = project;
    myAttributeName = attributeName;
    myIndex = index;
  }

  @Override
  public PsiElement getDeclaration() {
    final String name = DirectiveUtil.normalizeAttributeName(getName());
    final JSNamedElementProxy declaration = AngularIndexUtil.resolve(myProject, AngularDirectivesIndex.INDEX_ID, name);
    return declaration != null ? declaration :
           AngularIndexUtil.resolve(myProject, AngularDirectivesDocIndex.INDEX_ID, getName());
  }

  @Override
  public String getName() {
    return myAttributeName;
  }

  @Override
  public void init(PsiElement element) {}

  @Override
  public Object[] getDependences() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  @Override
  public boolean isRequired() {
    return false;
  }

  @Override
  public boolean hasIdType() {
    return false;
  }

  @Override
  public boolean hasIdRefType() {
    return false;
  }

  @Override
  public boolean isEnumerated() {
    return myIndex != null;
  }

  @Override
  public boolean isFixed() {
    return false;
  }

  @Override
  public String getDefaultValue() {
    return null;
  }

  @Override
  public String[] getEnumeratedValues() {
    if (myProject == null || myIndex == null) return ArrayUtil.EMPTY_STRING_ARRAY;
    return ArrayUtil.toStringArray(AngularIndexUtil.getAllKeys(myIndex, myProject));
  }

  @Override
  protected PsiElement getEnumeratedValueDeclaration(XmlElement xmlElement, String value) {
    if (myIndex != null) {
      return AngularIndexUtil.resolve(xmlElement.getProject(), myIndex, value);
    }
    return super.getEnumeratedValueDeclaration(xmlElement, value);
  }

  @Nullable
  @Override
  public String handleTargetRename(@NotNull @NonNls String newTargetName) {
    return DirectiveUtil.getAttributeName(newTargetName);
  }
}
