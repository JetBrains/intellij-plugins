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

import com.test.api_v2_lite.Proto2Lite.M1;
import com.test.api_v2_lite.Proto2Lite.M1.TestOneofCase;
import com.test.api_v2_lite.Proto2Lite.Shapes;
import com.test.api_v2_lite.Proto2Lite.weirdMessage_name;

/**
 * Test references to generated java API 2 "lite" protobuf code.
 *
 * <p>There's nothing particularly different between this and {@link Proto2User}, so we don't fill
 * out all the cases.
 */
@SuppressWarnings("unused") // Loaded by IntelliJ test.
public class Proto2LiteUser {

  public static int getSinglePrimitive(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Lite.proto / M1.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  public static M1.Builder setSinglePrimitiveWithBuilder(M1.Builder caretAfterThis, int value) {
    // EXPECT-NEXT: Proto2Lite.proto / M1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }

  // Make sure we have the correct base class for enums.
  public static M1.Builder setSingleEnum(M1.Builder caretAfterThis) {
    // EXPECT-NEXT: Proto2Lite.proto / M1.single_enum
    return caretAfterThis.setSingleEnum(
        // EXPECT-NEXT: Proto2Lite.proto / Shapes
        /* caretAfterThis */ Shapes.
            // EXPECT-NEXT: Proto2Lite.proto / CIRCLE
            /* caretAfterThis */ CIRCLE);
  }

  public static TestOneofCase oneofEnumValue(int x) {
    if (x > 0) {
      // EXPECT-NEXT: Proto2Lite.proto / M1.test_oneof
      return /* caretAfterThis */ TestOneofCase.
          // EXPECT-NEXT: Proto2Lite.proto / M1.string_choice
          /* caretAfterThis */ STRING_CHOICE;
    } else {
      // EXPECT-NEXT: Proto2Lite.proto / M1.test_oneof
      return TestOneofCase./* caretAfterThis */ TESTONEOF_NOT_SET;
    }
  }

  //---- Second message type
  public static int getSinglePrimitiveM2(weirdMessage_name caretAfterThis) {
    // EXPECT-NEXT: Proto2Lite.proto / weirdMessage_name.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  //---- Nested message single primitive
  public static M1.NestedM1.Builder setSinglePrimitive(
      M1.NestedM1.Builder caretAfterThis, int value) {
    // EXPECT-NEXT: Proto2Lite.proto / M1.NestedM1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }
}
