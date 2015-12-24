package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularBindingDescriptor extends AngularAttributeDescriptor {
  private final PsiElement myElement;

  public AngularBindingDescriptor(PsiElement element,
                                  String attributeName) {
    super(element.getProject(), attributeName, null);
    myElement = element;
  }

  @Override
  public PsiElement getDeclaration() {
    return myElement;
  }

  public static XmlAttributeDescriptor[] getBindingDescriptors(JSImplicitElement declaration) {
    final JSClass clazz = PsiTreeUtil.getParentOfType(declaration, JSClass.class);
    if (clazz != null) {
      JSVariable[] fields = clazz.getFields();
      final List<XmlAttributeDescriptor> result = new ArrayList<XmlAttributeDescriptor>(fields.length);
      for (JSVariable field : fields) {
        result.add(createBinding(field));
      }
      for (JSFunction function : clazz.getFunctions()) {
        if (function.isSetProperty()) {
          result.add(createBinding(function));
        }
      }
      return result.toArray(new XmlAttributeDescriptor[result.size()]);
    }
    return EMPTY;
  }

  @NotNull
  private static AngularBindingDescriptor createBinding(PsiNamedElement field) {
    return new AngularBindingDescriptor(field, "[" + field.getName() + "]");
  }

  @Nullable
  @Override
  public String handleTargetRename(@NotNull @NonNls String newTargetName) {
    return "[" + newTargetName + "]";
  }
}
