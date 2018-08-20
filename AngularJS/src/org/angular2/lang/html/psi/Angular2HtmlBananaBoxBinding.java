// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi;

import com.intellij.psi.xml.XmlAttribute;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Angular2HtmlBananaBoxBinding extends XmlAttribute {

  @NotNull
  String getPropertyName();

  @Nullable
  Angular2Binding getBinding();
}
