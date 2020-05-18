// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2SvgLanguage extends HTMLLanguage {
  public static final Angular2SvgLanguage INSTANCE = new Angular2SvgLanguage();

  protected Angular2SvgLanguage() {
    super(Angular2HtmlLanguage.INSTANCE, "Angular2Svg");
  }

  @Override
  public @Nullable LanguageFileType getAssociatedFileType() {
    return Angular2SvgFileType.INSTANCE;
  }

  @Override
  public @NotNull String getDisplayName() {
    return Angular2Bundle.message("angular.description.angular-svg-template");
  }
}
