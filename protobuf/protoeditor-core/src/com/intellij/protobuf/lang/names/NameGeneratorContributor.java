/*
 * Copyright 2021 Google LLC
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
package com.intellij.protobuf.lang.names;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.util.concurrency.annotations.RequiresReadLock;

/** Extension point for things that provide names for a protobuf generated class. */
public interface NameGeneratorContributor {
  ExtensionPointName<NameGeneratorContributor> EP_NAME =
    ExtensionPointName.create("com.intellij.protobuf.nameGeneratorContributor");

  /**
   * Allows a generator provider to opt into being used for a given protobuf file.
   *
   * @param file the protobuf file to provide name generators for
   * @return boolean indicating whether or not the extension will process the given file
   */
  boolean isApplicable(PbFile file);

  /**
   * Provides name generators for fields contained in the given protobuf file.
   *
   * @param file the protobuf file to provide name generators for
   * @param generatorClass the type of generator the caller is interested in
   * @return a list of name generators for the provided file
   */
  @RequiresReadLock
  <T> ImmutableList<T> contributeGenerators(PbFile file, Class<T> generatorClass);
}
