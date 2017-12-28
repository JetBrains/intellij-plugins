package org.angularjs.codeInsight.attributes;

import com.intellij.json.psi.JsonFile;
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil;
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedItemProcessor;
import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.meta.PsiPresentableMetaData;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.NullableFunction;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import icons.AngularJSIcons;
import org.angularjs.codeInsight.metadata.AngularClass;
import org.angularjs.codeInsight.metadata.AngularField;
import org.angularjs.codeInsight.metadata.AngularMetadata;
import org.angularjs.codeInsight.metadata.AngularMetadataLoader;
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
    final JSClass context = PsiTreeUtil.getContextOfType(declaration, JSClass.class);
    if (context != null) {
      final List<XmlAttributeDescriptor> result = new ArrayList<>();
      JSClassUtils.processClassesInHierarchy(context, true, (clazz, typeSubstitutor, fromImplements) -> {
        for (JSField field : clazz.getFields()) {
          String decoratedName = getDecoratedName(field, decorator);
          if (decoratedName == null) continue;
          ContainerUtil.addIfNotNull(result, factory.fun(Pair.create(field, decoratedName)));
        }
        for (JSFunction function : clazz.getFunctions()) {
          String decoratedName = getDecoratedName(function, decorator);
          if (decoratedName == null) continue;
          ContainerUtil.addIfNotNull(result, factory.fun(Pair.create(function, decoratedName)));
        }
        return true;
      });
      return result.toArray(new XmlAttributeDescriptor[result.size()]);
    }
    if (declaration.getContainingFile() instanceof JsonFile) {
      return getCompiledFieldBasedDescriptors(declaration, decorator, factory);
    }
    return EMPTY;
  }

  @NotNull
  private static XmlAttributeDescriptor[] getCompiledFieldBasedDescriptors(JSImplicitElement declaration,
                                                                             String decorator,
                                                                             NullableFunction<Pair<PsiElement, String>, XmlAttributeDescriptor> factory) {
    VirtualFile metadataJson = declaration.getContainingFile().getVirtualFile();
    AngularMetadata metadata = AngularMetadataLoader.INSTANCE.load(metadataJson);
    VirtualFile definition = metadataJson.getParent().findChild(StringUtil.trimEnd(metadataJson.getName(), "metadata.json") + "d.ts");
    PsiFile file = definition != null ? declaration.getManager().findFile(definition) : null;
    final SmartList<XmlAttributeDescriptor> result = new SmartList<>();
    for (AngularClass directive : metadata.findDirectives(declaration.getName())) {
      PsiElement realDeclaration = declaration;
      if (file instanceof JSFile) {
        ResolveResultSink sink = new ResolveResultSink(file, directive.getName());
        ES6PsiUtil.processExportDeclarationInScope((JSFile)file, new TypeScriptQualifiedItemProcessor<>(sink, file));
        realDeclaration = sink.getResult() != null ? sink.getResult() : declaration;
      }
      AngularField[] fields = "Input".equals(decorator) ? directive.getInputs() : directive.getOutputs();
      for (AngularField field : fields) {
        ContainerUtil.addIfNotNull(result, factory.fun(Pair.create(realDeclaration, field.getName())));
      }
    }
    return result.toArray(EMPTY);
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
    return "id".equals(myAttributeName);
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
  protected static JSQualifiedNamedElement findMember(@NotNull JSClass element, @NotNull String name) {
    Ref<JSQualifiedNamedElement> result = Ref.create();
    JSClassUtils.processClassesInHierarchy(element, true, (clazz, typeSubstitutor, fromImplements) -> {
      result.set(clazz.findFieldByName(name));
      if (result.isNull()) {
        result.set(clazz.findFunctionByName(name));
      }
      return result.isNull();
    });
    return result.get();
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
