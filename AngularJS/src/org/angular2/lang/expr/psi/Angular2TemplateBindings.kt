// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi;

import com.intellij.lang.javascript.psi.JSEvaluableElement;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ObjectUtils;
import org.angular2.Angular2InjectionUtils;
import org.angular2.lang.expr.psi.impl.Angular2EmptyTemplateBindings;
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings;
import org.angular2.lang.types.Angular2TypeUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @see Angular2HtmlTemplateBindings
 * @see Angular2TemplateBinding
 */
public interface Angular2TemplateBindings extends Angular2EmbeddedExpression, JSEvaluableElement {

  @NotNull
  String getTemplateName();

  Angular2TemplateBinding @NotNull [] getBindings();

  @Override
  default boolean evaluate(@NotNull JSEvaluateContext evaluateContext, @NotNull JSTypeProcessor typeProcessor) {
    JSType type = Angular2TypeUtils.getTemplateBindingsContextType(this);
    if (type != null) typeProcessor.process(type, evaluateContext);
    return true;
  }

  static @NotNull Angular2TemplateBindings get(@NotNull XmlAttribute attribute) {
    if (attribute instanceof Angular2HtmlTemplateBindings) {
      return ((Angular2HtmlTemplateBindings)attribute).getBindings();
    }
    assert attribute.getName().startsWith("*");
    return ObjectUtils.notNull(Angular2InjectionUtils.findInjectedAngularExpression(attribute, Angular2TemplateBindings.class),
                               () -> new Angular2EmptyTemplateBindings(attribute, attribute.getName().substring(1)));
  }
}
