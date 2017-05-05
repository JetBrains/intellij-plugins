package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiPresentableMetaData;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import icons.AngularJSIcons;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.index.AngularDirectivesDocIndex;
import org.angularjs.index.AngularDirectivesIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularAttributeDescriptor extends BasicXmlAttributeDescriptor implements XmlAttributeDescriptorEx, PsiPresentableMetaData {
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
                                                                  NullableFunction<Pair<PsiElement, String>, XmlAttributeDescriptor> factory) {
    final JSClass clazz = PsiTreeUtil.getStubOrPsiParentOfType(declaration, JSClass.class);
    if (clazz != null) {
      JSField[] fields = clazz.getFields();
      final List<XmlAttributeDescriptor> result = new ArrayList<>(fields.length);
      for (JSField field : fields) {
        String decoratedName = getDecoratedName(field, decorator);
        if (decoratedName == null) continue;
        ContainerUtil.addIfNotNull(result, factory.fun(Pair.create(field, decoratedName)));
      }
      for (JSFunction function : clazz.getFunctions()) {
        String decoratedName = getDecoratedName(function, decorator);
        if (decoratedName == null) continue;
        ContainerUtil.addIfNotNull(result, factory.fun(Pair.create(function, decoratedName)));
      }
      return result.toArray(new XmlAttributeDescriptor[result.size()]);
    }
    return EMPTY;
  }

  private static String getDecoratedName(JSAttributeListOwner field, String name) {
    final JSAttributeList list = field.getAttributeList();
    if (list != null) {
      for (PsiElement candidate : list.getChildren()) {
        if (candidate instanceof ES6Decorator) {
          final PsiElement child = candidate.getLastChild();
          if (child instanceof JSCallExpression) {
            final JSExpression expression = ((JSCallExpression)child).getMethodExpression();
            if (expression instanceof JSReferenceExpression &&
                JSSymbolUtil.isAccurateReferenceExpressionName((JSReferenceExpression)expression, name)) {
              JSExpression[] arguments = ((JSCallExpression)child).getArguments();
              if (arguments.length > 0 && arguments[0] instanceof JSLiteralExpression) {
                Object value = ((JSLiteralExpression)arguments[0]).getValue();
                if (value instanceof String) return (String)value;
              }
              return field.getName();
            }
          }
        }
      }
    }

    return null;
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

  @NotNull
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

  @Override
  public String getTypeName() {
    return null;
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return AngularJSIcons.Angular2;
  }
}
