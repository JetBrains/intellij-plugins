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
import com.intellij.protobuf.lang.psi.PbMapField;
import com.intellij.protobuf.lang.psi.PbMessageType;
import com.intellij.protobuf.lang.psi.PbSymbol;
import com.intellij.protobuf.lang.psi.PbTypeName;
import com.intellij.protobuf.lang.psi.impl.PbElementFactory.FieldBuilder;
import com.intellij.protobuf.lang.psi.impl.PbElementFactory.MessageBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.intellij.protobuf.lang.psi.SyntaxLevelKt.isDeprecatedProto2Syntax;

abstract class PbMapFieldMixin extends PbFieldBase implements PbMapField {

  PbMapFieldMixin(ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull List<PbSymbol> getAdditionalSiblings() {
    PbMessageType mapEntry = createMapEntry(mapEntryName(getName()), getKeyType(), getValueType());
    if (mapEntry != null) {
      return Collections.singletonList(mapEntry);
    }
    return Collections.emptyList();
  }

  @Override
  public @Nullable PbTypeName getTypeName() {
    // A map field's type is its associated generated map entry message.
    String name = getName();
    if (name == null) {
      return null;
    }
    return PbElementFactory.getInstance(getPbFile())
        .typeNameBuilder()
        .setName(mapEntryName(name))
        .setParent(this)
        .build();
  }

  @Override
  public CanonicalFieldLabel getCanonicalLabel() {
    // Map fields are always repeated.
    return CanonicalFieldLabel.REPEATED;
  }

  /** Generate a map entry name for the given field name. Adapted from protoc's parser.cc. */
  private static String mapEntryName(String fieldName) {
    if (fieldName == null) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    boolean capNext = true;
    for (int i = 0; i < fieldName.length(); ++i) {
      char curChar = fieldName.charAt(i);
      if (curChar == '_') {
        capNext = true;
      } else if (capNext) {
        if ('a' <= curChar && curChar <= 'z') {
          result.append((char) (curChar - 'a' + 'A'));
        } else {
          result.append(curChar);
        }
        capNext = false;
      } else {
        result.append(curChar);
      }
    }
    result.append("Entry");
    return result.toString();
  }

  private PbMessageType createMapEntry(String entryName, PbTypeName keyType, PbTypeName valueType) {
    if (entryName == null) {
      return null;
    }
    boolean needsOptionalLabel = isDeprecatedProto2Syntax(getPbFile().getSyntaxLevel());
    PbElementFactory factory = PbElementFactory.getInstance(getPbFile());
    MessageBuilder builder =
        factory
            .messageBuilder()
            .setName(entryName)
            .setParent(getParent())
            .setNavigationElement(this)
            .addStatement(
                factory.optionBuilder().setName("map_entry").setValue(true).buildStatement());
    if (keyType != null) {
      FieldBuilder keyBuilder =
          factory
              .fieldBuilder()
              .setType(keyType.getReferenceString())
              .setName("key")
              .setNumber(1)
              .setNavigationElement(getKeyType());
      if (needsOptionalLabel) {
        keyBuilder.setLabel("optional");
      }
      builder.addStatement(keyBuilder.build());
    }
    if (valueType != null) {
      FieldBuilder valueBuilder =
          factory
              .fieldBuilder()
              .setType(valueType.getReferenceString())
              .setName("value")
              .setNumber(2)
              .setNavigationElement(getValueType());
      if (needsOptionalLabel) {
        valueBuilder.setLabel("optional");
      }
      builder.addStatement(valueBuilder.build());
    }
    return builder.build();
  }
}
