// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.psi.impl.source.html.HtmlFileImpl;
import org.jetbrains.annotations.Nullable;

public interface Angular2Component extends Angular2Directive {

  @Nullable
  HtmlFileImpl getHtmlTemplate();
}
