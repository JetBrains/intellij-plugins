package com.dmarcotte.handlebars.editor.templates;

import com.dmarcotte.handlebars.HbLanguage;
import com.intellij.codeInsight.template.emmet.generators.XmlZenCodingGeneratorImpl;
import com.intellij.lang.Language;
import com.intellij.openapi.options.UnnamedConfigurable;
import org.jetbrains.annotations.Nullable;

public class HbEmmetGenerator extends XmlZenCodingGeneratorImpl {
  @Nullable
  @Override
  public UnnamedConfigurable createConfigurable() {
    return null;
  }

  @Override
  protected boolean isMyLanguage(Language language) {
    return language.is(HbLanguage.INSTANCE);
  }
}
