/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.resolve;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.ui.JBColor;
import com.intellij.protobuf.lang.annotation.OptionOccurrenceTracker;
import com.intellij.protobuf.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

/**
 * LookupElement that wraps {@link PbSymbol} and deduplicates completion variants by name. We really
 * only expect this for packages. Duplicate field names, type names, etc. are not allowed.
 */
public class PbSymbolLookupElement extends LookupElement {

  private final PbSymbol symbol;
  private final String name;
  private final Icon icon;
  private JBColor highlightColor = null;

  public PbSymbolLookupElement(PbSymbol symbol) {
    this(symbol, symbol.getName(), symbol.getIcon(0));
  }

  private PbSymbolLookupElement(PbSymbol symbol, String name, Icon icon) {
    this.symbol = symbol;
    this.name = name;
    this.icon = icon;
  }

  public static PbSymbolLookupElement forGroupDefinitionAsField(
      PbGroupDefinition group, OptionOccurrenceTracker.Occurrence occurrence) {
    PbSymbolLookupElement element = new PbSymbolLookupElement(group);
    PbField generatedField = group.getGeneratedField();
    if (generatedField != null
        && occurrence != null
        && !occurrence.canFieldBeUsed(generatedField)) {
      element.setRedHighlight();
    }
    return element;
  }

  public static PbSymbolLookupElement withUnusableFieldHighlight(
      PbSymbol symbol, OptionOccurrenceTracker.Occurrence occurrence) {
    PbSymbolLookupElement element = new PbSymbolLookupElement(symbol);
    if (symbol instanceof PbField
        && occurrence != null
        && !occurrence.canFieldBeUsed((PbField) symbol)) {
      element.setRedHighlight();
    }
    return element;
  }

  public static PbSymbolLookupElement withUnmergeableFieldHighlight(
      PbSymbol symbol, OptionOccurrenceTracker.Occurrence occurrence) {
    PbSymbolLookupElement element = new PbSymbolLookupElement(symbol);
    if (symbol instanceof PbField
        && occurrence != null
        && !occurrence.canFieldBeUsedOrMerged((PbField) symbol)) {
      element.setRedHighlight();
    }
    return element;
  }

  private void setRedHighlight() {
    this.highlightColor = JBColor.RED;
  }

  @NotNull
  @Override
  public String getLookupString() {
    return symbol instanceof PbPackageName ? name + "." : name;
  }

  @NotNull
  @Override
  public Object getObject() {
    return symbol;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof PbSymbolLookupElement)) {
      return false;
    }
    PbSymbolLookupElement otherSymbol = (PbSymbolLookupElement) other;
    return Objects.equals(name, otherSymbol.name) && Objects.equals(icon, otherSymbol.icon);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, icon);
  }

  @Override
  public void renderElement(LookupElementPresentation presentation) {
    presentation.setItemText(name);
    presentation.setIcon(icon);
    if (symbol instanceof PbField) {
      String typeText = getTypeTextForField((PbField) symbol);
      if (typeText != null) {
        presentation.setTypeText(typeText);
      }
    }
    if (highlightColor != null) {
      presentation.setItemTextForeground(highlightColor);
    }
  }

  @Override
  public void handleInsert(@NotNull InsertionContext insertionContext) {
    if (symbol instanceof PbPackageName) {
      AutoPopupController.getInstance(insertionContext.getProject())
          .scheduleAutoPopup(insertionContext.getEditor());
    }
  }

  private String getTypeTextForField(PbField field) {
    if (field instanceof PbMapField) {
      PbMapField mapField = (PbMapField) field;
      PbTypeName keyType = mapField.getKeyType();
      PbTypeName valueType = mapField.getValueType();
      if (keyType != null && valueType != null) {
        return String.format("map<%s, %s>", keyType.getShortName(), valueType.getShortName());
      } else {
        return "map<>";
      }
    } else {
      PbTypeName typeName = field.getTypeName();
      if (typeName != null) {
        return typeName.getShortName();
      }
    }
    return null;
  }
}
