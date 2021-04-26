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

import java.util.List;

/** A message element that contains fields. */
public interface PbTextMessage extends PbTextElement {
  /**
   * Returns the declared {@link PbMessageType type} associated with this message.
   *
   * @return the message type, or <code>null</code> if no type can be determined.
   */
  @Nullable
  PbMessageType getDeclaredMessage();

  /** Return the {@link PbTextField} children of this message. */
  default List<PbTextField> getFields() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbTextField.class);
  }
}
