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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceWrapper;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.protobuf.lang.psi.PbField;
import com.intellij.protobuf.lang.psi.PbField.CanonicalFieldLabel;
import com.intellij.protobuf.lang.psi.PbMessageType;
import com.intellij.protobuf.lang.psi.PbTextMessage;
import com.intellij.protobuf.lang.psi.PbTypeName;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;

/**
 * A {@link PsiReference} that resolves valid message set references to their equivalent extension
 * fields.
 *
 * <p>Text format supports special syntax for message set options, allowing use of a message set
 * extension type name rather than an extension field name. For example: <code>
 *   message MessageSet {
 *     option message_set_wire_format = true;
 *     extensions 4 to max;
 *   }
 *
 *   message Extension {
 *     extend MessageSet {
 *       optional Extension message_set_extension = 10;
 *     }
 *     optional string field = 1;
 *   }
 *
 *   message Options {
 *     optional MessageSet message_set_field = 1;
 *   }
 *
 *   extend google.protobuf.MessageOptions {
 *     optional Options opt = 1234;
 *   }
 *
 *   message TestMessage {
 *     option (opt) = {
 *       // Now we can use our special message set syntax.
 *       message_set_field {
 *         [Extension] {  // This uses the type name instead of the field name.
 *           field: "foo"
 *         }
 *       }
 *     };
 *   }
 * </code> For more context:
 * https://github.com/protocolbuffers/protobuf/blob/v3.6.1/src/google/protobuf/descriptor.cc#L6920
 */
public class PbTextMessageSetSupportingExtensionNameReference extends PsiReferenceWrapper {

  public PbTextMessageSetSupportingExtensionNameReference(PsiReference originalPsiReference) {
    super(originalPsiReference);
  }

  @Override
  public PsiElement resolve() {
    PsiElement resolved = super.resolve();
    if (resolved instanceof PbMessageType) {
      PbField messageSetField = getEquivalentMessageSetField((PbMessageType) resolved);
      if (messageSetField != null) {
        return messageSetField;
      }
    }
    return resolved;
  }

  private PbField getEquivalentMessageSetField(PbMessageType messageType) {
    PbTextMessage parentMessage = PsiTreeUtil.getParentOfType(getElement(), PbTextMessage.class);
    if (parentMessage == null) {
      return null;
    }
    PbMessageType messageSetType = parentMessage.getDeclaredMessage();
    if (messageSetType == null) {
      return null;
    }
    if (!messageSetType.isMessageSet()) {
      return null;
    }

    // Now we have messageType and messageSetType. We iterate through extensions defined in
    // messageType and pick the first one that:
    // 1. is optional
    // 2. extends messageSetType
    // 3. is of type messageType
    for (PbField field : messageType.getSymbols(PbField.class)) {
      if (field.getCanonicalLabel() != CanonicalFieldLabel.OPTIONAL) {
        continue;
      }
      PbTypeName fieldTypeName = field.getTypeName();
      if (fieldTypeName == null) {
        continue;
      }
      PbTypeName extendeeTypeName = field.getExtendee();
      if (extendeeTypeName == null) {
        continue;
      }
      PbMessageType fieldMessageType =
          PbPsiUtil.resolveRefToType(fieldTypeName.getEffectiveReference(), PbMessageType.class);
      if (!messageType.equals(fieldMessageType)) {
        continue;
      }
      PbMessageType extendeeMessageType =
          PbPsiUtil.resolveRefToType(extendeeTypeName.getEffectiveReference(), PbMessageType.class);
      if (!messageSetType.equals(extendeeMessageType)) {
        continue;
      }
      return field;
    }
    return null;
  }
}
