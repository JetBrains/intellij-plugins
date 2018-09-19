// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript;

import com.intellij.lang.javascript.liveTemplates.JSStatementContextTypeBase;

public class ActionScriptStatementContextType extends JSStatementContextTypeBase {
  private ActionScriptStatementContextType() {
    super("AS_STATEMENT", ActionScriptCodeContextType.class);
  }
}
