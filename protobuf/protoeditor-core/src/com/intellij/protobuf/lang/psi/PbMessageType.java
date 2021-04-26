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

import com.intellij.protobuf.lang.psi.util.PbPsiUtil;

/** A message type: either a MessageDefinition or a GroupField. */
public interface PbMessageType extends PbNamedTypeElement, PbSymbolOwner {

  /**
   * Get the {@link PbMessageBody} containing this type's members.
   *
   * @return The message body.
   */
  PbMessageBody getBody();

  /** Returns <code>true</code> if this message has <code>message_set_wire_format = true</code>. */
  default boolean isMessageSet() {
    PbMessageBody body = getBody();
    if (body == null) {
      return false;
    }
    return Boolean.TRUE.equals(
        PbPsiUtil.getBooleanDescriptorOption(body, "message_set_wire_format"));
  }

  /** Returns <code>true</code> if <code>fieldName</code> is reserved in this message. */
  default boolean isReservedFieldName(String fieldName) {
    if (fieldName == null) {
      return false;
    }
    PbMessageBody body = getBody();
    if (body == null) {
      return false;
    }
    for (PbReservedStatement reservedStatement : body.getReservedStatementList()) {
      for (PbStringValue reservedName : reservedStatement.getStringValueList()) {
        if (fieldName.equals(reservedName.getAsString())) {
          return true;
        }
      }
    }
    return false;
  }
}
