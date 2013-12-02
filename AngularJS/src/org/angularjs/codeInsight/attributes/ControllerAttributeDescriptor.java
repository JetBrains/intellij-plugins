package org.angularjs.codeInsight.attributes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ArrayUtil;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Dennis.Ushakov
 */
public class ControllerAttributeDescriptor extends AngularAttributeDescriptor {
  public ControllerAttributeDescriptor(Project project, VirtualFile file, int offset) {
    super(project, "ng-controller", file, offset);
  }

  @Override
  public String[] getEnumeratedValues() {
    if (myProject == null) return ArrayUtil.EMPTY_STRING_ARRAY;

    final Set<String> result = new HashSet<String>();
    for (AngularIndexUtil.Entry entry : AngularIndexUtil.collect(myProject, AngularJSIndexingHandler.CONTROLLER_KEY)) {
      result.add(entry.name);
    }
    return ArrayUtil.toStringArray(result);
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
      final AngularIndexUtil.Entry entry = AngularIndexUtil.resolve(element.getProject(), AngularJSIndexingHandler.CONTROLLER_KEY, key);
      if (entry == null) return null;

      PsiFile psiFile = element.getManager().findFile(entry.file);
      return psiFile != null ? psiFile.findElementAt(entry.offset) : null;
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
