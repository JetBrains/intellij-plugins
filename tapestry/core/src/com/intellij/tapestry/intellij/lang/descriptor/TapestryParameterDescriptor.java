package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaField;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Fedor.Korotkov
 */
public class TapestryParameterDescriptor extends BasicTapestryTagDescriptor {
  private final TapestryComponent myComponent;
  private final TapestryParameter myParameter;

  public TapestryParameterDescriptor(TapestryComponent component,
                                     @NotNull TapestryParameter parameter,
                                     @Nullable String namespacePrefix,
                                     TapestryNamespaceDescriptor descriptor) {
    super(namespacePrefix, descriptor);
    myComponent = component;
    myParameter = parameter;
  }

  @Override
  public String getDefaultName() {
    return getPrefixWithColon() + myParameter.getName();
  }

  @Override
  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return XmlAttributeDescriptor.EMPTY;
  }

  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String attributeName, @Nullable XmlTag context) {
    return null;
  }

  @Override
  public PsiElement getDeclaration() {
    IJavaField field = myParameter.getParameterField();
    if (field instanceof IntellijJavaField) { // class field name may be different from tag name
      return ((IntellijJavaField)field).getPsiField();
    }
    final PsiClass psiClass = ((IntellijJavaClassType)myComponent.getElementClass()).getPsiClass();
    return psiClass == null ? null : psiClass.findFieldByName(myParameter.getName(), true);
  }
}
