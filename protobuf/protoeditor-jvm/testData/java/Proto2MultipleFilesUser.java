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

import com.test.api_v2_multiple.M1;
import com.test.api_v2_multiple.M1OrBuilder;
import com.test.api_v2_multiple.Shapes;
import com.test.api_v2_multiple.weirdMessage_name;

/**
 * Test references to generated java code (w/ java_multiple_files = true).
 *
 * <p>There's nothing actually different about this vs {@link Proto2User}, it's just the outer class
 * that is different. So, we don't test all the cases here.
 */
@SuppressWarnings("unused") // Loaded by IntelliJ test.
public class Proto2MultipleFilesUser {

  // EXPECT-NEXT: Proto2MultipleFiles.proto / M1.NestedM1
  public static M1./* caretAfterThis */ NestedM1 messageType(M1.NestedM1 x) {
    return x;
  }

  // EXPECT-NEXT: Proto2MultipleFiles.proto / M1
  public static /* caretAfterThis */ M1OrBuilder orBuilderType(M1OrBuilder x) {
    return x;
  }

  public static int getSinglePrimitive(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2MultipleFiles.proto / M1.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  public static M1.Builder setSinglePrimitiveWithBuilder(M1.Builder caretAfterThis, int value) {
    // EXPECT-NEXT: Proto2MultipleFiles.proto / M1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }

  public static M1.Builder setSingleEnum(M1.Builder caretAfterThis) {
    // EXPECT-NEXT: Proto2MultipleFiles.proto / M1.single_enum
    return caretAfterThis.setSingleEnum(
        // EXPECT-NEXT: Proto2MultipleFiles.proto / Shapes
        /* caretAfterThis */ Shapes.
            // EXPECT-NEXT: Proto2MultipleFiles.proto / CIRCLE
            /* caretAfterThis */ CIRCLE);
  }

  //---- Second message type
  public static int getSinglePrimitiveM2(weirdMessage_name caretAfterThis) {
    // EXPECT-NEXT: Proto2MultipleFiles.proto / weirdMessage_name.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  //---- Nested message single primitive
  public static M1.NestedM1.Builder setSinglePrimitive(
      M1.NestedM1.Builder caretAfterThis, int value) {
    // EXPECT-NEXT: Proto2MultipleFiles.proto / M1.NestedM1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }
}
