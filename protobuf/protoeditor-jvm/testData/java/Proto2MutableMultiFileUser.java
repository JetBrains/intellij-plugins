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

import com.test.api_v2_mutable_multi.MutableProto2MutableMultiFile.M1;
import com.test.api_v2_mutable_multi.MutableProto2MutableMultiFile.Shapes;
import com.test.api_v2_mutable_multi.MutableProto2MutableMultiFile.weirdMessage_name;

/**
 * Test references to generated java code (w/ mutable api, and java_multiple_files set, but
 * java_multiple_files_mutable_package is not set). Since java_multiple_files_mutable_package isn't
 * set the multiple files option doesn't actually apply.
 */
@SuppressWarnings("unused") // Loaded by IntelliJ test.
public class Proto2MutableMultiFileUser {

  public static int getSinglePrimitive(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2MutableMultiFile.proto / M1.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  public static M1 setSinglePrimitiveWithBuilder(M1 caretAfterThis, int value) {
    // EXPECT-NEXT: Proto2MutableMultiFile.proto / M1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }

  public static M1 setSingleEnum(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2MutableMultiFile.proto / M1.single_enum
    return caretAfterThis.setSingleEnum(
        // EXPECT-NEXT: Proto2MutableMultiFile.proto / Shapes
        /* caretAfterThis */ Shapes.
            // EXPECT-NEXT: Proto2MutableMultiFile.proto / CIRCLE
            /* caretAfterThis */ CIRCLE);
  }

  //---- Second message type
  public static int getSinglePrimitiveM2(weirdMessage_name caretAfterThis) {
    // EXPECT-NEXT: Proto2MutableMultiFile.proto / weirdMessage_name.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  //---- Nested message single primitive
  public static M1.NestedM1 setSinglePrimitive(M1.NestedM1 caretAfterThis, int value) {
    // EXPECT-NEXT: Proto2MutableMultiFile.proto / M1.NestedM1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }
}
