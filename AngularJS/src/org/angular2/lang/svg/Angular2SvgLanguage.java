// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg;

import com.intellij.javascript.web.lang.html.WebFrameworkHtmlDialect;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.NotNull;

public class Angular2SvgLanguage extends WebFrameworkHtmlDialect {

  public static final Angular2SvgLanguage INSTANCE = new Angular2SvgLanguage();

  protected Angular2SvgLanguage() {
    super(Angular2HtmlLanguage.INSTANCE, "Angular2Svg");
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Angular SVG template";
  }
}
