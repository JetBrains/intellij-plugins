package com.dmarcotte.handlebars.file;

import com.dmarcotte.handlebars.HbLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.LanguageSubstitutors;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.templateLanguages.ConfigurableTemplateLanguageFileViewProvider;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static com.dmarcotte.handlebars.parsing.HbTokenTypes.CONTENT;
import static com.dmarcotte.handlebars.parsing.HbTokenTypes.OUTER_ELEMENT_TYPE;

public class HbFileViewProvider extends MultiplePsiFilesPerDocumentFileViewProvider
  implements ConfigurableTemplateLanguageFileViewProvider {

  private final Language myBaseLanguage;
  private final Language myTemplateLanguage;

  private static final ConcurrentMap<String, TemplateDataElementType> TEMPLATE_DATA_TO_LANG = ContainerUtil.newConcurrentMap();

  private static TemplateDataElementType getTemplateDataElementType(Language lang) {
    TemplateDataElementType result = TEMPLATE_DATA_TO_LANG.get(lang.getID());

    if (result != null) return result;
    TemplateDataElementType created = new TemplateDataElementType("HB_TEMPLATE_DATA", lang, CONTENT, OUTER_ELEMENT_TYPE) {
      @Override
      protected void appendCurrentTemplateToken(@NotNull StringBuilder result,
                                                @NotNull CharSequence buf,
                                                @NotNull Lexer lexer,
                                                @NotNull RangeCollector collector) {
        String nextSequence = lexer.getTokenText();
        if (nextSequence.endsWith("=")) {
          //insert fake ="" for attributes inside html tags
          nextSequence += "\"\"";
          collector.addRangeToRemove(new TextRange(lexer.getTokenEnd(), lexer.getTokenEnd() + 2));
        }
        result.append(nextSequence);
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
    return myBaseLanguage;
  }

  @NotNull
  @Override
  public Language getTemplateDataLanguage() {
    return myTemplateLanguage;
  }

  @NotNull
  @Override
  public Set<Language> getLanguages() {
    return new THashSet<>(Arrays.asList(myBaseLanguage, getTemplateDataLanguage()));
  }

  @NotNull
  @Override
  protected MultiplePsiFilesPerDocumentFileViewProvider cloneInner(@NotNull VirtualFile virtualFile) {
    return new HbFileViewProvider(getManager(), virtualFile, false, myBaseLanguage, myTemplateLanguage);
  }

  @Override
  protected PsiFile createFile(@NotNull Language lang) {
    ParserDefinition parserDefinition = getDefinition(lang);
    if (parserDefinition == null) {
      return null;
    }

    if (lang.is(getTemplateDataLanguage())) {
      PsiFileImpl file = (PsiFileImpl)parserDefinition.createFile(this);
      file.setContentElementType(getTemplateDataElementType(getBaseLanguage()));
      return file;
    }
    else if (lang.isKindOf(getBaseLanguage())) {
      return parserDefinition.createFile(this);
    }
    else {
      return null;
    }
  }

  private ParserDefinition getDefinition(Language lang) {
    ParserDefinition parserDefinition;
    if (lang.isKindOf(getBaseLanguage())) {
      parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(lang.is(getBaseLanguage()) ? lang : getBaseLanguage());
    } else {
      parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(lang);
    }
    return parserDefinition;
  }
}

