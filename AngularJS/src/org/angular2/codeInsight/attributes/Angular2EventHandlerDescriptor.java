// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.util.NotNullFunction;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2EventHandlerDescriptor extends Angular2AttributeDescriptor {
  public static final String OUTPUT = "Output";
  public static final NotNullFunction<Pair<PsiElement, String>, XmlAttributeDescriptor> FACTORY = Angular2EventHandlerDescriptor::createEventHandler;

  public Angular2EventHandlerDescriptor(PsiElement element,
                                        String attributeName) {
    super(element.getProject(), attributeName, null, element);
  }

  public static List<XmlAttributeDescriptor> getEventHandlerDescriptors(JSImplicitElement declaration) {
    return getFieldBasedDescriptors(declaration, OUTPUT, FACTORY);
  }

  @NotNull
  private static Angular2EventHandlerDescriptor createEventHandler(Pair<PsiElement, String> dom) {
    PsiElement element = dom.first;
    if (element instanceof TypeScriptClass) {
      JSQualifiedNamedElement declaration = findMember((JSClass)element, dom.second);
      if (declaration != null) return createEventHandler(Pair.pair(declaration, dom.second));
    }
    return new Angular2EventHandlerDescriptor(dom.first, "(" + dom.second + ")");
  }

  @Nullable
  @Override
  public String handleTargetRename(@NotNull @NonNls String newTargetName) {
    return "(" + newTargetName + ")";
  }
}
