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

import com.intellij.protobuf.lang.resolve.SchemaInfo;
import org.jetbrains.annotations.Nullable;

/**
 * A root message which provides optional schema information.
 *
 * <p>This interface may be implemented by message-like containers in other languages (like
 * PbAggregateValue).
 */
public interface PbTextRootMessage extends PbTextMessage {

  /**
   * Returns a {@link SchemaInfo} describing where to find proto definitions for this root message
   * and extensions defined within it.
   */
  @Nullable
  SchemaInfo getSchemaInfo();

  @Override
  default @Nullable PbMessageType getDeclaredMessage() {
    SchemaInfo schema = getSchemaInfo();
    return schema != null ? schema.getRootMessageType() : null;
  }

  @Override
  default PbTextRootMessage getRootMessage() {
    // Return myself as the root message in this tree.
    return this;
  }
}
