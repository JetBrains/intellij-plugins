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

import com.test.api_v2_outer_class.MyClass.M1;
import com.test.api_v2_outer_class.MyClass.weirdMessage_name;

/**
 * Test references to generated java code (w/ java_outer_classname = "MyClass").
 *
 * <p>There's nothing actually different about this vs {@link Proto2User}, it's just the outer class
 * that is different. So, we don't test all the cases here.
 */
@SuppressWarnings("unused") // Loaded by IntelliJ test.
public class Proto2OuterClassUser {

  public static int getSinglePrimitive(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2OuterClass.proto / M1.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  public static M1.Builder setSinglePrimitiveWithBuilder(M1.Builder caretAfterThis, int value) {
    // EXPECT-NEXT: Proto2OuterClass.proto / M1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }

  //---- Second message type
  public static int getSinglePrimitiveM2(weirdMessage_name caretAfterThis) {
    // EXPECT-NEXT: Proto2OuterClass.proto / weirdMessage_name.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  //---- Nested message single primitive
  public static M1.NestedM1.Builder setSinglePrimitive(
      M1.NestedM1.Builder caretAfterThis, int value) {
    // EXPECT-NEXT: Proto2OuterClass.proto / M1.NestedM1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }
}
