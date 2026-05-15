package com.intellij.lang.javascript.linter.jshint.config;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.LookupElementRenderer;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.jshint.JSHintOption;
import com.intellij.lang.javascript.linter.option.OptionEnumType;
import com.intellij.lang.javascript.linter.option.OptionEnumVariant;
import com.intellij.lang.javascript.linter.option.OptionTypes;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JSHintConfigCompletionContributor extends CompletionContributor {

  @Override
  public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    final PsiElement position = parameters.getPosition();
    if (!JSHintConfigFileUtil.isJSHintConfigFile(position)) {
      return;
    }

    boolean insideStringLiteral = JSLinterConfigFileUtil.isStringLiteral(position.getParent());

    JsonProperty property = JSLinterConfigFileUtil.getProperty(position);
    if (!isExtendsValuePlace(property, position)) {
      JSLinterConfigFileUtil.skipOtherCompletionContributors(parameters, result);
    }
    if (property == null) {
      return;
    }

    PsiElement propertyKeyElement = JSLinterConfigFileUtil.getFirstChildAsStringLiteral(property);
    if (propertyKeyElement == null) {
      completeJSHintOptions(result, insideStringLiteral);
      return;
    }
    if (position.getParent() == propertyKeyElement) {
      completeJSHintOptions(result, insideStringLiteral);
    }
    else {
      JSHintOption option = JSHintOption.findByName(StringUtil.stripQuotesAroundValue(propertyKeyElement.getText()));
      List<String> variants = Collections.emptyList();
      if (option != null) {
        if (OptionTypes.isBooleanOption(option)) {
          variants = List.of(Boolean.TRUE.toString(), Boolean.FALSE.toString());
        }
        else if (OptionTypes.isEnumOption(option)) {
          OptionEnumType enumType = OptionTypes.getOptionEnumType(option);
          variants = new ArrayList<>();
          for (OptionEnumVariant variant : enumType.getVariants()) {
            String text = variant.getValueAsJsonStr();
            if (insideStringLiteral) {
              if (variant.getValue() instanceof String) {
                variants.add(StringUtil.stripQuotesAroundValue(text));
              }
            }
            else {
              variants.add(text);
            }
          }
        }
        LookupElementRenderer<LookupElement> renderer = new LookupElementRenderer<>() {
          @Override
          public void renderElement(@NotNull LookupElement element, @NotNull LookupElementPresentation presentation) {
            presentation.setItemText(element.getLookupString());
            presentation.setItemTextBold(true);
          }
        };
        for (String variant : variants) {
          LookupElementBuilder builder = LookupElementBuilder.create(variant).withRenderer(renderer);
          result.addElement(builder);
        }
      }
    }
  }

  private static boolean isExtendsValuePlace(@Nullable JsonProperty property, @NotNull PsiElement position) {
    if (property == null) {
      return false;
    }
    PsiElement keyElement = JSLinterConfigFileUtil.getFirstChildAsStringLiteral(property);
    if (keyElement == null) {
      return false;
    }
    return JSLinterConfigFileUtil.isStringLiteral(position) && JSHintConfigFileUtil.isExtendsKey(keyElement);
  }

  private static void completeJSHintOptions(@NotNull CompletionResultSet result, boolean insideStringLiteral) {
    for (JSHintOption option : JSHintOption.values()) {
      addCompletionVariant(result, insideStringLiteral, option, option.getKey());
      if (option.getKeyAlias() != null) {
        addCompletionVariant(result, insideStringLiteral, option, option.getKeyAlias());
      }
    }
    addCompletionVariant(result, insideStringLiteral, null, JSHintConfigFileUtil.EXTENDS_KEY);
  }

  private static void addCompletionVariant(@NotNull CompletionResultSet result,
                                           boolean insideStringLiteral,
                                           final @Nullable JSHintOption option,
                                           @NotNull String variantName) {
    final String lookupString;
    if (insideStringLiteral) {
      lookupString = variantName;
    }
    else {
      lookupString = StringUtil.wrapWithDoubleQuote(variantName);
    }
    LookupElementBuilder builder = LookupElementBuilder.create(new JSHintOptionCompletionObject(lookupString), lookupString);
    builder = builder.withRenderer(new LookupElementRenderer<>() {
      @Override
      public void renderElement(@NotNull LookupElement element, @NotNull LookupElementPresentation presentation) {
        presentation.setItemText(element.getLookupString());
        presentation.setTypeGrayed(true);
        if (option != null) {
          presentation.setTypeText(option.getShortDescription());
        }
      }
    });
    result.addElement(builder);
  }
}
