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
import com.intellij.protobuf.lang.psi.PbExtensionRange;
import com.intellij.protobuf.lang.psi.PbNumberValue;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import org.jetbrains.annotations.Nullable;

abstract class PbExtensionRangeMixin extends PbElementBase implements PbExtensionRange {

  PbExtensionRangeMixin(ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public Long getFrom() {
    return getFromValue().getLongValue();
  }

  @Nullable
  @Override
  public Long getTo() {
    if (getNode().findChildByType(ProtoTokenTypes.MAX) != null) {
      return getMaxValue();
    }
    PbNumberValue toValue = getToValue();
    return toValue != null ? toValue.getLongValue() : null;
  }
}
