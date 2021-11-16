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

import com.google.protobuf.ByteString;
import com.test.api_v2.Proto2.M1;
import com.test.api_v2.Proto2.M1.TestOneofCase;
import com.test.api_v2.Proto2.M1OrBuilder;
import com.test.api_v2.Proto2.Shapes;
import com.test.api_v2.Proto2.weirdEnum_name;
import com.test.api_v2.Proto2.weirdMessage_name;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Test references to generated java API 2 protobuf code. */
@SuppressWarnings("unused") // Loaded by IntelliJ test.
public class Proto2User {

  //---- Message level stuff (not pertaining to a specific field)

  // EXPECT-NEXT: Proto2.proto / M1.NestedM1
  public static M1./* caretAfterThis */ NestedM1 messageType(M1.NestedM1 x) {
    return x;
  }

  // EXPECT-NEXT: Proto2.proto / M1
  public static /* caretAfterThis */ M1OrBuilder orBuilderType(M1OrBuilder x) {
    return x;
  }

  public static M1.Builder toBuilder(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1
    return caretAfterThis.toBuilder();
  }

  public static <TypeParameter extends M1> M1.Builder testTypeParam(
      // EXPECT-NEXT: Proto2User.java / TypeParameter
      /* caretAfterThis */ TypeParameter caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1
    return caretAfterThis.toBuilder();
  }

  public static M1 testVariableReference(M1 variable) {
    // EXPECT-NEXT: Proto2User.java / variable
    return /* caretAfterThis */ variable;
  }

  //---- Single primitive
  public static boolean hasSinglePrimitive(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.single_primitive
    return caretAfterThis.hasSinglePrimitive();
  }

  public static int getSinglePrimitive(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  public static M1.Builder setSinglePrimitive(M1.Builder caretAfterThis, int value) {
    // EXPECT-NEXT: Proto2.proto / M1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }

  public static M1.Builder clearSinglePrimitive(M1.Builder caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.single_primitive
    return caretAfterThis.clearSinglePrimitive();
  }

  //---- Repeated primitive
  public static int getRepeatedPrimitiveCount(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.repeated_primitive
    return caretAfterThis.getRepeatedPrimitiveCount();
  }

  public static List<Integer> getRepeatedPrimitiveList(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.repeated_primitive
    return caretAfterThis.getRepeatedPrimitiveList();
  }
  // No iterator variant.

  public static int getRepeatedPrimitive(M1 caretAfterThis, int index) {
    // EXPECT-NEXT: Proto2.proto / M1.repeated_primitive
    return caretAfterThis.getRepeatedPrimitive(index);
  }
  // No mutable all variant.

  public static M1.Builder addRepeatedPrimitive(M1.Builder caretAfterThis, int value) {
    // EXPECT-NEXT: Proto2.proto / M1.repeated_primitive
    return caretAfterThis.addRepeatedPrimitive(value);
  }

  public static M1.Builder addAllRepeatedPrimitive(
      M1.Builder caretAfterThis, Iterable<? extends Integer> value) {
    // EXPECT-NEXT: Proto2.proto / M1.repeated_primitive
    return caretAfterThis.addAllRepeatedPrimitive(value);
  }

  public static M1.Builder setRepeatedPrimitive(M1.Builder caretAfterThis, int index, int value) {
    // EXPECT-NEXT: Proto2.proto / M1.repeated_primitive
    return caretAfterThis.setRepeatedPrimitive(index, value);
  }

  public static M1.Builder clearRepeatedPrimitive(M1.Builder caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.repeated_primitive
    return caretAfterThis.clearRepeatedPrimitive();
  }

  //---- Single string: just the extra method *names* (ignoring overloads).
  public static ByteString getSingleStringAsBytes(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.single_string
    return caretAfterThis.getSingleStringBytes();
  }

  public static M1.Builder setSingleStringBytes(M1.Builder caretAfterThis, ByteString value) {
    // EXPECT-NEXT: Proto2.proto / M1.single_string
    return caretAfterThis.setSingleStringBytes(value);
  }

  //---- Repeated string (nothing special vs "optional string" and "repeated <t>".
  public static List<String> getRepeatedStringList(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.repeated_string
    return caretAfterThis.getRepeatedStringList();
  }

  //---- Single message
  public static M1.NestedM1OrBuilder getSingleMessageOrBuilder(M1OrBuilder caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.single_message
    return caretAfterThis.getSingleMessageOrBuilder();
  }

  public static M1.NestedM1.Builder getSingleMessageBuilder(M1.Builder caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.single_message
    return caretAfterThis.getSingleMessageBuilder();
  }

  public static M1.NestedM1 getSingleMessage(M1OrBuilder caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.single_message
    return caretAfterThis.getSingleMessage();
  }

  public static M1.Builder mergeSingleMessage(M1.Builder caretAfterThis, M1.NestedM1 value) {
    // EXPECT-NEXT: Proto2.proto / M1.single_message
    return caretAfterThis.mergeSingleMessage(value);
  }
  // no mutable variant

  //---- Repeated message
  public static List<? extends M1.NestedM1OrBuilder> getRepeatedMessageOrBuilderList(
      M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.repeated_message
    return caretAfterThis.getRepeatedMessageOrBuilderList();
  }

  public static M1.NestedM1.Builder addRepeatedMessageBuilder(M1.Builder caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.repeated_message
    return caretAfterThis.addRepeatedMessageBuilder();
  }

  public static M1.Builder removeRepeatedMessage(M1.Builder caretAfterThis, int index) {
    // EXPECT-NEXT: Proto2.proto / M1.repeated_message
    return caretAfterThis.removeRepeatedMessage(index);
  }

  //---- Single enum
  public static Shapes getSingleEnum(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.single_enum
    return caretAfterThis.getSingleEnum();
  }

  public static Shapes enumForValue(int value) {
    // EXPECT-NEXT: Proto2.proto / Shapes
    return Shapes./* caretAfterThis */ forNumber(value);
  }

  public static Shapes enumValueOf(String name) {
    // EXPECT-NEXT: Proto2.proto / Shapes
    return Shapes./* caretAfterThis */ valueOf(name);
  }

  public static M1.Builder setSingleEnum(M1.Builder caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.single_enum
    return caretAfterThis.setSingleEnum(
        // EXPECT-NEXT: Proto2.proto / Shapes
        /* caretAfterThis */ Shapes.
            // EXPECT-NEXT: Proto2.proto / CIRCLE
            /* caretAfterThis */ CIRCLE);
  }

  // EXPECT-NEXT: Proto2.proto / weirdEnum_name
  public static /* caretAfterThis */ weirdEnum_name checkWeirdEnum() {
    // EXPECT-NEXT: Proto2.proto / weirdValue_name
    return weirdEnum_name./* caretAfterThis */ weirdValue_name;
  }

  //---- Single bytes (no "AsBytes", unlike string).
  public static ByteString getSingleBytes(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.single_bytes
    return caretAfterThis.getSingleBytes();
  }

  //---- Repeated bytes
  public static List<ByteString> getRepeatedBytesList(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.repeated_bytes
    return caretAfterThis.getRepeatedBytesList();
  }

  //---- Map
  public static int getTestMapFieldOrDefault(M1 caretAfterThis, int key) {
    // EXPECT-NEXT: Proto2.proto / M1.test_map
    return caretAfterThis.getTestMapOrDefault(key, 0);
  }

  public static int getTestMapFieldOrThrow(M1 caretAfterThis, int key) {
    // EXPECT-NEXT: Proto2.proto / M1.test_map
    return caretAfterThis.getTestMapOrThrow(key);
  }

  public static boolean containsTestMap(M1 caretAfterThis, int key) {
    // EXPECT-NEXT: Proto2.proto / M1.test_map
    return caretAfterThis.containsTestMap(key);
  }

  public static int getTestMapFieldCount(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.test_map
    return caretAfterThis.getTestMapCount();
  }

  public static Map<Integer, Integer> getTestMapFieldMap(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.test_map
    return caretAfterThis.getTestMapMap();
  }

  public static M1.Builder removeTestMapField(M1.Builder caretAfterThis, int key) {
    // EXPECT-NEXT: Proto2.proto / M1.test_map
    return caretAfterThis.removeTestMap(key);
  }

  public static M1.Builder putTestMapField(M1.Builder caretAfterThis, int key, int value) {
    // EXPECT-NEXT: Proto2.proto / M1.test_map
    return caretAfterThis.putTestMap(key, value);
  }

  public static M1.Builder putAllTestMapField(M1.Builder caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.test_map
    return caretAfterThis.putAllTestMap(Collections.emptyMap());
  }

  //---- Oneof

  public static TestOneofCase getOneofFieldCase(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.test_oneof
    return caretAfterThis.getTestOneofCase();
  }

  public static M1.Builder clearOneofField(M1.Builder caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.test_oneof
    return caretAfterThis.clearTestOneof();
  }

  public static TestOneofCase oneofEnumValue(int x) {
    if (x > 0) {
      // EXPECT-NEXT: Proto2.proto / M1.test_oneof
      return /* caretAfterThis */ TestOneofCase.
          // EXPECT-NEXT: Proto2.proto / M1.string_choice
          /* caretAfterThis */ STRING_CHOICE;
    } else {
      // EXPECT-NEXT: Proto2.proto / M1.test_oneof
      return TestOneofCase./* caretAfterThis */ TESTONEOF_NOT_SET;
    }
  }

  public static TestOneofCase enumMethods(int value) {
    // EXPECT-NEXT: Proto2.proto / M1.test_oneof
    return TestOneofCase./* caretAfterThis */ forNumber(value);
  }

  public static boolean hasIntChoice(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.int_choice
    return caretAfterThis.hasIntChoice();
  }

  public static M1.Builder setStringChoiceBytes(M1.Builder caretAfterThis, ByteString value) {
    // EXPECT-NEXT: Proto2.proto / M1.string_choice
    return caretAfterThis.setStringChoiceBytes(value);
  }

  //---- Group
  // EXPECT-NEXT: Proto2.proto / M1.SingleGroupField
  public static M1./* caretAfterThis */ SingleGroupField getSingleGroupField(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / M1.singlegroupfield
    return caretAfterThis.getSingleGroupField();
  }

  //---- Second message type
  // Make sure we go to the right class even though M1 and M2 have the same field name.
  public static int getSinglePrimitiveM2(weirdMessage_name caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / weirdMessage_name.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  public static boolean hasRepeatedPrimitiveCountM2(weirdMessage_name caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / weirdMessage_name.Repeated_primitiveCount
    return caretAfterThis.hasRepeatedPrimitiveCount();
  }

  public static int getRepeatedPrimitiveCountM2(weirdMessage_name caretAfterThis) {
    // EXPECT-NEXT: Proto2.proto / weirdMessage_name.Repeated_primitiveCount
    return caretAfterThis.getRepeatedPrimitiveCount();
  }

  //---- Nested message single primitive
  public static M1.NestedM1.Builder setSinglePrimitive(
      M1.NestedM1.Builder caretAfterThis, int value) {
    // EXPECT-NEXT: Proto2.proto / M1.NestedM1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }

  //---- Grab the field constant

  public int getSinglePrimitiveFieldNumber() {
    // EXPECT-NEXT: Proto2.proto / M1.single_primitive
    return M1./* caretAfterThis */ SINGLE_PRIMITIVE_FIELD_NUMBER;
  }
}
