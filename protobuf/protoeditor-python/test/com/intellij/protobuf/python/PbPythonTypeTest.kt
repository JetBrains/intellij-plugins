package com.intellij.protobuf.python

import com.intellij.protobuf.gencodeutils.TypeExpectationMarker
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.inspections.PyCallingNonCallableInspection
import com.jetbrains.python.inspections.PyTypeCheckerInspection
import com.jetbrains.python.inspections.unresolvedReference.PyUnresolvedReferencesInspection
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.types.TypeEvalContext

/**
 * Miscellaneous tests verifying type inference.
 *
 * Checks both inspections and the inferred types of expressions.
 */
class PbPythonTypeTest : PbPythonTestBase() {

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(
      PyTypeCheckerInspection::class.java,
      PyUnresolvedReferencesInspection::class.java,
      PyCallingNonCallableInspection::class.java,
    )
  }

  companion object {
    private const val HEADER = $$"""
      import $importName
      from $importName import *
      
      msg = SomeMessage()
      scalars = msg.scalar_types_message
      wkt = msg.wkt_message
    """
  }

  fun testMessageAttributes() = doTest("""
    msg.<warning descr="'testing.all.SomeMessage (Protobuf Message)' object has no attribute 'nonexistent_field'">nonexistent_field</warning> = 1
    
    # EXPECT-TYPE: int
    SomeMessage.<caret>NESTED_ENUM_VALUE_1
    msg.<caret>NESTED_ENUM_VALUE_1
    msg.<warning descr="'testing.all.SomeMessage (Protobuf Message)' object has no attribute 'NESTED_ENUM_VALUE_1'">NESTED_ENUM_VALUE_1</warning> = 1
    """)

  fun testMessageConstructor() = doTest($$"""
    from datetime import timedelta
    from google.protobuf.any$apiSuffix import Any
    
    SomeMessage(<warning descr="Expected type 'Mapping[bool, google.protobuf.Duration (Protobuf Message) | Mapping] | None', got 'dict[Literal[False], timedelta]' instead">bool_duration_map={False: timedelta(seconds=1)}</warning>)

    SomeMessage(repeated_any_field=[Any()])
    SomeMessage(<warning descr="Expected type 'Iterable[Any | Mapping] | None', got 'list[Literal[\"not Any()\"]]' instead">repeated_any_field=["not Any()"]</warning>)
    
    SomeMessage(repeated_enum_field=[1, 2, 3])
    SomeMessage(<warning descr="Expected type 'Iterable[int | str] | None', got 'list[type[testing.all.SomeEnum (Protobuf Enum)]]' instead">repeated_enum_field=[$importName.SomeEnum]</warning>)
    """)

  fun testEnumNotCallable() = doTest("""
    <warning descr="'Nested_Enum' is not callable">SomeMessage.Nested_Enum()</warning>
    """)

  fun testEnumFields() = doTest("""
    SomeEnum.<warning descr="Unresolved attribute reference 'nonexistent_field' for class 'testing.all.SomeEnum (Protobuf Enum)'">nonexistent_field</warning>
    
    # EXPECT-TYPE: int
    SomeEnum.<caret>ENUM_VALUE_1
    """)

  fun testRepeatedContainerMethods() = doTest($$"""
    from google.protobuf.any$apiSuffix import Any
    
    msg.repeated_string_field.append("string")
    msg.repeated_string_field.<warning descr="Unresolved attribute reference 'add' for class 'RepeatedScalarFieldContainer'">add</warning>("string")
    
    msg.repeated_any_field.append(Any())
    msg.repeated_any_field.add()
    
    msg.repeated_string_field.append(<warning descr="Expected type 'str', got 'Literal[123]' instead">123</warning>)
    """)

  fun testMismatchedMapKeyValueType() = doTest("""
    msg.string_int32_map["key"] = <warning descr="Expected type 'int', got 'Literal[\"not an int\"]' instead">"not an int"</warning>
    msg.string_int32_map[<warning descr="Expected type 'str', got 'Literal[123]' instead">123</warning>] = 456
    """)

  fun testFieldAssignmentTypeMismatch() = doTest("""
    scalars.int32_field = <warning descr="Expected type 'int', got 'Literal[\"not an int\"]' instead">"not an int"</warning>
    """)

  // TODO: shows 2 identical warnings instead of 1
  fun testWrapperMessageFieldAssignment() = doTest($$"""
    from google.protobuf.wrappers$apiSuffix import StringValue

    wkt.<warning descr="'testing.all.WellKnownTypesMessage (Protobuf Message)' object has no attribute 'string_value_field'">string_value_field</warning> = <warning descr="Expected type 'google.protobuf.StringValue (Protobuf Message)', got 'Literal[\"direct assignment\"]' instead">"direct assignment"</warning>
    wkt.<warning descr="'testing.all.WellKnownTypesMessage (Protobuf Message)' object has no attribute 'string_value_field'">string_value_field</warning> = StringValue(value="other")
    wkt.int32_value_field.value = <warning descr="Expected type 'int', got 'Literal[\"not int\"]' instead">"not int"</warning>
    """)

  // TODO
  //fun testRepeatedFieldAssignment() = doTest("""
  //  <warning>msg.repeated_any_field[0]</warning> = Any()
  //  <warning>msg.repeated_any_field[:]</warning> = Any()
  //  """)

  // TODO
  //fun testMapFieldAssignment() = doTest("""
  //  msg.string_int32_map["key"] = 1
  //  msg.int32_any_map[1] = <warning>Any()</warning>
  //  msg.bool_duration_map[True] = <warning>Duration()</warning>
  //  msg.int32_enum_map[1] = msg.NESTED_ENUM_VALUE_1
  //  """)

  fun testScalarFields() = doTest("""
    scalars.double_field = 1.23
    scalars.float_field = -4.56
    scalars.int32_field = -100
    scalars.int64_field = 123456789
    scalars.uint32_field = 200
    scalars.uint64_field = 9876543210
    scalars.sint32_field = -50
    scalars.sint64_field = -12345
    scalars.fixed32_field = 300
    scalars.fixed64_field = 4000
    scalars.sfixed32_field = -60
    scalars.sfixed64_field = -700
    scalars.bool_field = True
    scalars.string_field = "string"
    scalars.bytes_field = b'\xde\xad'
    """)

  private fun doTest(body: String) = runWithGeneratedPb("all.proto") { context ->
    val text = (HEADER.trimIndent() + "\n\n" + body.trimIndent())
      .replace($$"$importName", context.importName)
      .replace($$"$apiSuffix", context.apiVersion.suffix)
    myFixture.configureByText("test.py", text)

    myFixture.checkHighlighting()

    testExpectations(TypeExpectationMarker::parseExpectations) { expectation, lineNumber ->
      val expr = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset).parentOfType<PyExpression>()
                 ?: error("No Python expression found at line $lineNumber")

      val codeAnalysisContext = TypeEvalContext.codeAnalysis(expr.project, expr.containingFile)
      assertType(expectation.expectedType, expr, codeAnalysisContext)

      // TODO
      //assertProjectFilesNotParsed(codeAnalysisContext)
      //
      //val userInitiatedContext = TypeEvalContext.userInitiated(expr.project, expr.containingFile)
      //assertType(expectation.expectedType, expr, userInitiatedContext)
    }
  }
}
