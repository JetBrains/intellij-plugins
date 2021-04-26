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
package com.intellij.protobuf.lang.psi;

import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

interface PbExtensionRangeBase extends PbElement {
  @Nullable
  Long getFrom();

  @Nullable
  Long getTo();

  /** Returns the value of 'max' for this extension range. */
  default long getMaxValue() {
    PbMessageType parent = PsiTreeUtil.getParentOfType(this, PbMessageType.class);
    if (parent != null && parent.isMessageSet()) {
      return PbField.MAX_MESSAGE_SET_FIELD_NUMBER;
    }
    return PbField.MAX_FIELD_NUMBER;
  }
}
