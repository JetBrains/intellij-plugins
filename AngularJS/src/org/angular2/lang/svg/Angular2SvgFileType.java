// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg;

import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.Angular2HtmlFileType;
import org.jetbrains.annotations.NotNull;

public class Angular2SvgFileType extends Angular2HtmlFileType {

  public static final Angular2SvgFileType INSTANCE = new Angular2SvgFileType();

  protected Angular2SvgFileType() {
    super(Angular2SvgLanguage.INSTANCE);
  }

  @Override
  public @NotNull String getName() {
    return "Angular2Svg";
  }

  @Override
  public @NotNull String getDescription() {
    return Angular2Bundle.message("angular.description.angular-svg-template");
  }
}
