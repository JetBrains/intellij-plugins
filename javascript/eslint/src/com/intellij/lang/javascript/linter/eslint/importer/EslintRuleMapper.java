package com.intellij.lang.javascript.linter.eslint.importer;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonBooleanLiteral;
import com.intellij.json.psi.JsonLiteral;
import com.intellij.json.psi.JsonNumberLiteral;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.BeforeAfter;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.intellij.lang.javascript.linter.eslint.importer.EslintSettingsConverter.MISCONFIGURATION;

public abstract class EslintRuleMapper {
  private static final Set<String> TURNED_ON = Set.of("warn", "error");
  private static final @NonNls String ALWAYS = "always";
  private static final @NonNls String NEVER = "never";
  protected static final String BEFORE = "before";
  protected static final String AFTER = "after";
  private final String myName;

  protected EslintRuleMapper(@NotNull String name) {myName = name;}

  public final @NotNull String getName() {
    return myName;
  }

  protected abstract @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                             @NotNull EslintConfig eslintConfig);

  public @NotNull EslintSettingsConverter parseSettings(@NotNull JsonValue element,
                                                        @NotNull EslintConfig configWrapper) {
    JsonLiteral literal = ObjectUtils.tryCast(element, JsonLiteral.class);
    List<JsonValue> optionsList = null;
    if (literal == null) {
      final JsonArray asArray = ObjectUtils.tryCast(element, JsonArray.class);
      if (asArray != null && !asArray.getValueList().isEmpty()) {
        final List<JsonValue> list = asArray.getValueList();
        literal = ObjectUtils.tryCast(list.get(0), JsonLiteral.class);
        optionsList = list.subList(1, list.size());
      }
    }
    if (literal == null) return MISCONFIGURATION;
    final RuleState severity = parseRuleSeverity(literal);
    if (!RuleState.ok.equals(severity)) return new EslintNoOpSettingsConverter(severity);
    return create(optionsList, configWrapper);
  }

  protected @Nullable Boolean getAlwaysNeverOption(@Nullable List<JsonValue> values, boolean defaultValue) {
    if (values != null && !values.isEmpty()) {
      return readValueAsStringWithTwoVariants(values.get(0), ALWAYS, NEVER);
    }
    return defaultValue;
  }

  protected @Nullable Boolean getAlwaysNeverOption(@Nullable JsonProperty value,
                                                   @SuppressWarnings("SameParameterValue") boolean defaultValue) {
    if (value != null) {
      return readValueAsStringWithTwoVariants(value.getValue(), ALWAYS, NEVER);
    }
    return defaultValue;
  }

  protected @Nullable Boolean readValueAsStringWithTwoVariants(@Nullable JsonValue value,
                                                               final @NotNull String trueStr,
                                                               final @NotNull String falseStr) {
    final JsonStringLiteral literal = ObjectUtils.tryCast(value, JsonStringLiteral.class);
    if (literal != null) {
      final String text = StringUtil.unquoteString(literal.getValue());
      if (trueStr.equals(text)) {
        return true;
      }
      else if (falseStr.equals(text)) return false;
    }
    return null;
  }

  private static @NotNull RuleState parseRuleSeverity(@NotNull JsonLiteral asLiteral) {
    if (asLiteral.isQuotedString()) {
      final String text = StringUtil.unquoteString(asLiteral.getText());
      if ("off".equals(text)) return RuleState.off;
      if (TURNED_ON.contains(text)) return RuleState.ok;
      return RuleState.misconfiguration;
    }
    final Integer value = getInteger(asLiteral);
    if (value == null) return RuleState.misconfiguration;
    if (value == 0) return RuleState.off;
    if (value == 1 || value == 2) return RuleState.ok;
    return RuleState.misconfiguration;
  }

  protected static @Nullable Integer getInteger(@NotNull JsonValue value) {
    final JsonNumberLiteral number = ObjectUtils.tryCast(value, JsonNumberLiteral.class);
    if (number != null) {
      try {
        return Integer.parseInt(number.getText());
      }
      catch (NumberFormatException e) {
        //
      }
    }
    return null;
  }

  protected @Nullable Boolean getArrayLineBreakRulesOption(@Nullable List<JsonValue> values) {
    Boolean isOn = getAlwaysNeverOption(values, true);
    if (isOn == null) {
      if (values != null && !values.isEmpty()) {
        final JsonObject object = ObjectUtils.tryCast(values.get(0), JsonObject.class);
        if (object == null) return null;
        // multiline is not equivalent to our settings, is also can enable/disable depending on the data -> skip
        final JsonProperty minItems = object.findProperty("minItems");
        if (minItems != null && minItems.getValue() != null) {
          final Integer num = getInteger(minItems.getValue());
          if (num != null && num == 0) return true;
        }
      }
    }
    return isOn;
  }

  protected static @Nullable Integer getIntOptionValue(final @Nullable JsonValue object, final @NotNull String name, int defaultValue) {
    if (object == null) return defaultValue;
    if (object instanceof JsonObject) {
      JsonProperty property = ((JsonObject)object).findProperty(name);
      if (property != null) {
        JsonValue value = property.getValue();
        if (value != null) {
          Integer intValue = getInteger(value);
          if (intValue != null) {
            return intValue;
          }
          return null; //misconfiguration
        }
      }
      return defaultValue;
    }
    return null;
  }

  protected static @Nullable Boolean getBooleanOptionValue(final @Nullable JsonObject object,
                                                           final @NotNull String name,
                                                           final boolean defaultValue) {
    if (object == null) return defaultValue;
    final JsonProperty property = object.findProperty(name);
    if (property == null) return defaultValue;
    final JsonBooleanLiteral booleanValue = ObjectUtils.tryCast(property.getValue(), JsonBooleanLiteral.class);
    if (booleanValue == null) return null;
    return booleanValue.getValue();
  }

  protected static @Nullable BeforeAfter<Boolean> getBeforeAfter(@Nullable List<JsonValue> values, boolean beforeDefault,
                                                                 @SuppressWarnings("SameParameterValue") boolean afterDefault) {
    if (values != null && !values.isEmpty()) {
      final JsonObject config = ObjectUtils.tryCast(values.get(0), JsonObject.class);
      if (config == null) return null;
      return getBeforeAfter(config, beforeDefault, afterDefault);
    }
    else {
      return new BeforeAfter<>(beforeDefault, afterDefault);
    }
  }

  protected static @Nullable BeforeAfter<Boolean> getBeforeAfter(final @NotNull JsonObject config,
                                                                 boolean beforeDefault,
                                                                 boolean afterDefault) {
    Boolean isBefore = getBooleanOptionValue(config, BEFORE, beforeDefault);
    Boolean isAfter = getBooleanOptionValue(config, AFTER, afterDefault);
    // before or after of the wrong type
    if (isBefore == null || isAfter == null) return null;
    return new BeforeAfter<>(isBefore, isAfter);
  }

  public interface EslintConfig {
    @NotNull JsonObject getConfigRoot();

    @NotNull Set<String> getPlugins();
  }

  public enum RuleState {
    off,
    misconfiguration,
    skipped,
    sameSettings,
    ok
  }
}
