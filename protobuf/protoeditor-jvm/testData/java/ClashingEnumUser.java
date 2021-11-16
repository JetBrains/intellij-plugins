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
package resources.java;

import static idea.plugin.protoeditor.java.test.ClashingEnumOuterClass.ClashingEnum.ZERO;

import idea.plugin.protoeditor.java.test.ClashingEnumOuterClass.ClashingEnum;

/** Exercises a proto file with the same name as an enum (after normalization). */
@SuppressWarnings("unused") // Loaded by IntelliJ test.
public class ClashingEnumUser {

  // EXPECT-NEXT: clashing_enum.proto / ClashingEnum
  public /* caretAfterThis */ ClashingEnum test() {
    // EXPECT-NEXT: clashing_enum.proto / ZERO
    return /* caretAfterThis */ ZERO;
  }
}
