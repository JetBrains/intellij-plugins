// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.stub;

import com.intellij.psi.stubs.StubElement;
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector;
import org.jetbrains.annotations.Nullable;

public interface Angular2HtmlNgContentSelectorStub extends StubElement<Angular2HtmlNgContentSelector> {

  @Nullable
  String getSelector();
}
