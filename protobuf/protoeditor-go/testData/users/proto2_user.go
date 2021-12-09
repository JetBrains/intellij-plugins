// Package proto2user provides dummy uses of proto2 for testing resolving to proto from go.
package proto2user

import pb "proto2_go_proto/go"

// EXPECT-NEXT: proto2.proto / M1
func use(m pb. /* caretAfterThis */ M1) {
  // EXPECT-NEXT: proto2.proto / M1.single_primitive
  m. /* caretAfterThis */ GetSinglePrimitive()
  // EXPECT-NEXT: proto2.proto / M1.single_primitive
  m. /* caretAfterThis */ SinglePrimitive = nil
  // EXPECT-NEXT: proto2.proto / M1.repeated_primitive
  m. /* caretAfterThis */ GetRepeatedPrimitive()
  // EXPECT-NEXT: proto2.proto / M1.repeated_primitive
  m. /* caretAfterThis */ RepeatedPrimitive = nil
  // EXPECT-NEXT: proto2.proto / M1.single_string
  m. /* caretAfterThis */ GetSingleString()
  // EXPECT-NEXT: proto2.proto / M1.single_string
  m. /* caretAfterThis */ SingleString = nil
  // EXPECT-NEXT: proto2.proto / M1.repeated_string
  m. /* caretAfterThis */ GetRepeatedString()
  // EXPECT-NEXT: proto2.proto / M1.repeated_string
  m. /* caretAfterThis */ RepeatedString = nil
  // EXPECT-NEXT: proto2.proto / M1.single_bytes
  m. /* caretAfterThis */ GetSingleBytes()
  // EXPECT-NEXT: proto2.proto / M1.single_bytes
  m. /* caretAfterThis */ SingleBytes = nil
  // EXPECT-NEXT: proto2.proto / M1.repeated_bytes
  m. /* caretAfterThis */ GetRepeatedBytes()
  // EXPECT-NEXT: proto2.proto / M1.repeated_bytes
  m. /* caretAfterThis */ RepeatedBytes = nil
  // EXPECT-NEXT: proto2.proto / Shapes
  var e pb. /* caretAfterThis */ Shapes
  // EXPECT-NEXT: proto2.proto / UNKNOWN
  e = pb. /* caretAfterThis */ Shapes_UNKNOWN
  // EXPECT-NEXT: proto2.proto / TRIANGLE
  e = pb. /* caretAfterThis */ Shapes_TRIANGLE
  // EXPECT-NEXT: proto2.proto / SQUARE
  e = pb. /* caretAfterThis */ Shapes_SQUARE
  // EXPECT-NEXT: proto2.proto / CIRCLE
  e = pb. /* caretAfterThis */ Shapes_CIRCLE
  // EXPECT-NEXT: proto2.proto / M1.single_enum
  m. /* caretAfterThis */ GetSingleEnum()
  // EXPECT-NEXT: proto2.proto / M1.single_enum
  m. /* caretAfterThis */ SingleEnum = &e
  // EXPECT-NEXT: proto2.proto / M1.repeated_enum
  m. /* caretAfterThis */ GetRepeatedEnum()
  // EXPECT-NEXT: proto2.proto / M1.repeated_enum
  m. /* caretAfterThis */ RepeatedEnum = nil
  // EXPECT-NEXT: proto2.proto / M1.single_bool
  m. /* caretAfterThis */ GetSingleBool()
  // EXPECT-NEXT: proto2.proto / M1.single_bool
  m. /* caretAfterThis */ SingleBool = nil
  // EXPECT-NEXT: proto2.proto / M1.repeated_bool
  m. /* caretAfterThis */ GetRepeatedBool()
  // EXPECT-NEXT: proto2.proto / M1.repeated_bool
  m. /* caretAfterThis */ RepeatedBool = nil
  // EXPECT-NEXT: proto2.proto / M1.NestedM1
  var nm pb. /* caretAfterThis */ M1_NestedM1
  // EXPECT-NEXT: proto2.proto / M1.NestedM1
  nm = pb. /* caretAfterThis */ M1_NestedM1{}
  // EXPECT-NEXT: proto2.proto / M1.NestedM1.single_primitive
  nm. /* caretAfterThis */ GetSinglePrimitive()
  // EXPECT-NEXT: proto2.proto / M1.NestedM1.single_primitive
  nm. /* caretAfterThis */ SinglePrimitive = nil
  // EXPECT-NEXT: proto2.proto / M1.NestedM1.repeated_primitive
  nm. /* caretAfterThis */ GetRepeatedPrimitive()
  // EXPECT-NEXT: proto2.proto / M1.NestedM1.repeated_primitive
  nm. /* caretAfterThis */ RepeatedPrimitive = nil
  // EXPECT-NEXT: proto2.proto / M1.NestedM1.NestedEnum
  var nmne pb. /* caretAfterThis */ M1_NestedM1_NestedEnum
  // EXPECT-NEXT: proto2.proto / M1.NestedM1.UNKNOWN
  nmne = pb. /* caretAfterThis */ M1_NestedM1_UNKNOWN
  // EXPECT-NEXT: proto2.proto / M1.NestedM1.KNOWN_KNOWNS
  nmne = pb. /* caretAfterThis */ M1_NestedM1_KNOWN_KNOWNS
  // EXPECT-NEXT: proto2.proto / M1.NestedM1.KNOWN_UNKNOWNS
  nmne = pb. /* caretAfterThis */ M1_NestedM1_KNOWN_UNKNOWNS
  // EXPECT-NEXT: proto2.proto / M1.NestedM1.UNKNOWN_UNKNOWNS
  nmne = pb. /* caretAfterThis */ M1_NestedM1_UNKNOWN_UNKNOWNS
  // EXPECT-NEXT: proto2.proto / M1.NestedM1.single_enum
  nm. /* caretAfterThis */ GetSingleEnum()
  // EXPECT-NEXT: proto2.proto / M1.NestedM1.single_enum
  nm. /* caretAfterThis */ SingleEnum = &nmne
  // EXPECT-NEXT: proto2.proto / M1.single_message
  m. /* caretAfterThis */ GetSingleMessage()
  // EXPECT-NEXT: proto2.proto / M1.single_message
  m. /* caretAfterThis */ SingleMessage = nil
  // EXPECT-NEXT: proto2.proto / M1.repeated_message
  m. /* caretAfterThis */ GetRepeatedMessage()
  // EXPECT-NEXT: proto2.proto / M1.repeated_message
  m. /* caretAfterThis */ RepeatedMessage = nil
  // EXPECT-NEXT: proto2.proto / M1.test_map
  m. /* caretAfterThis */ GetTestMap()
  // EXPECT-NEXT: proto2.proto / M1.test_map
  m. /* caretAfterThis */ TestMap = nil
  // EXPECT-NEXT: proto2.proto / M1.test_oneof
  switch m. /* caretAfterThis */ GetTestOneof().(type) {
  // EXPECT-NEXT: proto2.proto / M1.int_choice
  case *pb. /* caretAfterThis */ M1_IntChoice:
    // EXPECT-NEXT: proto2.proto / M1.int_choice
    m. /* caretAfterThis */ GetIntChoice()
    // EXPECT-NEXT: proto2.proto / M1.string_choice
  case *pb. /* caretAfterThis */ M1_StringChoice:
    // EXPECT-NEXT: proto2.proto / M1.string_choice
    m. /* caretAfterThis */ GetStringChoice()
  }
  // EXPECT-NEXT: proto2.proto / M1.test_oneof
  m. /* caretAfterThis */ TestOneof = nil
  // EXPECT-NEXT: proto2.proto / M1.NestedEnum
  var ne pb. /* caretAfterThis */ M1_NestedEnum
  // EXPECT-NEXT: proto2.proto / M1.UNKNOWN
  ne = pb. /* caretAfterThis */ M1_UNKNOWN
  // EXPECT-NEXT: proto2.proto / M1.RED
  ne = pb. /* caretAfterThis */ M1_RED
  // EXPECT-NEXT: proto2.proto / M1.GREEN
  ne = pb. /* caretAfterThis */ M1_GREEN
  // EXPECT-NEXT: proto2.proto / M1.BLUE
  ne = pb. /* caretAfterThis */ M1_BLUE
  // EXPECT-NEXT: proto2.proto / M1.single_enum2
  m. /* caretAfterThis */ GetSingleEnum2()
  // EXPECT-NEXT: proto2.proto / M1.single_enum2
  m. /* caretAfterThis */ SingleEnum2 = &ne
  // EXPECT-NEXT: proto2.proto / M1.single_string_piece
  m. /* caretAfterThis */ GetSingleStringPiece()
  // EXPECT-NEXT: proto2.proto / M1.single_string_piece
  m. /* caretAfterThis */ SingleStringPiece = nil
  // EXPECT-NEXT: proto2.proto / M1.repeated_string_piece
  m. /* caretAfterThis */ GetRepeatedStringPiece()
  // EXPECT-NEXT: proto2.proto / M1.repeated_string_piece
  m. /* caretAfterThis */ RepeatedStringPiece = nil
}
