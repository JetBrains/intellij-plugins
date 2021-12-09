// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.files;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Created by Lera Nikolaenko
 */
public final class CfmlFileViewProvider extends MultiplePsiFilesPerDocumentFileViewProvider implements TemplateLanguageFileViewProvider {
  private static final Set<Language> ourRelevantLanguages = Set.of(HTMLLanguage.INSTANCE, CfmlLanguage.INSTANCE);

  public CfmlFileViewProvider(final PsiManager manager, final VirtualFile virtualFile, final boolean physical) {
    super(manager, virtualFile, physical);
  }

  @Override
  @NotNull
  public Language getBaseLanguage() {
    return CfmlLanguage.INSTANCE;
  }

  @Override
  @NotNull
  public Set<Language> getLanguages() {
    return ourRelevantLanguages;
  }

  @Override
  @Nullable
  protected PsiFile createFile(@NotNull final Language lang) {
    if (lang == getTemplateDataLanguage()) {
      PsiFileImpl file = (PsiFileImpl)LanguageParserDefinitions.INSTANCE.forLanguage(HTMLLanguage.INSTANCE).createFile(this);
      file.setContentElementType(CfmlElementTypes.TEMPLATE_DATA);
      return file;
    }
    if (lang == CfmlUtil.getSqlLanguage()) {
      final PsiFileImpl file = (PsiFileImpl)LanguageParserDefinitions.INSTANCE.forLanguage(lang).createFile(this);
      file.setContentElementType(CfmlElementTypes.SQL_DATA);
      return file;
    }

    if (lang == getBaseLanguage()) {
      return LanguageParserDefinitions.INSTANCE.forLanguage(lang).createFile(this);
    }
    return null;
  }

  @NotNull
  @Override
  protected CfmlFileViewProvider cloneInner(@NotNull final VirtualFile copy) {
    return new CfmlFileViewProvider(getManager(), copy, false);
  }

  @Override
  @NotNull
  public Language getTemplateDataLanguage() {
    return HTMLLanguage.INSTANCE;
  }
}

