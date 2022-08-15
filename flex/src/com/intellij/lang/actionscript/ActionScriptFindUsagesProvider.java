// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript;

import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.findUsages.JSWordsScanner;
import com.intellij.lang.javascript.findUsages.JavaScriptFindUsagesProvider;

public class ActionScriptFindUsagesProvider extends JavaScriptFindUsagesProvider {
  @Override
  public WordsScanner getWordsScanner() {
    return new JSWordsScanner(DialectOptionHolder.ECMA_4);
  }
}
