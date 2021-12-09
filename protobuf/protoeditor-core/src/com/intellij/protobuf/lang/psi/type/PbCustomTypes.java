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
package com.intellij.protobuf.lang.psi.type;

import com.intellij.psi.tree.IElementType;
import com.intellij.protobuf.lang.PbLanguage;

/** Custom element types used in proto.bnf. */
public interface PbCustomTypes {
  IElementType ENUM_BODY = new PbBlockBodyType("ENUM_BODY", PbLanguage.INSTANCE);
  IElementType EXTEND_BODY = new PbBlockBodyType("EXTEND_BODY", PbLanguage.INSTANCE);
  IElementType MESSAGE_BODY = new PbBlockBodyType("MESSAGE_BODY", PbLanguage.INSTANCE);
  IElementType ONEOF_BODY = new PbBlockBodyType("ONEOF_BODY", PbLanguage.INSTANCE);
  IElementType SERVICE_BODY = new PbBlockBodyType("SERVICE_BODY", PbLanguage.INSTANCE);

  static IElementType get(String name) {
    if ("ENUM_BODY".equals(name)) {
      return ENUM_BODY;
    } else if ("EXTEND_BODY".equals(name)) {
      return EXTEND_BODY;
    } else if ("MESSAGE_BODY".equals(name)) {
      return MESSAGE_BODY;
    } else if ("ONEOF_BODY".equals(name)) {
      return ONEOF_BODY;
    } else if ("SERVICE_BODY".equals(name)) {
      return SERVICE_BODY;
    }
    throw new IllegalArgumentException("Unknown type: " + name);
  }
}
