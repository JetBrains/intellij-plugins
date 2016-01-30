package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.NotNullFunction;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularBindingDescriptor extends AngularAttributeDescriptor {
  public static final String INPUT = "Input";
  public static final NotNullFunction<JSNamedElement, XmlAttributeDescriptor> FACTORY = new NotNullFunction<JSNamedElement, XmlAttributeDescriptor>() {
    @NotNull
    @Override
    public XmlAttributeDescriptor fun(JSNamedElement dom) {
      return createBinding(dom);
    }
  };
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
    return getFieldBasedDescriptors(declaration, INPUT, FACTORY);
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
