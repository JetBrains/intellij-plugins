// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg;

import com.intellij.javascript.web.html.WebFrameworkHtmlFileType;
import org.angularjs.AngularJSBundle;
import org.jetbrains.annotations.NotNull;

public class Angular2SvgFileType extends WebFrameworkHtmlFileType {

  public static final Angular2SvgFileType INSTANCE = new Angular2SvgFileType();

  private Angular2SvgFileType() {
    super(Angular2SvgLanguage.INSTANCE, "Angular2Svg", "svg");
  }

  @NotNull
  @Override
  public String getDescription() {
    return AngularJSBundle.message("filetype.angular2svg.description");
  }
}
