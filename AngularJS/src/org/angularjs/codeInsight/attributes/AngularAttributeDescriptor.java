package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.index.AngularDirectivesIndex;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularAttributeDescriptor extends BasicXmlAttributeDescriptor {
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
    return AngularIndexUtil.resolve(myProject, AngularDirectivesIndex.INDEX_ID, getName());
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
    return ArrayUtil.toStringArray(FileBasedIndex.getInstance().getAllKeys(myIndex, myProject));
  }

  @Override
  protected PsiElement getEnumeratedValueDeclaration(XmlElement xmlElement, String value) {
    if (myIndex != null) {
      return AngularIndexUtil.resolve(xmlElement.getProject(), myIndex, value);
    }
    return super.getEnumeratedValueDeclaration(xmlElement, value);
  }

  public PsiReference[] getReferences(PsiElement element) {
    if (myIndex == null) return PsiReference.EMPTY_ARRAY;

    return new PsiReference[]{
      new AngularAttributeReference((XmlAttributeValue)element, myIndex)
    };
  }

  protected static class AngularAttributeReference extends PsiReferenceBase<XmlAttributeValue> {
    private final ID<String,Void> myIndex;

    public AngularAttributeReference(XmlAttributeValue value, final ID<String, Void> index) {
      super(value, value.getValueTextRange().shiftRight(1 - value.getTextOffset()));
      myIndex = index;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
      final XmlAttributeValue element = getElement();
      final String key = element.getValue();
      return AngularIndexUtil.resolve(element.getProject(), myIndex, key);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      AngularAttributeReference reference = (AngularAttributeReference)o;

      if (!getRangeInElement().equals(reference.getRangeInElement())) return false;
      if (!getElement().equals(reference.getElement())) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = getRangeInElement().hashCode();
      result = 31 * result + getElement().hashCode();
      return result;
    }
  }
}
