// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.lexer;

import com.intellij.lexer.FlexAdapter;

public class DroolsFlexLexer extends FlexAdapter {
  public DroolsFlexLexer() {
    super(new _DroolsLexer(null));
  }
}
