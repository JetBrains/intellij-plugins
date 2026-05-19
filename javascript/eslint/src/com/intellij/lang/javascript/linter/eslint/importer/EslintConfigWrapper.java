package com.intellij.lang.javascript.linter.eslint.importer;

import com.intellij.application.options.CodeStyle;
import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.lang.javascript.formatter.JSCodeStyleUtil;
import com.intellij.lang.javascript.linter.eslint.EslintUtil;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.extensions.ExtensionPointUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ClearableLazyValue;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.intellij.util.ObjectUtils.tryCast;

public final class EslintConfigWrapper {
  private static final @NotNull ClearableLazyValue<Map<String, EslintRuleMapper>> RULES =
    ExtensionPointUtil.dropLazyValueOnChange(ClearableLazyValue.create(() -> createRules()),
                                             EslintRuleMappersFactory.EP_NAME, null);
  private static final Key<ParameterizedCachedValue<EslintConfigWrapper, PsiFile>> CACHE_KEY =
    Key.create("Eslint.Import.Code.Style.cache.key");
  private final @NotNull Map<@NotNull String, @NotNull EslintSettingsConverter> myConfigRules;

  private EslintConfigWrapper(final @NotNull JsonObject config) {
    myConfigRules = parseConfig(config);
  }

  public static @Nullable EslintConfigWrapper getForFile(@NotNull PsiFile psiFile) {
    if (!psiFile.isPhysical()) return createImporter(psiFile);
    return CachedValuesManager.getManager(psiFile.getProject()).getParameterizedCachedValue(
      psiFile, CACHE_KEY, param -> CachedValueProvider.Result.create(createImporter(param), param), false, psiFile);
  }

  private static @Nullable EslintConfigWrapper createImporter(@NotNull PsiFile psiFile) {
    final JsonObject rootObject = EslintUtil.getConfigRootObject(psiFile);
    return rootObject != null ? new EslintConfigWrapper(rootObject) : null;
  }

  public boolean hasDataToImport(@NotNull Project project) {
    final CodeStyleSettings settings = CodeStyle.getSettings(project);

    if (myConfigRules.isEmpty()) return false;
    return myConfigRules.values().stream()
      .anyMatch(convertor -> !convertor.inSync(settings));
  }

  public @NotNull Set<String> modifySettings(final @NotNull Project project) {
    final Set<String> modifiedRules = new HashSet<>();
    JSCodeStyleUtil.updateProjectCodeStyle(project, settings -> WriteAction.run(() -> {
      for (Map.Entry<String, EslintSettingsConverter> entry : myConfigRules.entrySet()) {
        final EslintSettingsConverter convertor = entry.getValue();
        if (!convertor.inSync(settings)) {
          convertor.apply(settings);
          modifiedRules.add(entry.getKey());
        }
      }
    }));

    return modifiedRules;
  }

  private static @NotNull Map<@NotNull String, @NotNull EslintSettingsConverter> parseConfig(final @NotNull JsonObject config) {
    final JsonProperty property = config.findProperty("rules");
    if (property == null) return Collections.emptyMap();
    final JsonObject rulesObject = tryCast(property.getValue(), JsonObject.class);
    if (rulesObject == null) return Collections.emptyMap();

    final Set<String> plugins = StreamEx.ofNullable(config.findProperty("plugins"))
      .map(JsonProperty::getValue)
      .select(JsonArray.class)
      .flatCollection(JsonArray::getValueList)
      .select(JsonStringLiteral.class)
      .map(JsonStringLiteral::getValue)
      .toSet();

    final EslintRuleMapper.EslintConfig configWrapper = new EslintRuleMapper.EslintConfig() {
      @Override
      public @NotNull JsonObject getConfigRoot() {
        return config;
      }

      @Override
      public @NotNull Set<String> getPlugins() {
        return plugins;
      }
    };

    final Map<String, EslintSettingsConverter> result = new HashMap<>();
    final List<JsonProperty> list = rulesObject.getPropertyList();
    for (JsonProperty rule : list) {
      final String name = StringUtil.unquoteString(rule.getName());
      if (StringUtil.isEmptyOrSpaces(name) || rule.getValue() == null) continue;
      result.put(name, getSettingsConvertor(name, rule, configWrapper));
    }

    return result;
  }

  private static @NotNull EslintSettingsConverter getSettingsConvertor(final @NotNull String name,
                                                                       final @NotNull JsonProperty rule,
                                                                       @NotNull EslintRuleMapper.EslintConfig configWrapper) {
    final EslintRuleMapper mapper = RULES.getValue().get(name);
    final JsonValue value = rule.getValue();
    assert value != null;
    if (mapper == null) return EslintSettingsConverter.SKIPPED;
    return mapper.parseSettings(value, configWrapper);
  }

  private static Map<String, EslintRuleMapper> createRules() {
    return EslintRuleMappersFactory.EP_NAME.getExtensionList().stream().flatMap(factory -> factory.createMappers().stream())
      .collect(Collectors.toMap(EslintRuleMapper::getName, Function.identity()));
  }
}
