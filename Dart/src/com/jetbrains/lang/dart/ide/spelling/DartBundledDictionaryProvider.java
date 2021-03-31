// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.spelling;

import com.intellij.spellchecker.BundledDictionaryProvider;

public class DartBundledDictionaryProvider implements BundledDictionaryProvider {
  @Override
  public String[] getBundledDictionaries() {
    return new String[]{"/spelling/dart.dic"};
  }
}
