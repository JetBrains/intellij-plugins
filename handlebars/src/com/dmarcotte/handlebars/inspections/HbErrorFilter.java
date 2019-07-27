// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.inspections;


import com.dmarcotte.handlebars.file.HbFileViewProvider;
import com.intellij.codeInsight.highlighting.TemplateLanguageErrorFilter;
import com.intellij.psi.tree.TokenSet;

import static com.dmarcotte.handlebars.parsing.HbTokenTypes.*;

public class HbErrorFilter extends TemplateLanguageErrorFilter {

  public HbErrorFilter() {
    super(TokenSet.create(OPEN, OPEN_PARTIAL, OPEN_BLOCK, OPEN_INVERSE), HbFileViewProvider.class, "HTML");
  }

}
