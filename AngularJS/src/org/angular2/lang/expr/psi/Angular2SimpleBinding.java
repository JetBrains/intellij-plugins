// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSStatement;
import org.jetbrains.annotations.Nullable;

public interface Angular2SimpleBinding extends JSStatement {

  @Nullable
  JSExpression getExpression();

  @Nullable
  Angular2Quote getQuote();

}
