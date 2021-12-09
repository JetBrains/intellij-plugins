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

import com.test.api_v2_mutable.MutableProto2Mutable.M1;
import com.test.api_v2_mutable.MutableProto2Mutable.M1.TestOneofCase;
import com.test.api_v2_mutable.MutableProto2Mutable.Shapes;
import com.test.api_v2_mutable.MutableProto2Mutable.weirdEnum_name;
import com.test.api_v2_mutable.MutableProto2Mutable.weirdMessage_name;
import java.util.List;
import java.util.Map;

/**
 * Test references to generated java code (w/ java_mutable_api = true).
 *
 * <p>Cross between {@link Proto1User} and {@link Proto2User}.
 */
@SuppressWarnings("unused") // Loaded by IntelliJ test.
public class Proto2MutableUser {

  //---- Single primitive (like proto1)
  public static int getSinglePrimitive(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  public static M1 setSinglePrimitive(M1 caretAfterThis, int value) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }

  //---- Repeated primitive (more like proto2)
  public static int getRepeatedPrimitiveCount(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.repeated_primitive
    return caretAfterThis.getRepeatedPrimitiveCount();
  }

  public static List<Integer> getRepeatedPrimitiveList(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.repeated_primitive
    return caretAfterThis.getRepeatedPrimitiveList();
  }

  // Almost like proto1, but named differently.
  public static List<Integer> getMutableRepeatedPrimitiveList(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.repeated_primitive
    return caretAfterThis.getMutableRepeatedPrimitiveList();
  }

  public static M1 addAllRepeatedInt32Field(M1 caretAfterThis, Iterable<? extends Integer> values) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.repeated_primitive
    return caretAfterThis.addAllRepeatedPrimitive(values);
  }

  //---- Single string ("AsBytes" is like proto1 instead of proto2).

  public static M1 setSingleStringAsBytes(M1 caretAfterThis, byte[] value) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.single_string
    return caretAfterThis.setSingleStringAsBytes(value);
  }

  //---- Repeated string

  // Almost like proto1, but named differently.
  public static List<byte[]> getRepeatedStringListAsBytes(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.repeated_string
    return caretAfterThis.getRepeatedStringListAsBytes();
  }

  //---- Single message
  public static M1.NestedM1 getMutableSingleMessage(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.single_message
    return caretAfterThis.getMutableSingleMessage();
  }
  // no "OrBuilder" variant.
  // no merge

  //---- Repeated message
  public static List<M1.NestedM1> getMutableRepeatedMessageList(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.repeated_message
    return caretAfterThis.getMutableRepeatedMessageList();
  }

  public static M1 addAllRepeatedMessage(M1 caretAfterThis, List<? extends M1.NestedM1> values) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.repeated_message
    return caretAfterThis.addAllRepeatedMessage(values);
  }

  //---- Single enum
  public static Shapes getSingleEnum(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.single_enum
    return caretAfterThis.getSingleEnum();
  }

  public static M1 setSingleEnum(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.single_enum
    return caretAfterThis.setSingleEnum(
        // EXPECT-NEXT: Proto2Mutable.proto / Shapes
        /* caretAfterThis */ Shapes.
            // EXPECT-NEXT: Proto2Mutable.proto / CIRCLE
            /* caretAfterThis */ CIRCLE);
  }

  // EXPECT-NEXT: Proto2Mutable.proto / weirdEnum_name
  public static /* caretAfterThis */ weirdEnum_name checkWeirdEnum() {
    // EXPECT-NEXT: Proto2Mutable.proto / weirdValue_name
    return weirdEnum_name./* caretAfterThis */ weirdValue_name;
  }

  //---- Map
  public static Map<Integer, Integer> getTestMap(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.test_map
    return caretAfterThis.getTestMap();
  }

  public static Map<Integer, Integer> getMutableTestMap(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.test_map
    return caretAfterThis.getMutableTestMap();
  }

  public static M1 putAllTestMap(M1 caretAfterThis, Map<Integer, Integer> values) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.test_map
    return caretAfterThis.putAllTestMap(values);
  }

  //---- Oneof

  public static TestOneofCase getOneofFieldCase(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.test_oneof
    return caretAfterThis.getTestOneofCase();
  }

  public static M1 clearOneofField(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.test_oneof
    return caretAfterThis.clearTestOneof();
  }

  public static TestOneofCase enumMethods(int value) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.test_oneof
    return TestOneofCase./* caretAfterThis */ forNumber(value);
  }

  public static TestOneofCase oneofEnumValue(int x) {
    if (x > 0) {
      // EXPECT-NEXT: Proto2Mutable.proto / M1.test_oneof
      return /* caretAfterThis */ TestOneofCase.
          // EXPECT-NEXT: Proto2Mutable.proto / M1.string_choice
          /* caretAfterThis */ STRING_CHOICE;
    } else {
      // EXPECT-NEXT: Proto2Mutable.proto / M1.test_oneof
      return TestOneofCase./* caretAfterThis */ TESTONEOF_NOT_SET;
    }
  }

  public static boolean hasIntChoice(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.int_choice
    return caretAfterThis.hasIntChoice();
  }

  public static String getStringChoice(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.string_choice
    return caretAfterThis.getStringChoice();
  }

  //---- Group
  // EXPECT-NEXT: Proto2Mutable.proto / M1.SingleGroupField
  public static M1./* caretAfterThis */ SingleGroupField getSingleGroupField(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.singlegroupfield
    return caretAfterThis.getSingleGroupField();
  }

  //---- Second message type
  public static int getSinglePrimitiveM2(weirdMessage_name caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / weirdMessage_name.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  public static boolean hasRepeatedPrimitiveCountM2(weirdMessage_name caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / weirdMessage_name.Repeated_primitiveCount
    return caretAfterThis.hasRepeatedPrimitiveCount();
  }

  public static int getRepeatedPrimitiveCountM2(weirdMessage_name caretAfterThis) {
    // EXPECT-NEXT: Proto2Mutable.proto / weirdMessage_name.Repeated_primitiveCount
    return caretAfterThis.getRepeatedPrimitiveCount();
  }

  //---- Nested message single primitive
  public static M1.NestedM1 setSinglePrimitive(M1.NestedM1 caretAfterThis, int value) {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.NestedM1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }

  //---- Grab the field constant

  public int getSinglePrimitiveFieldNumber() {
    // EXPECT-NEXT: Proto2Mutable.proto / M1.single_primitive
    return M1./* caretAfterThis */ SINGLE_PRIMITIVE_FIELD_NUMBER;
  }
}
