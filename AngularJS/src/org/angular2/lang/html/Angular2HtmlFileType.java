// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.lang.Language;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;

public class Angular2HtmlFileType extends HtmlFileType {

  public static final Angular2HtmlFileType INSTANCE = new Angular2HtmlFileType();

  protected Angular2HtmlFileType() {
    super(Angular2HtmlLanguage.INSTANCE);
  }

  protected Angular2HtmlFileType(Language language) {
    super(language);
  }

  @Override
  public @NotNull String getName() {
    return "Angular2Html";
  }

  @Override
  public @NotNull String getDescription() {
    return Angular2Bundle.message("angular.description.angular-html-template");
  }
}
