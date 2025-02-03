package com.dmarcotte.handlebars.editor.templates;

import com.dmarcotte.handlebars.HbLanguage;
import com.intellij.codeInsight.template.emmet.generators.XmlZenCodingGeneratorImpl;
import com.intellij.lang.Language;

public final class HbEmmetGenerator extends XmlZenCodingGeneratorImpl {

  @Override
  protected boolean isMyLanguage(Language language) {
    return language.isKindOf(HbLanguage.INSTANCE);
  }
}
