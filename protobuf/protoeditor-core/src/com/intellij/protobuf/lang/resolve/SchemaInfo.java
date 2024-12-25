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

import com.intellij.protobuf.lang.psi.PbMessageType;
import org.jetbrains.annotations.NotNull;

/** Holds a root PbMessageType for a text format as well as an optional extension resolver. */
public class SchemaInfo {
  private final PbMessageType rootMessageType;
  private final PbSymbolResolver extensionResolver;

  private SchemaInfo(PbMessageType type, PbSymbolResolver resolver) {
    this.rootMessageType = type;
    this.extensionResolver = resolver;
  }

  public @NotNull PbMessageType getRootMessageType() {
    return this.rootMessageType;
  }

  public @NotNull PbSymbolResolver getExtensionResolver() {
    return this.extensionResolver;
  }

  /**
   * Creates a SchemaInfo with the given message type and a default extension resolver that uses the
   * file containing the root message.
   */
  public static SchemaInfo create(@NotNull PbMessageType type) {
    return new SchemaInfo(type, PbSymbolResolver.forFile(type.getPbFile()));
  }

  /** Creates a SchemaInfo with the given message type and resolver. */
  public static SchemaInfo create(@NotNull PbMessageType type, @NotNull PbSymbolResolver resolver) {
    return new SchemaInfo(type, resolver);
  }
}
