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
package com.intellij.protobuf.lang.stub.type;

import com.intellij.protobuf.lang.PbLanguage;
import com.intellij.psi.tree.IElementType;

/**
 * Stub element type constants and factory.
 */
public interface PbStubElementTypes {

  PbGroupDefinitionType GROUP_DEFINITION =
    new PbGroupDefinitionType("GROUP_DEFINITION", PbLanguage.INSTANCE);
  PbEnumDefinitionType ENUM_DEFINITION =
    new PbEnumDefinitionType("ENUM_DEFINITION", PbLanguage.INSTANCE);
  PbExtendDefinitionType EXTEND_DEFINITION =
    new PbExtendDefinitionType("EXTEND_DEFINITION", PbLanguage.INSTANCE);
  PbMessageDefinitionType MESSAGE_DEFINITION =
    new PbMessageDefinitionType("MESSAGE_DEFINITION", PbLanguage.INSTANCE);
  PbOneofDefinitionType ONEOF_DEFINITION =
    new PbOneofDefinitionType("ONEOF_DEFINITION", PbLanguage.INSTANCE);
  PbPackageStatementType PACKAGE_STATEMENT =
    new PbPackageStatementType("PACKAGE_STATEMENT", PbLanguage.INSTANCE);
  PbServiceDefinitionType SERVICE_DEFINITION =
    new PbServiceDefinitionType("SERVICE_DEFINITION", PbLanguage.INSTANCE);
  PbServiceMethodDefinitionType SERVICE_METHOD =
    new PbServiceMethodDefinitionType("SERVICE_METHOD", PbLanguage.INSTANCE);

  static IElementType get(String name) {
    if ("GROUP_DEFINITION".equals(name)) {
      return GROUP_DEFINITION;
    }
    else if ("ENUM_DEFINITION".equals(name)) {
      return ENUM_DEFINITION;
    }
    else if ("EXTEND_DEFINITION".equals(name)) {
      return EXTEND_DEFINITION;
    }
    else if ("MESSAGE_DEFINITION".equals(name)) {
      return MESSAGE_DEFINITION;
    }
    else if ("ONEOF_DEFINITION".equals(name)) {
      return ONEOF_DEFINITION;
    }
    else if ("PACKAGE_STATEMENT".equals(name)) {
      return PACKAGE_STATEMENT;
    }
    else if ("SERVICE_DEFINITION".equals(name)) {
      return SERVICE_DEFINITION;
    }
    else if ("SERVICE_METHOD".equals(name)) {
      return SERVICE_METHOD;
    }
    throw new IllegalArgumentException("Unknown type: " + name);
  }
}
