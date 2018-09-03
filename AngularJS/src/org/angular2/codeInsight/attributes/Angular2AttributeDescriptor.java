// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angular2.codeInsight.metadata.AngularDirectiveMetadata;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlReference;
import org.angular2.lang.html.psi.Angular2HtmlVariable;
import org.angularjs.codeInsight.attributes.AngularAttributeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Dennis.Ushakov
 */
public class Angular2AttributeDescriptor extends AngularAttributeDescriptor {

  public Angular2AttributeDescriptor(final Project project,
                                     String attributeName,
                                     final StubIndexKey<String, JSImplicitElementProvider> index,
                                     PsiElement element) {
    super(project, attributeName, index, element);
  }

  @NotNull
  public static List<XmlAttributeDescriptor> getFieldBasedDescriptors(JSImplicitElement declaration,
                                                                      String decorator,
                                                                      NullableFunction<Pair<PsiElement, String>, XmlAttributeDescriptor> factory) {
    final List<XmlAttributeDescriptor> result = new ArrayList<>();
    processFieldBasedDescriptors(declaration, decorator, (element, name) ->
      ContainerUtil.addIfNotNull(result, factory.fun(Pair.create(element, name))));
    return result;
  }

  public static void processFieldBasedDescriptors(JSImplicitElement declaration,
                                                                      String decorator,
                                                                      BiConsumer<PsiElement, String> processor) {
    AngularDirectiveMetadata metadata = AngularDirectiveMetadata.create(declaration);
    if ("Input".equals(decorator)) {
      metadata.getInputs().forEach(info -> processor.accept(info.source, info.name));
    } else if ("Output".equals(decorator)) {
      metadata.getOutputs().forEach(info -> processor.accept(info.source, info.name));
    } else {
      throw new IllegalArgumentException(decorator);
    }
  }

  public static List<XmlAttributeDescriptor> getExistingVarsAndRefsDescriptors(XmlTag context) {
    List<XmlAttributeDescriptor> result = new ArrayList<>();
    context.acceptChildren(new Angular2HtmlElementVisitor() {
      @Override
      public void visitVariable(Angular2HtmlVariable variable) {
        result.add(new Angular2AttributeDescriptor(context.getProject(), variable.getName(), null, variable.getNameElement()));
      }

      @Override
      public void visitReference(Angular2HtmlReference reference) {
        result.add(new Angular2AttributeDescriptor(context.getProject(), reference.getName(), null, reference.getNameElement()));
      }
    });
    return result;
  }

  @NotNull
  public static List<XmlAttributeDescriptor> getFieldBasedDescriptors(JSImplicitElement declaration) {
    return ContainerUtil.concat(Angular2BindingDescriptor.getBindingDescriptors(declaration),
                                Angular2EventHandlerDescriptor.getEventHandlerDescriptors(declaration));
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

}
