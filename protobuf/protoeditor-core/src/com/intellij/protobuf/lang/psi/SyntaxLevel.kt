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
package com.intellij.protobuf.lang.psi

/** An enum defining possible syntax levels.  */
enum class SyntaxLevel(val id: String) {
  PROTO2("proto2"),
  PROTO3("proto3"),
  EDITIONS("editions");

  companion object {
    @JvmStatic
    fun forString(level: String): SyntaxLevel? {
      return entries.firstOrNull { it.id == level }
    }
  }
}
