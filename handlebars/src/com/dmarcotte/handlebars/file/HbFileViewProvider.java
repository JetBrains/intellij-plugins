// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.file;

import com.dmarcotte.handlebars.HbLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.LanguageSubstitutors;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.templateLanguages.ConfigurableTemplateLanguageFileViewProvider;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import com.intellij.psi.templateLanguages.TemplateDataModifications;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.dmarcotte.handlebars.parsing.HbTokenTypes.CONTENT;
import static com.dmarcotte.handlebars.parsing.HbTokenTypes.OUTER_ELEMENT_TYPE;

public class HbFileViewProvider extends MultiplePsiFilesPerDocumentFileViewProvider
  implements ConfigurableTemplateLanguageFileViewProvider {

  private final Language myBaseLanguage;
  private final Language myTemplateLanguage;

  private static final ConcurrentMap<String, TemplateDataElementType> TEMPLATE_DATA_TO_LANG = new ConcurrentHashMap<>();

  private static TemplateDataElementType getTemplateDataElementType(Language lang) {
    TemplateDataElementType result = TEMPLATE_DATA_TO_LANG.get(lang.getID());

    if (result != null) return result;
    TemplateDataElementType created = new TemplateDataElementType("HB_TEMPLATE_DATA", lang, CONTENT, OUTER_ELEMENT_TYPE) {
      @Override
      protected @NotNull TemplateDataModifications appendCurrentTemplateToken(int tokenEndOffset, @NotNull CharSequence tokenText) {
        if (StringUtil.endsWithChar(tokenText, '=')) {
          //insert fake ="" for attributes inside html tags
          return TemplateDataModifications.fromRangeToRemove(tokenEndOffset, "\"\"");
        }
        return super.appendCurrentTemplateToken(tokenEndOffset, tokenText);
      }
    };
    TemplateDataElementType prevValue = TEMPLATE_DATA_TO_LANG.putIfAbsent(lang.getID(), created);

    return prevValue == null ? created : prevValue;
  }


  public HbFileViewProvider(PsiManager manager, VirtualFile file, boolean physical, Language baseLanguage) {
    this(manager, file, physical, baseLanguage, getTemplateDataLanguage(manager, file));
  }

  public HbFileViewProvider(PsiManager manager, VirtualFile file, boolean physical, Language baseLanguage, Language templateLanguage) {
    super(manager, file, physical);
    myBaseLanguage = baseLanguage;
    myTemplateLanguage = templateLanguage;
  }

  @Override
  public boolean supportsIncrementalReparse(@NotNull Language rootLanguage) {
    return false;
  }

  private static @NotNull Language getTemplateDataLanguage(PsiManager manager, VirtualFile file) {
    Language dataLang = TemplateDataLanguageMappings.getInstance(manager.getProject()).getMapping(file);
    if (dataLang == null) {
      dataLang = HbLanguage.getDefaultTemplateLang().getLanguage();
    }

    Language substituteLang = LanguageSubstitutors.getInstance().substituteLanguage(dataLang, file, manager.getProject());

    // only use a substituted language if it's templateable
    if (TemplateDataLanguageMappings.getTemplateableLanguages().contains(substituteLang)) {
      dataLang = substituteLang;
    }

    return dataLang;
  }

  @Override
  public @NotNull Language getBaseLanguage() {
    return myBaseLanguage;
  }

  @Override
  public @NotNull Language getTemplateDataLanguage() {
    return myTemplateLanguage;
  }

  @Override
  public @NotNull Set<Language> getLanguages() {
    return ContainerUtil.set(myBaseLanguage, getTemplateDataLanguage());
  }

  @Override
  protected @NotNull MultiplePsiFilesPerDocumentFileViewProvider cloneInner(@NotNull VirtualFile virtualFile) {
    return new HbFileViewProvider(getManager(), virtualFile, false, myBaseLanguage, myTemplateLanguage);
  }

  @Override
  protected PsiFile createFile(@NotNull Language lang) {
    ParserDefinition parserDefinition = getDefinition(lang);
    if (parserDefinition == null) {
      return null;
    }

    if (lang.is(getTemplateDataLanguage())) {
      PsiFile file = parserDefinition.createFile(this);
      IElementType type = getContentElementType(lang);
      if (type != null) {
        ((PsiFileImpl)file).setContentElementType(type);
      }
      return file;
    }
    else if (lang.isKindOf(getBaseLanguage())) {
      return parserDefinition.createFile(this);
    }
    return null;
  }

  @Override
  public IElementType getContentElementType(@NotNull Language language) {
    if (language.is(getTemplateDataLanguage())) {
      return getTemplateDataElementType(getBaseLanguage());
    }
    return null;
  }

  private ParserDefinition getDefinition(Language lang) {
    ParserDefinition parserDefinition;
    if (lang.isKindOf(getBaseLanguage())) {
      parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(lang.is(getBaseLanguage()) ? lang : getBaseLanguage());
    }
    else {
      parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(lang);
    }
    return parserDefinition;
  }
}

