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

import idea.plugin.protoeditor.java.test.MutableClashingMessageMutableOuterClass.ClashingMessageMutable;

/**
 * Exercises a proto file with the same name as a message (after normalization). Tests the mutable
 * API, where we also need to add a prefix to the outer class name.
 */
@SuppressWarnings("unused") // Loaded by IntelliJ test.
public class ClashingMessageMutableUser {

  // EXPECT-NEXT: clashing_message_mutable.proto / ClashingMessageMutable
  public /* caretAfterThis */ ClashingMessageMutable test(ClashingMessageMutable x) {
    // EXPECT-NEXT: clashing_message_mutable.proto / ClashingMessageMutable.foo
    x./* caretAfterThis */ setFoo(123);
    return x;
  }
}
