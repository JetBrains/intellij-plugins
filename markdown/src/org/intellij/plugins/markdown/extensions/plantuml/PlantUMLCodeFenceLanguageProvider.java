package org.intellij.plugins.markdown.extensions.plantuml;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.plugins.markdown.injection.CodeFenceLanguageProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlantUMLCodeFenceLanguageProvider implements CodeFenceLanguageProvider {
  private static final String PLANTUML = "plantuml";
  private static final String PUML = "puml";
  private static final List<String> PLANT_UML_LANGS = ContainerUtil.list(PLANTUML, PUML);

  @Nullable
  @Override
  public Language getLanguageByInfoString(@NotNull String infoString) {
    return PLANT_UML_LANGS.contains(infoString) ? PlantUMLLanguage.INSTANCE : null;
  }

  @NotNull
  @Override
  public List<LookupElement> getCompletionVariantsForInfoString(@NotNull CompletionParameters parameters) {
    return ContainerUtil.list(LookupElementBuilder.create(PUML));
  }
}