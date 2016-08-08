package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES7Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.NotNullFunction;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.index.AngularDirectivesDocIndex;
import org.angularjs.index.AngularDirectivesIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularAttributeDescriptor extends BasicXmlAttributeDescriptor implements XmlAttributeDescriptorEx {
  protected final Project myProject;
  private final String myAttributeName;
  private final StubIndexKey<String, JSImplicitElementProvider> myIndex;

  public AngularAttributeDescriptor(final Project project, String attributeName, final StubIndexKey<String, JSImplicitElementProvider> index) {
    myProject = project;
    myAttributeName = attributeName;
    myIndex = index;
  }

  public static XmlAttributeDescriptor[] getFieldBasedDescriptors(JSImplicitElement declaration,
                                                                  String decorator,
                                                                  NotNullFunction<JSNamedElement, XmlAttributeDescriptor> factory) {
    final JSClass clazz = PsiTreeUtil.getParentOfType(declaration, JSClass.class);
    if (clazz != null) {
      JSVariable[] fields = clazz.getFields();
      final List<XmlAttributeDescriptor> result = new ArrayList<>(fields.length);
      for (JSVariable field : fields) {
        if (!hasDecorator(field, decorator)) continue;
        result.add(factory.fun(field));
      }
      for (JSFunction function : clazz.getFunctions()) {
        if (!hasDecorator(function, decorator)) continue;
        if (function.isSetProperty()) {
          result.add(factory.fun(function));
        }
      }
      return result.toArray(new XmlAttributeDescriptor[result.size()]);
    }
    return EMPTY;
  }

  private static boolean hasDecorator(JSAttributeListOwner field, String name) {
    final JSAttributeList list = field.getAttributeList();
    if (list != null) {
      for (PsiElement candidate : list.getChildren()) {
        if (candidate instanceof ES7Decorator) {
          final PsiElement child = candidate.getLastChild();
          if (child instanceof JSCallExpression) {
            final JSExpression expression = ((JSCallExpression)child).getMethodExpression();
            if (expression instanceof JSReferenceExpression &&
                JSSymbolUtil.isAccurateReferenceExpressionName((JSReferenceExpression)expression, name)) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  @NotNull
  public static XmlAttributeDescriptor[] getFieldBasedDescriptors(JSImplicitElement declaration) {
    return ArrayUtil.mergeArrays(AngularBindingDescriptor.getBindingDescriptors(declaration),
                                 AngularEventHandlerDescriptor.getEventHandlerDescriptors(declaration));
  }

  @Override
  public PsiElement getDeclaration() {
    final String name = DirectiveUtil.normalizeAttributeName(getName());
    final JSImplicitElement declaration = AngularIndexUtil.resolve(myProject, AngularDirectivesIndex.KEY, name);
    return declaration != null ? declaration :
           AngularIndexUtil.resolve(myProject, AngularDirectivesDocIndex.KEY, getName());
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
    return xmlElement;
  }

  @Nullable
  @Override
  public String handleTargetRename(@NotNull @NonNls String newTargetName) {
    return newTargetName;
  }
}
