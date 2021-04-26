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

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.protobuf.lang.psi.PbTextFile;
import org.jetbrains.annotations.Nullable;

/**
 * SchemaProvider is an extension point for providing {@link SchemaInfo} instances for a PbTextFile.
 */
public interface SchemaProvider {
  ExtensionPointName<SchemaProvider> EP_NAME =
      ExtensionPointName.create("com.intellij.protobuf.schemaProvider");

  /** Returns the first SchemaInfo determined for the given file. */
  @Nullable
  static SchemaInfo forFile(PbTextFile file) {
    if (file == null) {
      return null;
    }
    for (SchemaProvider provider : SchemaProvider.EP_NAME.getExtensions(file.getProject())) {
      SchemaInfo info = provider.getSchemaInfo(file);
      if (info != null) {
        return info;
      }
    }
    return null;
  }

  /** Returns a {@link SchemaInfo} object for the given file, or <code>null</code>. */
  @Nullable
  SchemaInfo getSchemaInfo(PbTextFile file);
}
