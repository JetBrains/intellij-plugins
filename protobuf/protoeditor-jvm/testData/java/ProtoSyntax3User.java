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

import com.test.syntax3.ProtoSyntax3.M1;
import com.test.syntax3.ProtoSyntax3.M1.TestOneofCase;
import com.test.syntax3.ProtoSyntax3.Shapes;
import com.test.syntax3.ProtoSyntax3.weirdMessage_name;

/**
 * Test references to generated java API 2, starting with proto3 syntax.
 *
 * <p>There's nothing particularly different between this and {@link Proto2User}, so we don't fill
 * out all the cases.
 */
@SuppressWarnings("unused") // Loaded by IntelliJ test.
public class ProtoSyntax3User {

  public static int getSinglePrimitive(M1 caretAfterThis) {
    // EXPECT-NEXT: ProtoSyntax3.proto / M1.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  public static M1.Builder setSinglePrimitiveWithBuilder(M1.Builder caretAfterThis, int value) {
    // EXPECT-NEXT: ProtoSyntax3.proto / M1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }

  //---- Check enums, in case the base class changes in the future (currently still "v2" API).
  public static M1.Builder setSingleEnum(M1.Builder caretAfterThis) {
    // EXPECT-NEXT: ProtoSyntax3.proto / M1.single_enum
    return caretAfterThis.setSingleEnum(
        // EXPECT-NEXT: ProtoSyntax3.proto / Shapes
        /* caretAfterThis */ Shapes.
            // EXPECT-NEXT: ProtoSyntax3.proto / CIRCLE
            /* caretAfterThis */ CIRCLE);
  }

  public static M1.TestOneofCase oneofEnumValue(int x) {
    if (x > 0) {
      // EXPECT-NEXT: ProtoSyntax3.proto / M1.test_oneof
      return /* caretAfterThis */ TestOneofCase.
          // EXPECT-NEXT: ProtoSyntax3.proto / M1.string_choice
          /* caretAfterThis */ STRING_CHOICE;
    } else {
      // EXPECT-NEXT: ProtoSyntax3.proto / M1.test_oneof
      return M1.TestOneofCase./* caretAfterThis */ TESTONEOF_NOT_SET;
    }
  }

  //---- Second message type
  public static int getSinglePrimitiveM2(weirdMessage_name caretAfterThis) {
    // EXPECT-NEXT: ProtoSyntax3.proto / weirdMessage_name.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  //---- Nested message single primitive
  public static M1.NestedM1.Builder setSinglePrimitive(
      M1.NestedM1.Builder caretAfterThis, int value) {
    // EXPECT-NEXT: ProtoSyntax3.proto / M1.NestedM1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }
}
