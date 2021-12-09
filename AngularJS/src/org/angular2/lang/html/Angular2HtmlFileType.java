// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html;

import com.intellij.javascript.web.lang.html.WebFrameworkHtmlFileType;
import org.angularjs.AngularJSBundle;
import org.jetbrains.annotations.NotNull;

public final class Angular2HtmlFileType extends WebFrameworkHtmlFileType {

  public static final Angular2HtmlFileType INSTANCE = new Angular2HtmlFileType();

  private Angular2HtmlFileType() {
    super(Angular2HtmlLanguage.INSTANCE, "Angular2Html", "html");
  }

  @NotNull
  @Override
  public String getDescription() {
    return AngularJSBundle.message("filetype.angular2html.description");
  }
}
