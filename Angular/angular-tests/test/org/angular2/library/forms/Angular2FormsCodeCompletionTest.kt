package org.angular2.library.forms

import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.SkipTsGoProxy
import org.angular2.TestTsGoProxy
import org.angular2.TestTsNode
import org.junit.Test

@TestTsNode
@TestTsGoProxy
class Angular2FormsCodeCompletionTest : Angular2TestCase("library/forms/completion") {

  @Test
  fun testNestedFormGroupControlAttribute() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "formGroupName=\"<caret>name\"",
      "formControlName=\"<caret>email\"",
      "formControlName=\"<caret>first\"",
      "formGroupName=\"<caret>foo\"",
      "formControlName=\"<caret>bar\"",
      "formControlName=\"<caret>baz\"",
    ))

  @Test
  fun testNestedFormGroupGetLiteral() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "get('<caret>name')",
      "get('name.<caret>first')",
      "get('name.first<caret>')",
      "get('name.first<caret>.')",
      "get('name.first.<caret>')",
      "get('name.foo.<caret>bar')",
      "get('email.<caret>')",
      "get('foo.<caret>')",
    ))

  @Test
  fun testNestedFormGroupGetArrayLiteral() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "get(['<caret>name', 'foo', 'bar'])",
      "get(['name', '<caret>foo', 'bar'])",
      "get(['name', 'foo', '<caret>bar'])",
      "get(['email', '<caret>first'])",
      "get(['foo', '<caret>first'])",
    ))

  @Test
  fun testNestedFormArrayControlAttribute() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "formArrayName=\"<caret>first\"",
      "formControlName=\"<caret>12\"",
      "formGroupName=\"<caret>group\"",
      "formArrayName=\"<caret>members\"",
      "formControlName=\"<caret>23\"",
    ))

  @Test
  fun testNestedFormArrayGetArrayLiteral() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "get(['<caret>first', '12'])",
      "get(['first', '<caret>12'])",
      "get(['group', '<caret>members', '12'])",
      "get(['group', 'members', '<caret>12'])",
    ))

  @Test
  fun testNestedFormArrayGetLiteral() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "get('<caret>first.12')",
      "get('first.<caret>12')",
      "get('group.<caret>members.12')",
      "get('group.members.<caret>12')",
    ))

  @Test
  fun testFormBuilderInFieldInitializer() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "get(\"<caret>name.more.foo\")",
      "get(\"name.<caret>more.foo\")",
      "get(\"name.more.<caret>foo\")",
      "get([\"name\", \"more\", \"<caret>foo\"])",
      "formGroupName=\"<caret>more\"",
    ))

  @Test
  fun testFormBuilderInConstructor() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "get(\"<caret>name.more.foo\")",
      "get(\"name.<caret>more.foo\")",
      "get(\"name.more.<caret>foo\")",
      "get([\"name\", \"more\", \"<caret>foo\"])",
      "formGroupName=\"<caret>more\"",
    ))
}