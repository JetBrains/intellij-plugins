// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex;

import com.intellij.lang.xml.XMLLanguage;

public final class MxmlLanguage extends XMLLanguage {
  public static final MxmlLanguage INSTANCE = new MxmlLanguage();

  private MxmlLanguage() {
    super(XMLLanguage.INSTANCE, "Mxml");
  }
}
