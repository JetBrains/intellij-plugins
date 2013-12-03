package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.index.AngularControllerIndex;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ArrayUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class ControllerAttributeDescriptor extends AngularAttributeDescriptor {
  public ControllerAttributeDescriptor(final Project project) {
    super(project, "ng-controller");
  }

  @Override
  public String[] getEnumeratedValues() {
    if (myProject == null) return ArrayUtil.EMPTY_STRING_ARRAY;
    return ArrayUtil.toStringArray(FileBasedIndex.getInstance().getAllKeys(AngularControllerIndex.INDEX_ID, myProject));
  }

  @Override
  public PsiReference[] getReferences(PsiElement element) {
    return new PsiReference[] {
      new ControllerReference((XmlAttributeValue)element)
    };
  }

  private class ControllerReference extends PsiReferenceBase<XmlAttributeValue> {
    public ControllerReference(XmlAttributeValue value) {
      super(value, value.getValueTextRange().shiftRight(1 - value.getTextOffset()));
    }

    @Nullable
    @Override
    public PsiElement resolve() {
      final XmlAttributeValue element = getElement();
      final String key = element.getValue();
      return AngularIndexUtil.resolve(element.getProject(), AngularControllerIndex.INDEX_ID, key);
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

      ControllerReference reference = (ControllerReference)o;

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
