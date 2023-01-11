package com.dmarcotte.handlebars.editor.templates;

import com.dmarcotte.handlebars.HbLanguage;
import com.intellij.codeInsight.template.emmet.generators.XmlZenCodingGeneratorImpl;
import com.intellij.lang.Language;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nullable;

public class HbEmmetGenerator extends XmlZenCodingGeneratorImpl {
  @Nullable
  @Override
  public Configurable createConfigurable() {
    return super.createConfigurable();
  }

  @Override
  protected boolean isMyLanguage(Language language) {
    return language.isKindOf(HbLanguage.INSTANCE);
  }
}
