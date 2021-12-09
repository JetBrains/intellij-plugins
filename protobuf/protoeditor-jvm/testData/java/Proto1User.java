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

import com.test.api_v1.M1;
import com.test.api_v1.M1_NestedM1;
import com.test.api_v1.M1_TestMapEntry;
import com.test.api_v1.weirdMessage_name;

import java.util.Iterator;
import java.util.List;

/**
 * Test references to generated java API 1 protobuf code.
 *
 * <p>We use a variable (or comment) {@link
 * idea.plugin.protoeditor.java.PbJavaGotoDeclarationHandlerTest#CARET_MARKER} to
 * mark where to place a caret for testing. This makes the file compilable, and so that we can try
 * out the protobuf plugin on this file in an IDE interactively.
 *
 * <p>We specify test expectations with {@link
 * idea.plugin.protoeditor.gencodeutils.GotoExpectationMarker#EXPECT_MARKER}.
 */
@SuppressWarnings("unused") // Loaded by IntelliJ test.
public class Proto1User {

  //---- Message level stuff (not pertaining to a specific field)

  // EXPECT-NEXT: Proto1.proto / M1.NestedM1
  public static /*caretAfterThis */ M1_NestedM1 messageType(M1_NestedM1 x) {
    return x;
  }

  public static M1 messageMember(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1
    return caretAfterThis.getDefaultInstanceForType();
  }

  //---- Single primitive
  public static boolean hasSinglePrimitive(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.single_primitive
    return caretAfterThis.hasSinglePrimitive();
  }

  public static int getSinglePrimitive(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  public static M1 setSinglePrimitive(M1 caretAfterThis, int value) {
    // EXPECT-NEXT: Proto1.proto / M1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }

  public static M1 clearSinglePrimitive(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.single_primitive
    return caretAfterThis.clearSinglePrimitive();
  }

  //---- Repeated primitive
  public static int getRepeatedPrimitiveCount(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_primitive
    return caretAfterThis.repeatedPrimitiveSize();
  }

  public static List<Integer> getRepeatedPrimitiveList(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_primitive
    return caretAfterThis.repeatedPrimitives();
  }

  public static Iterator<Integer> getRepeatedPrimitiveIterator(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_primitive
    return caretAfterThis.repeatedPrimitiveIterator();
  }

  public static int getRepeatedPrimitive(M1 caretAfterThis, int index) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_primitive
    return caretAfterThis.getRepeatedPrimitive(index);
  }

  public static List<Integer> mutableAllRepeatedPrimitive(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_primitive
    return caretAfterThis.mutableRepeatedPrimitives();
  }

  public static M1 addRepeatedPrimitive(M1 caretAfterThis, int value) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_primitive
    return caretAfterThis.addRepeatedPrimitive(value);
  }
  // No add all variant.

  public static M1 setRepeatedPrimitive(M1 caretAfterThis, int index, int value) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_primitive
    return caretAfterThis.setRepeatedPrimitive(index, value);
  }

  public static M1 clearRepeatedPrimitive(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_primitive
    return caretAfterThis.clearRepeatedPrimitive();
  }

  //---- Single string: just the extra method *names* (ignoring overloads).
  public static byte[] getSingleStringAsBytes(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.single_string
    return caretAfterThis.getSingleStringAsBytes();
  }

  public static M1 setSingleStringAsBytes(M1 caretAfterThis, byte[] value) {
    // EXPECT-NEXT: Proto1.proto / M1.single_string
    return caretAfterThis.setSingleStringAsBytes(value);
  }

  //---- Repeated string
  public static List<byte[]> getRepeatedStringListAsBytes(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_string
    return caretAfterThis.repeatedStringsAsBytes();
  }

  public static Iterator<byte[]> getRepeatedStringIteratorAsBytes(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_string
    return caretAfterThis.repeatedStringAsBytesIterator();
  }

  public static M1 addRepeatedStringListAsBytes(M1 caretAfterThis, byte[] values) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_string
    return caretAfterThis.addRepeatedStringAsBytes(values);
  }

  //---- Single message
  public static M1_NestedM1 getMutableSingleMessage(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.single_message
    return caretAfterThis.getMutableSingleMessage();
  }

  // no "OrBuilder" variant.
  // no merge

  //---- Repeated message
  public static M1_NestedM1 insertRepeatedMessage(M1 caretAfterThis, int index, M1_NestedM1 value) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_message
    return caretAfterThis.insertRepeatedMessage(index, value);
  }

  public static M1_NestedM1 removeRepeatedMessage(M1 caretAfterThis, int index) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_message
    return caretAfterThis.removeRepeatedMessage(index);
  }

  //---- Single enum (just represents enums as ints).
  public static int getSingleEnum(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.single_enum
    return caretAfterThis.getSingleEnum();
  }

  public static M1 setSingleEnum(M1 caretAfterThis, int value) {
    // EXPECT-NEXT: Proto1.proto / M1.single_enum
    return caretAfterThis.setSingleEnum(value);
  }

  //---- Single bytes (basically like strings)
  public static byte[] getSingleBytesAsBytes(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.single_bytes
    return caretAfterThis.getSingleBytesAsBytes();
  }

  //---- Repeated bytes
  public static Iterator<byte[]> getRepeatedBytesIteratorAsBytes(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_bytes
    return caretAfterThis.repeatedBytesAsBytesIterator();
  }

  //---- Single bool (oddly, uses isFoo() instead of getFoo()).
  public static boolean isSingleBool(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.single_bool
    return caretAfterThis.isSingleBool();
  }

  public static M1 setSingleBool(M1 caretAfterThis, boolean value) {
    // EXPECT-NEXT: Proto1.proto / M1.single_bool
    return caretAfterThis.setSingleBool(value);
  }

  //---- Repeated bool
  public static boolean isRepeatedBool(M1 caretAfterThis, int index) {
    // EXPECT-NEXT: Proto1.proto / M1.repeated_bool
    return caretAfterThis.isRepeatedBool(index);
  }

  //---- Map (treated like repeated message)
  public static M1_TestMapEntry getTestMap(M1 caretAfterThis, int key) {
    // EXPECT-NEXT: Proto1.proto / M1.test_map
    return caretAfterThis.getTestMap(key);
  }

  public static Iterator<M1_TestMapEntry> getTestMapIterator(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.test_map
    return caretAfterThis.testMapIterator();
  }

  public static int getTestMapSize(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.test_map
    return caretAfterThis.testMapSize();
  }

  public static M1_TestMapEntry removeTestMapSize(M1 caretAfterThis, int key) {
    // EXPECT-NEXT: Proto1.proto / M1.test_map
    return caretAfterThis.removeTestMap(key);
  }

  public static M1_TestMapEntry insertTestMapSize(
      M1 caretAfterThis, int key, M1_TestMapEntry value) {
    // EXPECT-NEXT: Proto1.proto / M1.test_map
    return caretAfterThis.insertTestMap(key, value);
  }

  //---- Oneof
  public static boolean hasIntChoice(M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.int_choice
    return caretAfterThis.hasIntChoice();
  }

  public static M1 setStringChoiceAsBytes(M1 caretAfterThis, byte[] value) {
    // EXPECT-NEXT: Proto1.proto / M1.string_choice
    return caretAfterThis.setStringChoiceAsBytes(value);
  }

  //---- Group
  // EXPECT-NEXT: Proto1.proto / M1.SingleGroupField
  public static M1./* caretAfterThis */ SingleGroupField getSingleGroupPrimitive(
      M1 caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / M1.singlegroupfield
    return caretAfterThis.getSingleGroupField();
  }

  //---- Second message type
  public static int getSinglePrimitiveM2(weirdMessage_name caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / weirdMessage_name.single_primitive
    return caretAfterThis.getSinglePrimitive();
  }

  public static boolean hasRepeatedPrimitiveCountM2(weirdMessage_name caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / weirdMessage_name.Repeated_primitiveCount
    return caretAfterThis.hasRepeatedPrimitiveCount();
  }

  public static int getRepeatedPrimitiveCountM2(weirdMessage_name caretAfterThis) {
    // EXPECT-NEXT: Proto1.proto / weirdMessage_name.Repeated_primitiveCount
    return caretAfterThis.getRepeatedPrimitiveCount();
  }

  //---- Nested message single primitive
  public static M1_NestedM1 setSinglePrimitive(M1_NestedM1 caretAfterThis, int value) {
    // EXPECT-NEXT: Proto1.proto / M1.NestedM1.single_primitive
    return caretAfterThis.setSinglePrimitive(value);
  }
}
