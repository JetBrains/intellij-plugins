// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr;

import com.intellij.lang.DependentLanguage;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.JSLanguageFeature;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class Angular2Language extends JSLanguageDialect implements DependentLanguage {
  public static final Angular2Language INSTANCE = new Angular2Language();

  protected Angular2Language() {
    super("Angular2", new DialectOptionHolder("ANGULAR2", true) {
      @Override
      protected @NotNull Set<@NotNull JSLanguageFeature> defineFeatures() {
        return Collections.singleton(JSLanguageFeature.IMPORT_DECLARATIONS);
      }
    });
  }

  @Override
  public boolean isAtLeast(@NotNull JSLanguageDialect other) {
    return super.isAtLeast(other) || JavaScriptSupportLoader.TYPESCRIPT.isAtLeast(other);
  }
}
