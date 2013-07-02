package com.dmarcotte.handlebars.file;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.LanguageSubstitutors;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.templateLanguages.ConfigurableTemplateLanguageFileViewProvider;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

public class HbFileViewProvider extends MultiplePsiFilesPerDocumentFileViewProvider
  implements ConfigurableTemplateLanguageFileViewProvider {

  private final Language myTemplateDataLanguage;

  public HbFileViewProvider(PsiManager manager, VirtualFile file, boolean physical) {
    this(manager, file, physical, getTemplateDataLanguage(manager, file));
  }

  public HbFileViewProvider(PsiManager manager, VirtualFile file, boolean physical, Language templateDataLanguage) {
    super(manager, file, physical);

    myTemplateDataLanguage = templateDataLanguage;
  }

  @Override
  public boolean supportsIncrementalReparse(@NotNull Language rootLanguage) {
    return false;
  }

  @NotNull
  private static Language getTemplateDataLanguage(PsiManager manager, VirtualFile file) {
    Language dataLang = TemplateDataLanguageMappings.getInstance(manager.getProject()).getMapping(file);
    if (dataLang == null) {
      dataLang = HbLanguage.getDefaultTemplateLang().getLanguage();
    }

    Language substituteLang = LanguageSubstitutors.INSTANCE.substituteLanguage(dataLang, file, manager.getProject());

    // only use a substituted language if it's templateable
    if (TemplateDataLanguageMappings.getTemplateableLanguages().contains(substituteLang)) {
      dataLang = substituteLang;
    }

    return dataLang;
  }

  @NotNull
  @Override
  public Language getBaseLanguage() {
    return HbLanguage.INSTANCE;
  }

  @NotNull
  @Override
  public Language getTemplateDataLanguage() {
    return myTemplateDataLanguage;
  }

  @NotNull
  @Override
  public Set<Language> getLanguages() {
    return new THashSet<Language>(Arrays.asList(new Language[]{HbLanguage.INSTANCE, myTemplateDataLanguage}));
  }

  @Override
  protected MultiplePsiFilesPerDocumentFileViewProvider cloneInner(VirtualFile virtualFile) {
    return new HbFileViewProvider(getManager(), virtualFile, false, myTemplateDataLanguage);
  }

  @Override
  protected PsiFile createFile(@NotNull Language lang) {
    ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(lang);
    if (parserDefinition == null) {
      return null;
    }

    if (lang == myTemplateDataLanguage) {
      PsiFileImpl file = (PsiFileImpl)parserDefinition.createFile(this);
      file.setContentElementType(
        new TemplateDataElementType("HB_TEMPLATE_DATA", myTemplateDataLanguage, HbTokenTypes.CONTENT, HbTokenTypes.OUTER_ELEMENT_TYPE));
      return file;
    }
    else if (lang == HbLanguage.INSTANCE) {
      return parserDefinition.createFile(this);
    }
    else {
      return null;
    }
  }
}

