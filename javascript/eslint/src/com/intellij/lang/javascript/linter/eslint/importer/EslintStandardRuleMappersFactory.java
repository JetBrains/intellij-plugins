package com.intellij.lang.javascript.linter.eslint.importer;

import com.intellij.json.psi.JsonBooleanLiteral;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.util.BeforeAfter;
import com.intellij.util.LineSeparator;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.intellij.lang.javascript.linter.eslint.importer.EslintSettingsConverter.MISCONFIGURATION;
import static com.intellij.lang.javascript.linter.eslint.importer.EslintSettingsConverter.SKIPPED;
import static com.intellij.psi.codeStyle.CommonCodeStyleSettings.DO_NOT_WRAP;
import static com.intellij.psi.codeStyle.CommonCodeStyleSettings.WRAP_ALWAYS;

public class EslintStandardRuleMappersFactory implements EslintRuleMappersFactory {

  @Override
  public List<EslintRuleMapper> createMappers() {
    return List.of(
      new ArrayBracketNewLine(),
      new ArrayBracketSpacing(),
      new ArrayElementNewLine(),
      new ArrowSpacing(),
      new BraceStyle(),

      new CommaDangle(),
      new CommaSpacing(),
      new Curly(),
      new DotLocation(),
      new EolLast(),

      new FuncCallSpacing(),
      new Indent(),
      new IndentLegacy(),
      new KeySpacing(),
      new KeywordSpacing(),

      new LinebreakStyle(),
      new MaxLen(),
      new MultilineTernary(),
      new NewlinePerChainedCall(),
      new NoMultipleEmptyLines(),

      new NoTabs(),
      new NoTrailingSpaces(),
      new ObjectCurlyNewline(),
      new ObjectCurlySpacing(),
      new OneVarDeclarationPerLine(),

      new ObjectPropertyNewLine(),
      new Quotes(),
      new RestSpreadSpacing(),
      new Semi(),
      new SemiSpacing(),

      new SortImports(),
      new SpaceBeforeBlocks(),
      new SpaceBeforeFunctionParen(),
      new SpaceInParens(),
      new SpaceInfixOps(),

      new SpaceUnaryOps(),
      new SpacedComment(),
      new YieldStarSpacing(),
      new TemplateCurlySpacing());
  }

  private static class FuncCallSpacing extends EslintRuleMapper {
    protected FuncCallSpacing() {
      super("func-call-spacing");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final Boolean isOn = getAlwaysNeverOption(values, false);
      if (isOn == null) return MISCONFIGURATION;
      // no options
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> common.SPACE_BEFORE_METHOD_CALL_PARENTHESES != isOn,
        (common, custom) -> common.SPACE_BEFORE_METHOD_CALL_PARENTHESES = isOn);
    }
  }

  private static class SpaceUnaryOps extends EslintRuleMapper {
    protected SpaceUnaryOps() {
      super("space-unary-ops");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      Boolean nonWords = false;
      Boolean notValue = false;
      if (values != null && !values.isEmpty()) {
        final JsonObject object = ObjectUtils.tryCast(values.get(0), JsonObject.class);
        if (object == null) return MISCONFIGURATION;
        nonWords = getBooleanOptionValue(object, "nonwords", false);
        if (nonWords == null) return MISCONFIGURATION;
        JsonProperty overridesProp = object.findProperty("overrides");
        if (overridesProp != null) {
          JsonObject overridesValue = ObjectUtils.tryCast(overridesProp.getValue(), JsonObject.class);
          if (overridesValue != null) {
            notValue = getBooleanOptionValue(overridesValue, "!", nonWords);
          }
        }
        if (notValue == null) {
          notValue = nonWords;
        }
      }

      Boolean finalNonWords = nonWords;
      Boolean finalNotValue = notValue;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) ->
          common.SPACE_AROUND_UNARY_OPERATOR != finalNonWords ||
          custom.SPACE_AFTER_UNARY_NOT != finalNotValue,
        (common, custom) -> {
          common.SPACE_AROUND_UNARY_OPERATOR = finalNonWords;
          custom.SPACE_AFTER_UNARY_NOT = finalNotValue;
        });
    }
  }

  private static class SpaceInfixOps extends EslintRuleMapper {
    protected SpaceInfixOps() {
      super("space-infix-ops");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      boolean skipBitwise = false;
      if (values != null && !values.isEmpty()) {
        final JsonObject object = ObjectUtils.tryCast(values.get(0), JsonObject.class);
        if (object == null) return MISCONFIGURATION;
        final JsonProperty int32Hint = object.findProperty("int32Hint");
        if (int32Hint != null) {
          final JsonBooleanLiteral literal = ObjectUtils.tryCast(int32Hint.getValue(), JsonBooleanLiteral.class);
          if (literal == null) return MISCONFIGURATION;
          skipBitwise = literal.getValue();
        }
      }
      boolean finalSkipBitwise = skipBitwise;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> !common.SPACE_AROUND_ASSIGNMENT_OPERATORS ||
                            !common.SPACE_AROUND_LOGICAL_OPERATORS ||
                            !common.SPACE_AROUND_EQUALITY_OPERATORS ||
                            !common.SPACE_AROUND_RELATIONAL_OPERATORS ||
                            finalSkipBitwise == common.SPACE_AROUND_BITWISE_OPERATORS ||
                            !common.SPACE_AROUND_ADDITIVE_OPERATORS ||
                            !common.SPACE_AROUND_MULTIPLICATIVE_OPERATORS ||
                            !common.SPACE_AROUND_SHIFT_OPERATORS,
        (common, custom) -> {
          common.SPACE_AROUND_ASSIGNMENT_OPERATORS = true;
          common.SPACE_AROUND_LOGICAL_OPERATORS = true;
          common.SPACE_AROUND_EQUALITY_OPERATORS = true;
          common.SPACE_AROUND_RELATIONAL_OPERATORS = true;
          common.SPACE_AROUND_BITWISE_OPERATORS = !finalSkipBitwise;
          common.SPACE_AROUND_ADDITIVE_OPERATORS = true;
          common.SPACE_AROUND_MULTIPLICATIVE_OPERATORS = true;
          common.SPACE_AROUND_SHIFT_OPERATORS = true;
        });
    }
  }

  private static class SpaceInParens extends EslintRuleMapper {
    protected SpaceInParens() {
      super("space-in-parens");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final Boolean option = getAlwaysNeverOption(values, false);
      if (option == null) return MISCONFIGURATION;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> common.SPACE_WITHIN_METHOD_CALL_PARENTHESES != option ||
                            common.SPACE_WITHIN_PARENTHESES != option ||
                            common.SPACE_WITHIN_METHOD_PARENTHESES != option,
        (common, custom) -> {
          common.SPACE_WITHIN_METHOD_CALL_PARENTHESES = option;
          common.SPACE_WITHIN_PARENTHESES = option;
          common.SPACE_WITHIN_METHOD_PARENTHESES = option;
        });
    }
  }

  private static class SpaceBeforeFunctionParen extends EslintRuleMapper {
    protected SpaceBeforeFunctionParen() {
      super("space-before-function-paren");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final Boolean forNamed;
      final Boolean forAnonymous;
      final Boolean forArrow;

      final Boolean option = getAlwaysNeverOption(values, true);
      if (option == null) {
        final JsonObject obj = values != null && !values.isEmpty() && values.get(0) instanceof JsonObject ?
                               (JsonObject)values.get(0) : null;
        if (obj == null) return MISCONFIGURATION;

        forNamed = getAlwaysNeverOption(obj.findProperty("named"), true);
        forAnonymous = getAlwaysNeverOption(obj.findProperty("anonymous"), true);
        forArrow = getAlwaysNeverOption(obj.findProperty("asyncArrow"), true);
      }
      else {
        forNamed = option;
        forAnonymous = option;
        forArrow = option;
      }
      if (forAnonymous == null || forNamed == null || forArrow == null) return MISCONFIGURATION;

      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> common.SPACE_BEFORE_METHOD_PARENTHESES != forNamed ||
                            custom.SPACE_BEFORE_FUNCTION_LEFT_PARENTH != forAnonymous ||
                            custom.SPACE_BEFORE_ASYNC_ARROW_LPAREN != forArrow,
        (common, custom) -> {
          common.SPACE_BEFORE_METHOD_PARENTHESES = forNamed;
          custom.SPACE_BEFORE_FUNCTION_LEFT_PARENTH = forAnonymous;
          custom.SPACE_BEFORE_ASYNC_ARROW_LPAREN = forArrow;
        });
    }
  }

  private static class SemiSpacing extends EslintRuleMapper {
    protected SemiSpacing() {
      super("semi-spacing");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final BeforeAfter<Boolean> beforeAfter = getBeforeAfter(values, false, true);
      if (beforeAfter == null) return MISCONFIGURATION;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> common.SPACE_BEFORE_SEMICOLON != beforeAfter.getBefore(),
        (common, custom) -> common.SPACE_BEFORE_SEMICOLON = beforeAfter.getBefore());
    }
  }

  private static class ObjectCurlySpacing extends EslintRuleMapper {
    protected ObjectCurlySpacing() {
      super("object-curly-spacing");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final Boolean option = getAlwaysNeverOption(values, false);
      if (option == null) return MISCONFIGURATION;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> custom.SPACES_WITHIN_OBJECT_LITERAL_BRACES != option ||
                            custom.SPACES_WITHIN_IMPORTS != option,
        (common, custom) -> {
          custom.SPACES_WITHIN_OBJECT_LITERAL_BRACES = option;
          custom.SPACES_WITHIN_IMPORTS = option;
        });
    }
  }

  private static class ObjectCurlyNewline extends EslintRuleMapper {
    protected ObjectCurlyNewline() {
      super("object-curly-newline");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      Boolean option = true;
      if (values != null && !values.isEmpty()) {
        option = readOption(values);
        if (option == null) {
          final JsonObject config = ObjectUtils.tryCast(values.get(0), JsonObject.class);
          if (config == null) return MISCONFIGURATION;
          // also string or object
          final JsonProperty property = config.findProperty("ObjectExpression");
          final JsonValue objectExpression = property == null ? null : property.getValue();
          if (objectExpression != null) {
            option = readOption(Collections.singletonList(objectExpression));
            if (option == null) return MISCONFIGURATION;
          }
          else {
            return SKIPPED;
          }
        }
      }
      final int optionValue = option ? WRAP_ALWAYS : DO_NOT_WRAP;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> custom.OBJECT_LITERAL_WRAP != optionValue,
        (common, custom) -> custom.OBJECT_LITERAL_WRAP = optionValue);
    }

    private @Nullable Boolean readOption(@NotNull List<JsonValue> values) {
      final Boolean option = getAlwaysNeverOption(values, true);
      if (option != null) return option;
      final JsonObject config = ObjectUtils.tryCast(values.get(0), JsonObject.class);
      if (config == null) return null;
      final JsonProperty property = config.findProperty("multiline");
      final JsonBooleanLiteral multiline = property == null ? null : ObjectUtils.tryCast(property.getValue(), JsonBooleanLiteral.class);
      if (multiline != null) {
        return multiline.getValue();
      }
      return null;
    }
  }

  private static class YieldStarSpacing extends EslintRuleMapper {
    protected YieldStarSpacing() {
      super("yield-star-spacing");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final BeforeAfter<Boolean> beforeAfter;
      if (values != null && !values.isEmpty()) {
        final JsonStringLiteral literal = ObjectUtils.tryCast(values.get(0), JsonStringLiteral.class);
        if (literal != null) {
          switch (StringUtil.unquoteString(literal.getValue())) {
            case AFTER -> beforeAfter = new BeforeAfter<>(false, true);
            case BEFORE -> beforeAfter = new BeforeAfter<>(true, false);
            case "both" -> beforeAfter = new BeforeAfter<>(true, true);
            case "neither" -> beforeAfter = new BeforeAfter<>(false, false);
            default -> {
              return MISCONFIGURATION;
            }
          }
        }
        else {
          beforeAfter = getBeforeAfter(values, false, true);
          if (beforeAfter == null) return MISCONFIGURATION;
        }
      }
      else {
        beforeAfter = new BeforeAfter<>(false, true);
      }

      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> custom.SPACE_BEFORE_GENERATOR_MULT != beforeAfter.getBefore() ||
                            custom.SPACE_AFTER_GENERATOR_MULT != beforeAfter.getAfter(),
        (common, custom) -> {
          custom.SPACE_BEFORE_GENERATOR_MULT = beforeAfter.getBefore();
          custom.SPACE_AFTER_GENERATOR_MULT = beforeAfter.getAfter();
        });
    }
  }

  private static class RestSpreadSpacing extends EslintRuleMapper {
    protected RestSpreadSpacing() {
      super("rest-spread-spacing");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final Boolean option = getAlwaysNeverOption(values, false);
      if (option == null) return MISCONFIGURATION;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> custom.SPACE_AFTER_DOTS_IN_REST_PARAMETER != option,
        (common, custom) -> custom.SPACE_AFTER_DOTS_IN_REST_PARAMETER = option);
    }
  }

  private static final class TemplateCurlySpacing extends EslintRuleMapper {
    private TemplateCurlySpacing() {
      super("template-curly-spacing");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final Boolean option = getAlwaysNeverOption(values, false);
      if (option == null) return MISCONFIGURATION;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> custom.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS != option,
        (common, custom) -> custom.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = option);
    }
  }


  private static class DotLocation extends EslintRuleMapper {
    protected DotLocation() {
      super("dot-location");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      boolean isOnNewLine = false;
      if (values != null && !values.isEmpty()) {
        final JsonStringLiteral value = ObjectUtils.tryCast(values.get(0), JsonStringLiteral.class);
        if (value == null) return MISCONFIGURATION;
        final String text = StringUtil.unquoteString(value.getValue());
        if ("property".equals(text)) {
          isOnNewLine = true;
        }
        else if (!"object".equals(text)) return MISCONFIGURATION;
      }
      boolean finalIsOnNewLine = isOnNewLine;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> custom.CHAINED_CALL_DOT_ON_NEW_LINE != finalIsOnNewLine,
        (common, custom) -> custom.CHAINED_CALL_DOT_ON_NEW_LINE = finalIsOnNewLine);
    }
  }

  private static class Curly extends EslintRuleMapper {
    protected Curly() {
      super("curly");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      Option option = Option.all;
      if (values != null && !values.isEmpty()) {
        final JsonStringLiteral value = ObjectUtils.tryCast(values.get(0), JsonStringLiteral.class);
        if (value == null) return MISCONFIGURATION;
        final String text = StringUtil.unquoteString(value.getValue());
        option = ContainerUtil.find(Option.values(), o -> o.getText().equals(text));
        if (option == null) return MISCONFIGURATION;
      }
      final int selected = option.getValue();
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> common.IF_BRACE_FORCE != selected ||
                            common.FOR_BRACE_FORCE != selected ||
                            common.WHILE_BRACE_FORCE != selected ||
                            common.DOWHILE_BRACE_FORCE != selected,
        (common, custom) -> {
          common.IF_BRACE_FORCE = selected;
          common.FOR_BRACE_FORCE = selected;
          common.WHILE_BRACE_FORCE = selected;
          common.DOWHILE_BRACE_FORCE = selected;
        });
    }

    private enum Option {
      all("all", CommonCodeStyleSettings.FORCE_BRACES_ALWAYS),
      multi("multi", CommonCodeStyleSettings.FORCE_BRACES_IF_MULTILINE),
      multiLine("multi-line", CommonCodeStyleSettings.FORCE_BRACES_IF_MULTILINE),
      multiOrNest("multi-or-nest", CommonCodeStyleSettings.FORCE_BRACES_IF_MULTILINE);

      private final String myText;
      private final int myValue;

      Option(String text, int value) {
        myText = text;
        myValue = value;
      }

      public String getText() {
        return myText;
      }

      @CommonCodeStyleSettings.ForceBraceConstant
      public int getValue() {
        return myValue;
      }
    }
  }

  private static class EolLast extends EslintRuleMapper {
    protected EolLast() {
      super("eol-last");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final Boolean option = getAlwaysNeverOption(values, true);
      if (option == null) return MISCONFIGURATION;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> EditorSettingsExternalizable
                              .getInstance().isEnsureNewLineAtEOF() != option,
        (common, custom) -> EditorSettingsExternalizable.getInstance()
          .setEnsureNewLineAtEOF(option));
    }
  }

  private static class CommaSpacing extends EslintRuleMapper {
    protected CommaSpacing() {
      super("comma-spacing");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final BeforeAfter<Boolean> beforeAfter = getBeforeAfter(values, false, true);
      if (beforeAfter == null) return MISCONFIGURATION;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> beforeAfter.getBefore() != common.SPACE_BEFORE_COMMA ||
                            beforeAfter.getAfter() != common.SPACE_AFTER_COMMA,
        (common, custom) -> {
          common.SPACE_BEFORE_COMMA = beforeAfter.getBefore();
          common.SPACE_AFTER_COMMA = beforeAfter.getAfter();
        });
    }
  }

  private static class BraceStyle extends EslintRuleMapper {
    protected BraceStyle() {
      super("brace-style");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      Style style = Style.oneTrueBraceStyle;
      if (values != null && !values.isEmpty()) {
        final JsonStringLiteral literal = ObjectUtils.tryCast(values.get(0), JsonStringLiteral.class);
        if (literal != null) {
          final String text = StringUtil.unquoteString(literal.getValue());
          if (Style.allman.getText().equals(text)) {
            style = Style.allman;
          }
          else if (Style.stroustrup.getText().equals(text)) {
            style = Style.stroustrup;
          }
          else if (!Style.oneTrueBraceStyle.getText().equals(text)) return MISCONFIGURATION;
        }
        else {
          return MISCONFIGURATION;
        }
      }

      Style finalStyle = style;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> {
          final int braceStyle = finalStyle.getBeginBraceStyle();
          //noinspection MagicConstant
          return braceStyle != common.BRACE_STYLE || braceStyle != common.METHOD_BRACE_STYLE ||
                 braceStyle != custom.FUNCTION_EXPRESSION_BRACE_STYLE || braceStyle != common.CLASS_BRACE_STYLE ||
                 common.ELSE_ON_NEW_LINE != finalStyle.isElseOnNewLine();
        },
        (common, custom) -> {
          final int braceStyle = finalStyle.getBeginBraceStyle();
          //noinspection MagicConstant
          common.BRACE_STYLE = braceStyle;
          //noinspection MagicConstant
          common.METHOD_BRACE_STYLE = braceStyle;
          //noinspection MagicConstant
          custom.FUNCTION_EXPRESSION_BRACE_STYLE = braceStyle;
          //noinspection MagicConstant
          common.CLASS_BRACE_STYLE = braceStyle;
          common.ELSE_ON_NEW_LINE = finalStyle.isElseOnNewLine();
        });
    }

    private enum Style {
      oneTrueBraceStyle("1tbs", CommonCodeStyleSettings.END_OF_LINE, false),
      stroustrup("stroustrup", CommonCodeStyleSettings.END_OF_LINE, true),
      allman("allman", CommonCodeStyleSettings.NEXT_LINE, true);

      private final String myText;
      private final int myBeginBraceStyle;
      private final boolean myElseOnNewLine;

      Style(String text, int style, boolean line) {
        myText = text;
        myBeginBraceStyle = style;
        myElseOnNewLine = line;
      }

      public String getText() {
        return myText;
      }

      public int getBeginBraceStyle() {
        return myBeginBraceStyle;
      }

      public boolean isElseOnNewLine() {
        return myElseOnNewLine;
      }
    }
  }

  private static class ArrayBracketNewLine extends EslintRuleMapper {
    protected ArrayBracketNewLine() {
      super("array-bracket-newline");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final Boolean isOn = getArrayLineBreakRulesOption(values);
      if (isOn == null) return MISCONFIGURATION;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> common.ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE != isOn ||
                            common.ARRAY_INITIALIZER_RBRACE_ON_NEXT_LINE != isOn,
        (common, custom) -> {
          common.ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE = isOn;
          common.ARRAY_INITIALIZER_RBRACE_ON_NEXT_LINE = isOn;
        });
    }
  }

  private static class ArrayElementNewLine extends EslintRuleMapper {
    protected ArrayElementNewLine() {
      super("array-element-newline");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final Boolean isOn = getArrayLineBreakRulesOption(values);
      if (isOn == null) return MISCONFIGURATION;
      final int value = isOn ? WRAP_ALWAYS : DO_NOT_WRAP;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> common.ARRAY_INITIALIZER_WRAP != value,
        (common, custom) -> common.ARRAY_INITIALIZER_WRAP = value);
    }
  }

  private static class ArrayBracketSpacing extends EslintRuleMapper {
    protected ArrayBracketSpacing() {
      super("array-bracket-spacing");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final Boolean isOn = getAlwaysNeverOption(values, false);
      if (isOn == null) return MISCONFIGURATION;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> custom.SPACE_WITHIN_ARRAY_INITIALIZER_BRACKETS != isOn,
        (common, custom) -> custom.SPACE_WITHIN_ARRAY_INITIALIZER_BRACKETS = isOn);
    }
  }

  private static class CommaDangle extends EslintRuleMapper {
    protected CommaDangle() {
      super("comma-dangle");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      JSCodeStyleSettings.TrailingCommaOption option = JSCodeStyleSettings.TrailingCommaOption.Remove;
      if (values != null && !values.isEmpty()) {
        final JsonStringLiteral literal = ObjectUtils.tryCast(values.get(0), JsonStringLiteral.class);
        if (literal != null) {
          switch (StringUtil.unquoteString(literal.getValue())) {
            case "never" -> {}
            case "always", "ignore" -> option = JSCodeStyleSettings.TrailingCommaOption.Keep;
            case "always-multiline", "only-multiline" -> option = JSCodeStyleSettings.TrailingCommaOption.WhenMultiline;
            default -> {
              return MISCONFIGURATION;
            }
          }
        }
        else if (values.get(0) instanceof JsonObject) return SKIPPED;
      }
      final JSCodeStyleSettings.TrailingCommaOption finalOption = option;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> custom.ENFORCE_TRAILING_COMMA != finalOption,
        (common, custom) -> custom.ENFORCE_TRAILING_COMMA = finalOption);
    }
  }

  private static class KeySpacing extends EslintRuleMapper {
    private static final String BEFORE_COLON = "beforeColon";
    private static final String AFTER_COLON = "afterColon";

    KeySpacing() {
      super("key-spacing");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      Boolean beforeColon = false;
      Boolean afterColon = true;
      Boolean alignIsColon = null;
      if (values != null && !values.isEmpty()) {
        final JsonObject config = ObjectUtils.tryCast(values.get(0), JsonObject.class);
        if (config == null) return MISCONFIGURATION;
        beforeColon = getBooleanOptionValue(config, BEFORE_COLON, false);
        afterColon = getBooleanOptionValue(config, AFTER_COLON, true);
        final JsonProperty alignProperty = config.findProperty("align");
        if (alignProperty != null) {
          final JsonObject alignObject = ObjectUtils.tryCast(alignProperty.getValue(), JsonObject.class);
          if (alignObject != null) {
            alignIsColon = true;
            final JsonProperty on = alignObject.findProperty("on");
            if (on != null) {
              alignIsColon = readValueAsStringWithTwoVariants(on.getValue(), "colon", "value");
              if (alignIsColon == null) return MISCONFIGURATION;
            }

            if (alignObject.findProperty(BEFORE_COLON) != null) beforeColon = getBooleanOptionValue(alignObject, BEFORE_COLON, false);
            if (alignObject.findProperty(AFTER_COLON) != null) afterColon = getBooleanOptionValue(alignObject, AFTER_COLON, true);
          }
          else {
            alignIsColon = readValueAsStringWithTwoVariants(alignProperty.getValue(), "colon", "value");
            if (alignIsColon == null) return MISCONFIGURATION;
          }
        }
      }
      if (beforeColon == null || afterColon == null) return MISCONFIGURATION;
      final Boolean finalBeforeColon = beforeColon;
      final Boolean finalAfterColon = afterColon;
      final Integer finalAlign = alignIsColon == null ? null : alignIsColon ? JSCodeStyleSettings.ALIGN_ON_COLON :
                                                               JSCodeStyleSettings.ALIGN_ON_VALUE;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> custom.SPACE_BEFORE_PROPERTY_COLON != finalBeforeColon ||
                            custom.SPACE_AFTER_PROPERTY_COLON != finalAfterColon ||
                            finalAlign != null &&
                            custom.ALIGN_OBJECT_PROPERTIES != finalAlign,
        (common, custom) -> {
          custom.SPACE_BEFORE_PROPERTY_COLON = finalBeforeColon;
          custom.SPACE_AFTER_PROPERTY_COLON = finalAfterColon;
          if (finalAlign != null) custom.ALIGN_OBJECT_PROPERTIES = finalAlign;
        });
    }
  }

  private static class KeywordSpacing extends EslintRuleMapper {
    private static @Unmodifiable Map<String, EslintSettingsConverter> beforeMap(final boolean value,
                                                                                @NotNull EslintConfig configWrapper) {
      return ContainerUtil.map2Map(Set.of(
        Pair.create("else", new EslintJsSettingsConverter(configWrapper,
                                                          (common, custom) -> common.SPACE_BEFORE_ELSE_KEYWORD != value,
                                                          (common, custom) -> common.SPACE_BEFORE_ELSE_KEYWORD = value)),
        Pair.create("while", new EslintJsSettingsConverter(configWrapper,
                                                           (common, custom) -> common.SPACE_BEFORE_WHILE_KEYWORD != value,
                                                           (common, custom) -> common.SPACE_BEFORE_WHILE_KEYWORD = value)),
        Pair.create("catch", new EslintJsSettingsConverter(configWrapper,
                                                           (common, custom) -> common.SPACE_BEFORE_CATCH_KEYWORD != value,
                                                           (common, custom) -> common.SPACE_BEFORE_CATCH_KEYWORD = value)),
        Pair.create("finally", new EslintJsSettingsConverter(configWrapper,
                                                             (common, custom) -> common.SPACE_BEFORE_FINALLY_KEYWORD != value,
                                                             (common, custom) -> common.SPACE_BEFORE_FINALLY_KEYWORD =
                                                               value))));
    }

    private static @Unmodifiable Map<String, EslintSettingsConverter> afterMap(final boolean value,
                                                                               @NotNull EslintConfig configWrapper) {
      return ContainerUtil.map2Map(Set.of(
        Pair.create("if", new EslintJsSettingsConverter(configWrapper,
                                                        (common, custom) -> common.SPACE_BEFORE_IF_PARENTHESES != value,
                                                        (common, custom) -> common.SPACE_BEFORE_IF_PARENTHESES = value)),
        Pair.create("for", new EslintJsSettingsConverter(configWrapper,
                                                         (common, custom) -> common.SPACE_BEFORE_FOR_PARENTHESES != value,
                                                         (common, custom) -> common.SPACE_BEFORE_FOR_PARENTHESES = value)),
        Pair.create("while", new EslintJsSettingsConverter(configWrapper,
                                                           (common, custom) -> common.SPACE_BEFORE_WHILE_PARENTHESES != value,
                                                           (common, custom) -> common.SPACE_BEFORE_WHILE_PARENTHESES = value)),
        Pair.create("switch", new EslintJsSettingsConverter(configWrapper,
                                                            (common, custom) -> common.SPACE_BEFORE_SWITCH_PARENTHESES != value,
                                                            (common, custom) -> common.SPACE_BEFORE_SWITCH_PARENTHESES =
                                                              value)),
        Pair.create("catch", new EslintJsSettingsConverter(configWrapper,
                                                           (common, custom) -> common.SPACE_BEFORE_CATCH_PARENTHESES != value,
                                                           (common, custom) -> common.SPACE_BEFORE_CATCH_PARENTHESES =
                                                             value))));
    }

    protected KeywordSpacing() {
      super("keyword-spacing");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      // default before: true, after: true
      final Map<String, EslintSettingsConverter> beforeTrue = beforeMap(true, eslintConfig);
      final Map<String, EslintSettingsConverter> afterTrue = afterMap(true, eslintConfig);
      final Map<String, EslintSettingsConverter> beforeFalse = new HashMap<>(beforeMap(false, eslintConfig));
      final Map<String, EslintSettingsConverter> afterFalse = new HashMap<>(afterMap(false, eslintConfig));

      Map<String, EslintSettingsConverter> beforeMap = new HashMap<>(beforeMap(true, eslintConfig));
      Map<String, EslintSettingsConverter> afterMap = new HashMap<>(afterMap(true, eslintConfig));

      if (values != null && !values.isEmpty()) {
        final JsonObject object = ObjectUtils.tryCast(values.get(0), JsonObject.class);
        if (object == null) return MISCONFIGURATION;
        final BeforeAfter<Boolean> beforeAfter = getBeforeAfter(object, true, true);
        if (beforeAfter == null) return MISCONFIGURATION;
        if (!Boolean.TRUE.equals(beforeAfter.getBefore())) beforeMap = beforeFalse;
        if (!Boolean.TRUE.equals(beforeAfter.getAfter())) afterMap = afterFalse;

        final JsonProperty overrides = object.findProperty("overrides");
        if (overrides != null) {
          final JsonObject overridesObj = ObjectUtils.tryCast(overrides.getValue(), JsonObject.class);
          if (overridesObj == null) return MISCONFIGURATION;

          if (fillMapFromOverrides(BEFORE, beforeMap, beforeTrue, beforeFalse, overridesObj)) return MISCONFIGURATION;
          if (fillMapFromOverrides(AFTER, afterMap, afterTrue, afterFalse, overridesObj)) return MISCONFIGURATION;
        }
      }
      final Collection<EslintSettingsConverter> beforeConvertors = beforeMap.values();
      final Collection<EslintSettingsConverter> afterConvertors = afterMap.values();
      return new EslintSettingsConverter() {

        @Override
        public boolean inSync(@NotNull CodeStyleSettings settings) {
          return beforeConvertors.stream().allMatch(c -> c.inSync(settings))
                 && afterConvertors.stream().allMatch(c -> c.inSync(settings));
        }

        @Override
        public void apply(@NotNull CodeStyleSettings settings) {
          beforeConvertors.forEach(c -> c.apply(settings));
          afterConvertors.forEach(c -> c.apply(settings));
        }
      };
    }

    private static boolean fillMapFromOverrides(final @NotNull String propName,
                                                final @NotNull Map<String, EslintSettingsConverter> map,
                                                final @NotNull Map<String, EslintSettingsConverter> mapTrue,
                                                final @NotNull Map<String, EslintSettingsConverter> mapFalse,
                                                final @NotNull JsonObject overridesObj) {
      return overridesObj.getPropertyList().stream()
        .filter(p -> mapTrue.containsKey(StringUtil.unquoteString(p.getName())))
        .map(p -> {
          final JsonObject pObj = ObjectUtils.tryCast(p.getValue(), JsonObject.class);
          if (pObj == null) return false;
          final Boolean option = getBooleanOptionValue(pObj, propName, true);
          if (option == null) return false;
          final String name = StringUtil.unquoteString(p.getName());
          map.put(name, option ? mapTrue.get(name) : mapFalse.get(name));
          return true;
        }).anyMatch(value -> !value);
    }
  }

  private static class LinebreakStyle extends EslintRuleMapper {
    protected LinebreakStyle() {
      super("linebreak-style");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      boolean isLf = true;  // unix default (lf)
      if (values != null && !values.isEmpty()) {
        final JsonStringLiteral literal = ObjectUtils.tryCast(values.get(0), JsonStringLiteral.class);
        if (literal != null) {
          final String text = StringUtil.unquoteString(literal.getValue());
          if ("windows".equals(text)) {
            isLf = false;
          }
          else if (!"unix".equals(text)) return MISCONFIGURATION;
        }
        else {
          return MISCONFIGURATION;
        }
      }
      final LineSeparator separator = isLf ? LineSeparator.LF : LineSeparator.CRLF;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> !separator.getSeparatorString().equals(common.getRootSettings().getLineSeparator()),
        (common, custom) -> common.getRootSettings().LINE_SEPARATOR = separator.getSeparatorString());
    }
  }

  private static class NewlinePerChainedCall extends EslintRuleMapper {
    protected NewlinePerChainedCall() {
      super("newline-per-chained-call");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> common.METHOD_CALL_CHAIN_WRAP != WRAP_ALWAYS,
        (common, custom) -> common.METHOD_CALL_CHAIN_WRAP = WRAP_ALWAYS);
    }
  }

  private static class MultilineTernary extends EslintRuleMapper {
    protected MultilineTernary() {
      super("multiline-ternary");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final Boolean isOn = getAlwaysNeverOption(values, true);
      if (isOn == null) return MISCONFIGURATION;
      final int option = isOn ? WRAP_ALWAYS : DO_NOT_WRAP;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> common.TERNARY_OPERATION_WRAP != option,
        (common, custom) -> common.TERNARY_OPERATION_WRAP = option);
    }
  }

  private static class NoMultipleEmptyLines extends EslintRuleMapper {
    protected NoMultipleEmptyLines() {
      super("no-multiple-empty-lines");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      Integer blankLines = null;
      if (values != null && !values.isEmpty()) {
        final JsonObject object = ObjectUtils.tryCast(values.get(0), JsonObject.class);
        if (object != null) {
          final JsonProperty code = object.findProperty("max");
          if (code != null && code.getValue() != null) {
            blankLines = getInteger(code.getValue());
          }
        }
        else {
          return MISCONFIGURATION;
        }
      }
      blankLines = blankLines == null ? 2 : blankLines;
      int finalBlankLines = blankLines;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> common.KEEP_BLANK_LINES_IN_CODE != finalBlankLines,
        (common, custom) -> common.KEEP_BLANK_LINES_IN_CODE = finalBlankLines);
    }
  }

  private static class NoTrailingSpaces extends EslintRuleMapper {
    protected NoTrailingSpaces() {
      super("no-trailing-spaces");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> {
          final EditorSettingsExternalizable editorSettings = EditorSettingsExternalizable.getInstance();
          return editorSettings != null &&
                 !EditorSettingsExternalizable.STRIP_TRAILING_SPACES_WHOLE.equals(editorSettings.getStripTrailingSpaces());
        },
        (common, custom) -> {
          final EditorSettingsExternalizable editorSettings = EditorSettingsExternalizable.getInstance();
          assert editorSettings != null;
          editorSettings.setStripTrailingSpaces(EditorSettingsExternalizable.STRIP_TRAILING_SPACES_WHOLE);
        });
    }
  }

  private static class NoTabs extends EslintRuleMapper {
    NoTabs() {
      super("no-tabs");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> {
          final CommonCodeStyleSettings.IndentOptions indentOptions = common.getIndentOptions();
          assert indentOptions != null;
          return indentOptions.USE_TAB_CHARACTER;
        },
        (common, custom) -> {
          final CommonCodeStyleSettings.IndentOptions indentOptions = common.getIndentOptions();
          assert indentOptions != null;
          indentOptions.USE_TAB_CHARACTER = false;
        });
    }
  }

  private static class MaxLen extends EslintRuleMapper {
    protected MaxLen() {
      super("max-len");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      Integer rightMargin = null;
      Integer tabWidth = null;
      if (values != null && !values.isEmpty()) {
        rightMargin = getInteger(values.get(0));
        if (rightMargin != null) {
          if (values.size() > 1) {
            tabWidth = getInteger(values.get(1));
            if (tabWidth == null) return MISCONFIGURATION;
          }
        }
        else {
          final JsonObject object = ObjectUtils.tryCast(values.get(0), JsonObject.class);
          if (object != null) {
            final JsonProperty code = object.findProperty("code");
            if (code != null && code.getValue() != null) {
              rightMargin = getInteger(code.getValue());
            }
            final JsonProperty tabWidthProperty = object.findProperty("tabWidth");
            if (tabWidthProperty != null && tabWidthProperty.getValue() != null) {
              tabWidth = getInteger(tabWidthProperty.getValue());
              if (tabWidth == null) return MISCONFIGURATION;
            }
          }
          else {
            return MISCONFIGURATION; // not an integer, not an object
          }
        }
      }
      rightMargin = rightMargin == null ? 80 : rightMargin;
      tabWidth = tabWidth == null ? 4 : tabWidth;

      int finalRightMargin = rightMargin;
      Integer finalTabWidth = tabWidth;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> common.RIGHT_MARGIN != finalRightMargin ||
                            common.getIndentOptions() != null && common.getIndentOptions().TAB_SIZE != finalTabWidth,
        (common, custom) -> {
          common.RIGHT_MARGIN = finalRightMargin;
          final CommonCodeStyleSettings.IndentOptions indentOptions = common.getIndentOptions();
          if (indentOptions != null) indentOptions.TAB_SIZE = finalTabWidth;
        });
    }
  }

  private static class SpacedComment extends EslintRuleMapper {
    protected SpacedComment() {
      super("spaced-comment");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final Boolean isOn = getAlwaysNeverOption(values, true);
      if (isOn == null) return MISCONFIGURATION;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> isOn != common.LINE_COMMENT_ADD_SPACE || isOn != common.BLOCK_COMMENT_ADD_SPACE,
        (common, custom) -> {
          if (isOn) common.LINE_COMMENT_AT_FIRST_COLUMN = false;
          common.LINE_COMMENT_ADD_SPACE = isOn;
          common.BLOCK_COMMENT_ADD_SPACE = isOn;
        });
    }
  }

  private static class ArrowSpacing extends EslintRuleMapper {
    protected ArrowSpacing() {
      super("arrow-spacing");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final BeforeAfter<Boolean> beforeAfter = getBeforeAfter(values, true, true);
      if (beforeAfter == null) return MISCONFIGURATION;
      if (beforeAfter.getBefore() != beforeAfter.getAfter()) return SKIPPED;
      final boolean haveSpaces = Boolean.TRUE.equals(beforeAfter.getBefore());
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> haveSpaces != custom.SPACE_AROUND_ARROW_FUNCTION_OPERATOR,
        (common, custom) -> custom.SPACE_AROUND_ARROW_FUNCTION_OPERATOR = haveSpaces);
    }
  }

  private static class SortImports extends EslintRuleMapper {
    protected SortImports() {
      super("sort-imports");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      boolean sortMembers = true;
      if (values != null && !values.isEmpty()) {
        final JsonObject config = ObjectUtils.tryCast(values.get(0), JsonObject.class);
        if (config == null) return MISCONFIGURATION;
        final Boolean option = getBooleanOptionValue(config, "ignoreMemberSort", false);
        if (option == null) return MISCONFIGURATION;
        sortMembers = !option;
      }
      boolean finalSortMembers = sortMembers;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> finalSortMembers != custom.IMPORT_SORT_MEMBERS || !custom.IMPORT_SORT_MODULE_NAME,
        (common, custom) -> {
          custom.IMPORT_SORT_MEMBERS = finalSortMembers;
          custom.IMPORT_SORT_MODULE_NAME = true;
        });
    }
  }

  private static class Semi extends EslintRuleMapper {
    protected Semi() {
      super("semi");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final Boolean isOn = getAlwaysNeverOption(values, true);
      if (isOn == null) return MISCONFIGURATION;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> isOn != custom.USE_SEMICOLON_AFTER_STATEMENT || !custom.FORCE_SEMICOLON_STYLE,
        (common, custom) -> {
          custom.USE_SEMICOLON_AFTER_STATEMENT = isOn;
          custom.FORCE_SEMICOLON_STYLE = true;
        });
    }
  }

  private static class Quotes extends EslintRuleMapper {
    protected Quotes() {
      super("quotes");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      boolean isDouble = true;
      if (values != null && !values.isEmpty()) {
        final JsonStringLiteral literal = ObjectUtils.tryCast(values.get(0), JsonStringLiteral.class);
        if (literal != null) {
          final String text = StringUtil.unquoteString(literal.getValue());
          if ("single".equals(text)) {
            isDouble = false;
          }
          else if (!"double".equals(text)) return MISCONFIGURATION;
        }
        else {
          return MISCONFIGURATION;
        }
      }
      boolean finalIsDouble = isDouble;
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> finalIsDouble != custom.USE_DOUBLE_QUOTES || !custom.FORCE_QUOTE_STYlE,
        (common, custom) -> {
          custom.USE_DOUBLE_QUOTES = finalIsDouble;
          custom.FORCE_QUOTE_STYlE = true;
        });
    }
  }

  private static class IndentLegacy extends Indent {
    IndentLegacy() {
      super("indent-legacy");
    }
  }

  private static class Indent extends EslintRuleMapper {
    Indent() {
      super("indent");
    }

    protected Indent(String name) {
      super(name);
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final Integer indentSize;
      final boolean useTab;
      if (values == null || values.isEmpty()) {
        indentSize = 4;
        useTab = false;
      }
      else if (values.get(0) instanceof JsonStringLiteral) {
        useTab = "tab".equals(StringUtil.unquoteString(((JsonStringLiteral)values.get(0)).getValue()));
        indentSize = null;
      }
      else {
        indentSize = getInteger(values.get(0));
        useTab = false;
      }
      final boolean indentSwitchCases;
      if (values != null && values.size() > 1) {
        Integer intOptionValue = getIntOptionValue(values.get(1), "SwitchCase", 0);
        if (intOptionValue != null) {
          indentSwitchCases = intOptionValue > 0;
        }
        else {
          return MISCONFIGURATION;
        }
      }
      else {
        indentSwitchCases = false;
      }
      if (indentSize == null && !useTab) {
        return MISCONFIGURATION;
      }
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> {
          if (common.ALIGN_MULTILINE_PARAMETERS_IN_CALLS
              || common.ALIGN_MULTILINE_PARAMETERS
              || common.ALIGN_MULTILINE_TERNARY_OPERATION
              || common.ALIGN_MULTILINE_ARRAY_INITIALIZER_EXPRESSION
              || common.ALIGN_MULTILINE_CHAINED_METHODS
              || common.ALIGN_MULTILINE_FOR) {
            return true;
          }
          if (common.INDENT_CASE_FROM_SWITCH != indentSwitchCases) {
            return true;
          }
          final CommonCodeStyleSettings.IndentOptions indentOptions = common.getIndentOptions();
          assert indentOptions != null;
          if (indentSize != null) {
            return indentSize != indentOptions.INDENT_SIZE || indentSize != indentOptions.CONTINUATION_INDENT_SIZE;
          }
          return !indentOptions.USE_TAB_CHARACTER;
        },
        (common, custom) -> {
          common.ALIGN_MULTILINE_PARAMETERS_IN_CALLS = false;
          common.ALIGN_MULTILINE_PARAMETERS = false;
          common.ALIGN_MULTILINE_TERNARY_OPERATION = false;
          common.ALIGN_MULTILINE_ARRAY_INITIALIZER_EXPRESSION = false;
          common.ALIGN_MULTILINE_CHAINED_METHODS = false;
          common.ALIGN_MULTILINE_FOR = false;

          common.INDENT_CASE_FROM_SWITCH = indentSwitchCases;

          final CommonCodeStyleSettings.IndentOptions indentOptions = common.getIndentOptions();
          assert indentOptions != null;
          if (indentSize != null) {
            indentOptions.INDENT_SIZE = indentSize;
            indentOptions.CONTINUATION_INDENT_SIZE = indentSize;
          }
          indentOptions.USE_TAB_CHARACTER = useTab;
        });
    }
  }

  private static class ObjectPropertyNewLine extends EslintRuleMapper {
    protected ObjectPropertyNewLine() {
      super("object-property-newline");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      if (values != null && !values.isEmpty()) {
        final JsonObject object = ObjectUtils.tryCast(values.get(0), JsonObject.class);
        if (object == null) return MISCONFIGURATION;
        final Boolean multiplePropertiesPerLine = getBooleanOptionValue(object, "allowMultiplePropertiesPerLine", false);
        if (multiplePropertiesPerLine == null) return MISCONFIGURATION;
        if (multiplePropertiesPerLine) return SKIPPED;
      }
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> custom.OBJECT_LITERAL_WRAP != WRAP_ALWAYS,
        (common, custom) -> custom.OBJECT_LITERAL_WRAP = WRAP_ALWAYS);
    }
  }

  private static class OneVarDeclarationPerLine extends EslintRuleMapper {
    protected OneVarDeclarationPerLine() {
      super("one-var-declaration-per-line");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> custom.VAR_DECLARATION_WRAP != WRAP_ALWAYS,
        (common, custom) -> custom.VAR_DECLARATION_WRAP = WRAP_ALWAYS);
    }
  }

  private static class SpaceBeforeBlocks extends EslintRuleMapper {
    protected SpaceBeforeBlocks() {
      super("space-before-blocks");
    }

    @Override
    protected @NotNull EslintSettingsConverter create(@Nullable List<JsonValue> values,
                                                      @NotNull EslintConfig eslintConfig) {
      final SubGroupOption functions = new SubGroupOption("functions");
      final SubGroupOption keywords = new SubGroupOption("keywords");
      final SubGroupOption classes = new SubGroupOption("classes");
      if (values != null && !values.isEmpty()) {
        final JsonObject config = ObjectUtils.tryCast(values.get(0), JsonObject.class);
        if (config != null) {
          final boolean hasErrors = Stream.of(functions, keywords, classes)
            .anyMatch(group -> {
              final JsonProperty functionsProperty = config.findProperty(group.myOptionName);
              if (functionsProperty == null) return false;
              final JsonValue value = functionsProperty.getValue();
              if (value == null) return true;
              final Boolean option = getAlwaysNeverOption(Collections.singletonList(value), true);
              if (option == null) return true;
              group.myValue = option;
              return false;
            });
          if (hasErrors) return MISCONFIGURATION;
        }
        else {
          final Boolean option = getAlwaysNeverOption(values, true);
          if (option == null) return MISCONFIGURATION;
          functions.myValue = keywords.myValue = classes.myValue = option;
        }
      }
      return new EslintJsSettingsConverter(
        eslintConfig,
        (common, custom) -> common.SPACE_BEFORE_METHOD_LBRACE != functions.myValue ||
                            common.SPACE_BEFORE_IF_LBRACE != keywords.myValue ||
                            common.SPACE_BEFORE_ELSE_LBRACE != keywords.myValue ||
                            common.SPACE_BEFORE_FOR_LBRACE != keywords.myValue ||
                            common.SPACE_BEFORE_WHILE_LBRACE != keywords.myValue ||
                            common.SPACE_BEFORE_DO_LBRACE != keywords.myValue ||
                            common.SPACE_BEFORE_SWITCH_LBRACE != keywords.myValue ||
                            common.SPACE_BEFORE_TRY_LBRACE != keywords.myValue ||
                            common.SPACE_BEFORE_CATCH_LBRACE != keywords.myValue ||
                            common.SPACE_BEFORE_FINALLY_LBRACE != keywords.myValue ||
                            custom.SPACE_BEFORE_CLASS_LBRACE != classes.myValue,
        (common, custom) -> {
          common.SPACE_BEFORE_METHOD_LBRACE = functions.myValue;
          common.SPACE_BEFORE_IF_LBRACE = keywords.myValue;
          common.SPACE_BEFORE_ELSE_LBRACE = keywords.myValue;
          common.SPACE_BEFORE_FOR_LBRACE = keywords.myValue;
          common.SPACE_BEFORE_WHILE_LBRACE = keywords.myValue;
          common.SPACE_BEFORE_DO_LBRACE = keywords.myValue;
          common.SPACE_BEFORE_SWITCH_LBRACE = keywords.myValue;
          common.SPACE_BEFORE_TRY_LBRACE = keywords.myValue;
          common.SPACE_BEFORE_CATCH_LBRACE = keywords.myValue;
          common.SPACE_BEFORE_FINALLY_LBRACE = keywords.myValue;
          custom.SPACE_BEFORE_CLASS_LBRACE = classes.myValue;
        });
    }

    private static class SubGroupOption {
      private final String myOptionName;
      private Boolean myValue;

      SubGroupOption(String optionName) {
        myOptionName = optionName;
        myValue = true;
      }
    }
  }
}
