// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSExpressionWithExpectedTypeHolder;
import com.intellij.psi.xml.XmlAttribute;
import org.angular2.Angular2InjectionUtils;
import org.angular2.lang.html.psi.Angular2HtmlBananaBoxBinding;
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Angular2Binding extends Angular2EmbeddedExpression, JSExpressionWithExpectedTypeHolder {

  @Override
  @Nullable
  JSExpression getExpression();

  @Nullable
  Angular2Quote getQuote();

  static @Nullable Angular2Binding get(@NotNull XmlAttribute attribute) {
    if (attribute instanceof Angular2HtmlPropertyBinding) {
      return ((Angular2HtmlPropertyBinding)attribute).getBinding();
    }
    else if (attribute instanceof Angular2HtmlBananaBoxBinding) {
      return ((Angular2HtmlBananaBoxBinding)attribute).getBinding();
    }
    return Angular2InjectionUtils.findInjectedAngularExpression(attribute, Angular2Binding.class);
  }
}
