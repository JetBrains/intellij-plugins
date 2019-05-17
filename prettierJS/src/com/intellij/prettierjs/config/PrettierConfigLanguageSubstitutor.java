// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs.config;

import com.intellij.lang.javascript.linter.JSLinterConfigLangSubstitutor;
import com.intellij.prettierjs.PrettierUtil;

public class PrettierConfigLanguageSubstitutor extends JSLinterConfigLangSubstitutor {
  public PrettierConfigLanguageSubstitutor() {
    super(PrettierUtil.RC_FILE_NAME);
  }
}
