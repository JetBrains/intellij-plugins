// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2HtmlLanguage extends HTMLLanguage {
  public static final Angular2HtmlLanguage INSTANCE = new Angular2HtmlLanguage();

  protected Angular2HtmlLanguage() {
    super(HTMLLanguage.INSTANCE, "Angular2Html");
  }

  @Override
  public @Nullable LanguageFileType getAssociatedFileType() {
    return Angular2HtmlFileType.INSTANCE;
  }

  @Override
  public @NotNull String getDisplayName() {
    return Angular2Bundle.message("angular.description.angular-html-template");
  }
}
