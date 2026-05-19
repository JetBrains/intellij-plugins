package com.intellij.lang.javascript.linter.eslint.importer;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.util.PairConsumer;
import com.intellij.util.PairProcessor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class EslintJsSettingsConverter implements EslintSettingsConverter {

  private static final Set<String> TYPESCRIPT_ESLINT_PLUGINS = ContainerUtil.newHashSet("@typescript-eslint", "typescript");

  private final @NotNull PairConsumer<? super CommonCodeStyleSettings, ? super JSCodeStyleSettings> myApplier;
  private final @NotNull EslintRuleMapper.EslintConfig myConfigWrapper;
  private final @NotNull PairProcessor<? super CommonCodeStyleSettings, ? super JSCodeStyleSettings> myValidator;

  public EslintJsSettingsConverter(@NotNull EslintRuleMapper.EslintConfig configWrapper,
                                   @NotNull PairProcessor<? super CommonCodeStyleSettings, ? super JSCodeStyleSettings> validator,
                                   @NotNull PairConsumer<? super CommonCodeStyleSettings, ? super JSCodeStyleSettings> applier) {
    myConfigWrapper = configWrapper;
    myValidator = validator;
    myApplier = applier;
  }

  @Override
  public boolean inSync(@NotNull CodeStyleSettings settings) {
    boolean result = !myValidator.process(settings.getCommonSettings(JavascriptLanguage.INSTANCE),
                                          settings.getCustomSettings(JSCodeStyleSettings.class));
    if (result && ContainerUtil.find(myConfigWrapper.getPlugins(), TYPESCRIPT_ESLINT_PLUGINS::contains) != null) {
      result = !myValidator.process(settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT),
                                    settings.getCustomSettings(TypeScriptCodeStyleSettings.class));
    }
    return result;
  }

  @Override
  public void apply(@NotNull CodeStyleSettings settings) {
    myApplier.consume(settings.getCommonSettings(JavascriptLanguage.INSTANCE),
                      settings.getCustomSettings(JSCodeStyleSettings.class));
    if (ContainerUtil.find(myConfigWrapper.getPlugins(), TYPESCRIPT_ESLINT_PLUGINS::contains) != null) {
      myApplier.consume(settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT),
                        settings.getCustomSettings(TypeScriptCodeStyleSettings.class));
    }
  }
}
