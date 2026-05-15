package com.intellij.lang.javascript.linter.jshint.config;

import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.linter.jshint.JSHintBundle;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.jshint.JSHintOption;
import com.intellij.lang.javascript.linter.option.OptionEnumType;
import com.intellij.lang.javascript.linter.option.OptionTypes;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sergey Simonchik
 */
public class JSHintConfigFileAnnotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    JsonFile file = ObjectUtils.tryCast(element, JsonFile.class);
    if (file != null && JSHintConfigFileUtil.isJSHintConfigFile(file)) {
      annotateFile(file, holder);
    }
  }

  private static void annotateFile(@NotNull JsonFile file, @NotNull AnnotationHolder holder) {
    for (PsiElement element : file.getChildren()) {
      JsonObject objectLiteralExpression = ObjectUtils.tryCast(element, JsonObject.class);
      if (objectLiteralExpression != null) {
        annotateObjectLiteralExpression(objectLiteralExpression, holder);
      }
    }
  }

  private static void annotateObjectLiteralExpression(@NotNull JsonObject expression,
                                                      @NotNull AnnotationHolder holder) {
    final List<JsonProperty> properties = expression.getPropertyList();
    Map<JSHintOption, PsiElement> encounteredOptions = new EnumMap<>(JSHintOption.class);
    for (JsonProperty property : properties) {
      annotateJSProperty(property, holder, encounteredOptions);
    }
  }

  private static void annotateJSProperty(@NotNull JsonProperty property,
                                         @NotNull AnnotationHolder holder,
                                         @NotNull Map<JSHintOption, PsiElement> encounteredOptions) {
    PsiElement keyElement = JSLinterConfigFileUtil.getFirstChildAsStringLiteral(property);
    if (keyElement == null) {
      return;
    }
    String keyStr = StringUtil.stripQuotesAroundValue(keyElement.getText());
    if (JSHintConfigFileUtil.EXTENDS_KEY.equals(keyStr)) {
      return;
    }
    JSHintOption option = JSHintOption.findByName(keyStr);
    if (option == null) {
      if (!JSHintConfigFileUtil.isWarningKey(keyStr)) {
        annotateJSPropertyKeyAsUnknown(keyElement, holder);
      }
      return;
    }
    PsiElement oldKeyElement = encounteredOptions.get(option);
    if (oldKeyElement != null) {
      annotateJSPropertyKeyAsDuplicated(oldKeyElement, keyElement, holder);
      return;
    }
    encounteredOptions.put(option, keyElement);
    annotateJSPropertyValue(property, option, holder);
  }

  private static void annotateJSPropertyKeyAsUnknown(@NotNull PsiElement keyElement,
                                                     @NotNull AnnotationHolder holder) {
    annotateKeyElement(keyElement, holder, JSHintBundle.message("jshint.inspection.message.unexpected.jshint.option.name"));
  }

  private static void annotateJSPropertyKeyAsDuplicated(@NotNull PsiElement oldKeyElement,
                                                        @NotNull PsiElement newKeyElement,
                                                        @NotNull AnnotationHolder holder) {
    String oldKeyStr = StringUtil.stripQuotesAroundValue(oldKeyElement.getText());
    String newKeyStr = StringUtil.stripQuotesAroundValue(newKeyElement.getText());
    if (!oldKeyStr.equals(newKeyStr)) {
      // for example, 'predef' and 'globals'
      String message = JSHintBundle.message("jshint.inspection.message.duplicate.options", oldKeyStr, newKeyStr);
      annotateKeyElement(oldKeyElement, holder, message);
      annotateKeyElement(newKeyElement, holder, message);
    }
  }

  private static void annotateKeyElement(@NotNull PsiElement keyElement,
                                         @NotNull AnnotationHolder holder,
                                         @NotNull @InspectionMessage String message) {
    TextRange textRange = keyElement.getTextRange();
    if (JSLinterConfigFileUtil.isStringLiteral(keyElement)) {
      int startInd = textRange.getStartOffset() + 1;
      int endInd = textRange.getEndOffset() - 1;
      if (startInd < endInd) {
        textRange = new TextRange(startInd, endInd);
      }
    }
    if (!textRange.isEmpty()) {
      holder.newAnnotation(HighlightSeverity.WARNING, message).range(textRange).create();
    }
  }

  private static void annotateJSPropertyValue(@NotNull JsonProperty property,
                                              @NotNull JSHintOption option,
                                              @NotNull AnnotationHolder holder) {
    JsonValue value = property.getValue();
    if (value == null) {
      return;
    }
    if (option == JSHintOption.PREDEF) {
      if (!(value instanceof JsonObject) && !(value instanceof JsonArray)) {
        holder.newAnnotation(HighlightSeverity.WARNING, JSHintBundle.message(
          "jshint.inspection.message.object.or.array.expected")).range(value).create();
      }
      return;
    }
    String valueStr = value.getText();
    Object unifiedValue = option.getType().fromString(valueStr);
    if (unifiedValue != null) {
      return;
    }
    String message = formatErrorMessageFor(option);
    holder.newAnnotation(HighlightSeverity.WARNING, message).range(value).create();
  }

  private static @InspectionMessage String formatErrorMessageFor(@NotNull JSHintOption option) {
    List<String> validVariants = Collections.emptyList();
    if (OptionTypes.isEnumOption(option)) {
      OptionEnumType enumType = OptionTypes.getOptionEnumType(option);
      validVariants = ContainerUtil.map(
        enumType.getVariants(),
        variant -> variant.getValueAsJsonStr()
      );
    }
    else if (OptionTypes.isBooleanOption(option)) {
      validVariants = List.of(Boolean.TRUE.toString(), Boolean.FALSE.toString());
    }
    final String message;
    int size = validVariants.size();
    if (size == 1) {
      message = JSHintBundle.message("jshint.inspection.message.expected.value", validVariants.get(0));
    } else if (size > 0) {
      message =
        JSHintBundle.message("jshint.inspection.message.expected.values.x.or.y", StringUtil.join(validVariants.subList(0, size - 1), ", "), validVariants.get(size - 1));
    }
    else {
      message = JSHintBundle.message("jshint.inspection.message.unexpected.value");
    }
    return message;
  }

}
