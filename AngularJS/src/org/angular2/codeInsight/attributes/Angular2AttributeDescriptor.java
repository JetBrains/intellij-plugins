// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Consumer;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angular2.codeInsight.metadata.AngularDirectiveMetadata;
import org.angular2.codeInsight.metadata.AngularDirectiveMetadata.PropertyInfo;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlReference;
import org.angular2.lang.html.psi.Angular2HtmlVariable;
import org.angularjs.codeInsight.attributes.AngularAttributeDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class Angular2AttributeDescriptor extends AngularAttributeDescriptor {

  public Angular2AttributeDescriptor(final Project project,
                                     String attributeName,
                                     final StubIndexKey<String, JSImplicitElementProvider> index,
                                     PsiElement element) {
    super(project, attributeName, index, Collections.singletonList(element));
  }

  @NotNull
  public static List<XmlAttributeDescriptor> getFieldBasedDescriptors(JSImplicitElement declaration,
                                                                      String decorator,
                                                                      NullableFunction<PropertyInfo, XmlAttributeDescriptor> factory) {
    final List<XmlAttributeDescriptor> result = new ArrayList<>();
    processFieldBasedDescriptors(declaration, decorator, info ->
      ContainerUtil.addIfNotNull(result, factory.fun(info)));
    return result;
  }

  public static void processFieldBasedDescriptors(JSImplicitElement declaration,
                                                  String decorator,
                                                  Consumer<? super PropertyInfo> processor) {
    AngularDirectiveMetadata metadata = AngularDirectiveMetadata.create(declaration);
    if ("Input".equals(decorator)) {
      metadata.getInputs().forEach(info -> processor.consume(info));
    }
    else if ("Output".equals(decorator)) {
      metadata.getOutputs().forEach(info -> processor.consume(info));
    }
    else {
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
}
