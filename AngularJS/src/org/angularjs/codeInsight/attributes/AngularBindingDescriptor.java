package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSField;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.types.primitives.JSStringType;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.NotNullFunction;
import com.intellij.util.NullableFunction;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularBindingDescriptor extends AngularAttributeDescriptor {
  public static final String INPUT = "Input";
  public static final NotNullFunction<Pair<PsiElement, String>, XmlAttributeDescriptor> FACTORY = AngularBindingDescriptor::createBinding;
  public static final NullableFunction<Pair<PsiElement, String>, XmlAttributeDescriptor> FACTORY2 = AngularBindingDescriptor::createOneTimeBinding;

  public AngularBindingDescriptor(PsiElement element,
                                  String attributeName) {
    super(element.getProject(), attributeName, null, element);
  }

  public static XmlAttributeDescriptor[] getBindingDescriptors(JSImplicitElement declaration) {
    return ArrayUtil.mergeArrays(getFieldBasedDescriptors(declaration, INPUT, FACTORY),
                                 getFieldBasedDescriptors(declaration, INPUT, FACTORY2));
  }

  @NotNull
  private static AngularBindingDescriptor createBinding(Pair<PsiElement, String> dom) {
    return new AngularBindingDescriptor(dom.first, "[" + dom.second + "]");
  }

  @Nullable
  private static AngularBindingDescriptor createOneTimeBinding(Pair<PsiElement, String> dom) {
    PsiElement element = dom.first;
    final JSType type = element instanceof JSFunction ? ((JSFunction)element).getReturnType() :
                        element instanceof JSField ? ((JSField)element).getType() :
                        null;

    return type instanceof JSStringType ? new AngularBindingDescriptor(element, dom.second) : null;
  }

  @Nullable
  @Override
  public String handleTargetRename(@NotNull @NonNls String newTargetName) {
    return "[" + newTargetName + "]";
  }
}
