// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html;

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect;
import org.jetbrains.annotations.NotNull;

public class Angular2HtmlLanguage extends WebFrameworkHtmlDialect {
  public static final Angular2HtmlLanguage INSTANCE = new Angular2HtmlLanguage();

  private Angular2HtmlLanguage() {
    super("Angular2Html");
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Angular HTML template";
  }
}
