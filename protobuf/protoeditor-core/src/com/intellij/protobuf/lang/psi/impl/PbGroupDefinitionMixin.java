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
package com.intellij.protobuf.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.protobuf.ide.PbCompositeModificationTracker;
import com.intellij.protobuf.ide.util.PbIcons;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.impl.PbElementFactory.FieldBuilder;
import com.intellij.protobuf.lang.stub.PbGroupDefinitionStub;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

abstract class PbGroupDefinitionMixin extends PbStubbedSymbolOwnerBase<PbGroupDefinitionStub>
    implements PbGroupDefinition {

  PbGroupDefinitionMixin(ASTNode node) {
    super(node);
  }

  PbGroupDefinitionMixin(
      PbGroupDefinitionStub stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  @NotNull
  @Override
  public List<PbSymbol> getAdditionalSiblings() {
    PbSimpleField generatedField = getGeneratedField();
    if (generatedField != null) {
      return Collections.singletonList(generatedField);
    }
    return Collections.emptyList();
  }

  @Nullable
  @Override
  public PbSimpleField getGeneratedField() {
    return CachedValuesManager.getCachedValue(
        this, () -> Result.create(generateField(), PbCompositeModificationTracker.byElement(this)));
  }

  @Nullable
  private PbSimpleField generateField() {
    String name = getName();
    PbNumberValue number = getFieldNumber();
    Long numberValue = number != null ? number.getLongValue() : null;
    if (name == null || numberValue == null || !number.isValidInt32()) {
      return null;
    }
    PbElementFactory factory = PbElementFactory.getInstance(getPbFile());
    FieldBuilder builder =
        factory
            .fieldBuilder()
            .setParent(getParent())
            .setNavigationElement(this)
            .setLabel(getDeclaredLabel())
            .setType(getName())
            .setName(name.toLowerCase())
            .setNumber(numberValue.intValue());

    // We add all of the options to the generated field to properly represent the field's text.
    // However, the actual PbOptionExpression objects returned by the generated field's getOptions
    // method come from the group. See GroupDefinitionField below.
    PbGroupOptionContainer optionContainer = getGroupOptionContainer();
    if (optionContainer != null) {
      builder.setOptionText(optionContainer.getText());
    }

    // Wrap the built structure in a GroupDefinitionField.
    return new GroupDefinitionField(builder.build());
  }

  @Override
  public Icon getIcon(int flags) {
    return PbIcons.GROUP_FIELD;
  }

  private class GroupDefinitionField extends PbSimpleFieldImpl {
    GroupDefinitionField(PbSimpleField field) {
      super(field.getNode());
      if (field instanceof PbOverridableElement) {
        ((PbOverridableElement) field).copyOverridesTo(this);
      }
    }

    @Nullable
    @Override
    public PbFieldLabel getDeclaredLabel() {
      return PbGroupDefinitionMixin.this.getDeclaredLabel();
    }

    @Nullable
    @Override
    public PbNumberValue getFieldNumber() {
      return PbGroupDefinitionMixin.this.getFieldNumber();
    }

    @NotNull
    @Override
    public List<PbOptionExpression> getOptions() {
      // Return the group's option list.
      PbGroupOptionContainer optionContainer = getGroupOptionContainer();
      if (optionContainer == null) {
        return Collections.emptyList();
      }
      List<PbOptionExpression> options = optionContainer.getOptions();
      if (options == null) {
        return Collections.emptyList();
      }
      return options;
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
      if (super.isEquivalentTo(another)) {
        return true;
      }
      return another instanceof GroupDefinitionField
          && Objects.equals(getText(), another.getText());
    }
  }
}
