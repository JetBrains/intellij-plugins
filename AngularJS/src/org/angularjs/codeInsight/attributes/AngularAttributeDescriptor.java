package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiPresentableMetaData;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
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
import org.angularjs.index.AngularDecoratorsIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularAttributeDescriptor extends BasicXmlAttributeDescriptor implements XmlAttributeDescriptorEx, PsiPresentableMetaData {
  protected final Project myProject;
  protected final PsiElement myElement;
  private final String myAttributeName;
  private final StubIndexKey<String, JSImplicitElementProvider> myIndex;

  /**
   * NativeScript compatibility
   * @deprecated to be removed in 2017.3
   */
  public AngularAttributeDescriptor(final Project project,
                                    String attributeName,
                                    final StubIndexKey<String, JSImplicitElementProvider> index) {
    this(project, attributeName, index, null);
  }

  public AngularAttributeDescriptor(final Project project,
                                    String attributeName,
                                    final StubIndexKey<String, JSImplicitElementProvider> index,
                                    PsiElement element) {
    myProject = project;
    myAttributeName = attributeName;
    myIndex = index;
    myElement = element;
  }

  public static XmlAttributeDescriptor[] getFieldBasedDescriptors(JSImplicitElement declaration,
                                                                  String decorator,
                                                                  NullableFunction<Pair<PsiElement, String>, XmlAttributeDescriptor> factory) {
    final JSClass clazz = PsiTreeUtil.getContextOfType(declaration, JSClass.class);
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
    if (!DialectDetector.isTypeScript(declaration)) {
      return getCompiledFieldBasedDescriptors(declaration, decorator, factory);
    }
    return EMPTY;
  }

  @NotNull
  private static XmlAttributeDescriptor[] getCompiledFieldBasedDescriptors(JSImplicitElement declaration,
                                                                             String decorator,
                                                                             NullableFunction<Pair<PsiElement, String>, XmlAttributeDescriptor> factory) {
    Project project = declaration.getProject();
    Collection<String> keys = StubIndex.getInstance().getAllKeys(AngularDecoratorsIndex.KEY, project);
    GlobalSearchScope scope = GlobalSearchScope.fileScope(declaration.getContainingFile());
    JSAssignmentExpression context = PsiTreeUtil.getContextOfType(declaration, JSAssignmentExpression.class);
    if (context == null) return EMPTY;

    final List<XmlAttributeDescriptor> result = new ArrayList<>();
    for (String key : keys) {
      StubIndex.getInstance().processElements(AngularDecoratorsIndex.KEY, key, project, scope, JSImplicitElementProvider.class, (provider) -> {
        JSElementIndexingData data = provider.getIndexingData();
        Collection<JSImplicitElement> elements = data != null ? data.getImplicitElements() : null;
        if (elements != null) {
          for (JSImplicitElement element : elements) {
            if (key.equals(element.getName())) {
              String type = element.getTypeString();
              if (type != null && type.startsWith(decorator + ";") && inContext(context, element)) {
                ContainerUtil.addIfNotNull(result, factory.fun(Pair.create(element, element.getName())));
              }
            }
          }
        }
        return true;
      });
    }
    return result.toArray(new XmlAttributeDescriptor[result.size()]);
  }

  private static boolean inContext(@NotNull JSAssignmentExpression context, JSImplicitElement element) {
    JSAssignmentExpression elementContext = PsiTreeUtil.getContextOfType(element, JSAssignmentExpression.class);
    if (elementContext != null) {
      JSDefinitionExpression declarationDef = context.getDefinitionExpression();
      JSDefinitionExpression elementDef = elementContext.getDefinitionExpression();
      if (declarationDef != null && elementDef != null &&
          Comparing.equal(declarationDef.getNamespace(), elementDef.getNamespace())) {
        return true;
      }
    }
    JSVariable declarationVar = PsiTreeUtil.getContextOfType(context, JSVariable.class);
    JSVariable elementVar = PsiTreeUtil.getContextOfType(element, JSVariable.class);
    return declarationVar != null && elementVar != null && declarationVar.isEquivalentTo(elementVar);
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

  @Override
  public PsiElement getDeclaration() {
    return myElement;
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
