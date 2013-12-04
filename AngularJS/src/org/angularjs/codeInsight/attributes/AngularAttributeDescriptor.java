package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.index.AngularDirectivesIndex;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ArrayUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularAttributeDescriptor extends AnyXmlAttributeDescriptor {
  protected final Project myProject;
  private final ID<String, Void> myIndex;

  public AngularAttributeDescriptor(final Project project, String attributeName, final ID<String, Void> index) {
    super(attributeName);
    myProject = project;
    myIndex = index;
  }

  @Override
  public PsiElement getDeclaration() {
    return AngularIndexUtil.resolve(myProject, AngularDirectivesIndex.INDEX_ID, getName());
  }

  @Override
  public String[] getEnumeratedValues() {
    if (myProject == null || myIndex == null) return ArrayUtil.EMPTY_STRING_ARRAY;
    return ArrayUtil.toStringArray(FileBasedIndex.getInstance().getAllKeys(myIndex, myProject));
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
