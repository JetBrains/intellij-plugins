package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSField;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl;
import com.intellij.lang.javascript.psi.types.JSTypeContext;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations;
import com.intellij.lang.javascript.psi.types.primitives.JSStringType;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.NotNullFunction;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularBindingDescriptor extends AngularAttributeDescriptor {
  public static final JSType STRING_TYPE = new JSStringType(true, JSTypeSource.EXPLICITLY_DECLARED, JSTypeContext.INSTANCE);
  public static final String INPUT = "Input";
  public static final NotNullFunction<Pair<PsiElement, String>, XmlAttributeDescriptor> FACTORY = AngularBindingDescriptor::createBinding;
  public static final NullableFunction<Pair<PsiElement, String>, XmlAttributeDescriptor> FACTORY2 =
    AngularBindingDescriptor::createOneTimeBinding;

  public AngularBindingDescriptor(PsiElement element,
                                  String attributeName) {
    super(element.getProject(), attributeName, null, element);
  }

  public static List<XmlAttributeDescriptor> getBindingDescriptors(JSImplicitElement declaration) {
    return ContainerUtil.concat(getFieldBasedDescriptors(declaration, INPUT, FACTORY),
                                getFieldBasedDescriptors(declaration, INPUT, FACTORY2));
  }

  @NotNull
  private static AngularBindingDescriptor createBinding(Pair<PsiElement, String> dom) {
    PsiElement element = dom.first;
    if (element instanceof TypeScriptClass) {
      JSQualifiedNamedElement declaration = findMember((JSClass)element, dom.second);
      if (declaration != null) return createBinding(Pair.pair(declaration, dom.second));
    }
    return new AngularBindingDescriptor(dom.first, "[" + dom.second + "]");
  }

  @Nullable
  private static AngularBindingDescriptor createOneTimeBinding(Pair<PsiElement, String> dom) {
    PsiElement element = dom.first;
    if (element instanceof TypeScriptClass) {
      JSQualifiedNamedElement declaration = findMember((JSClass)element, dom.second);
      if (declaration != null) return createOneTimeBinding(Pair.pair(declaration, dom.second));
      return new AngularBindingDescriptor(element, dom.second);
    }
    final JSType type = expandStringLiteralTypes(element instanceof JSFunction ?
                                                 ((JSFunction)element).getReturnType() :
                                                 element instanceof JSField ? ((JSField)element).getType() :
                                                 null);

    return type != null && type.isDirectlyAssignableType(STRING_TYPE, null) ?
           new AngularBindingDescriptor(element, dom.second) : null;
  }

  @Contract("null->null")
  private static JSType expandStringLiteralTypes(@Nullable JSType type) {
    if (type == null) return null;

    type = TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(type);
    return type.transformTypeHierarchy(toApply -> toApply instanceof JSStringLiteralTypeImpl ? STRING_TYPE : toApply);
  }

  @Nullable
  @Override
  public String handleTargetRename(@NotNull @NonNls String newTargetName) {
    return "[" + newTargetName + "]";
  }
}
