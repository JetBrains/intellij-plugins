package org.angular2.library.forms

import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2FormsCodeCompletionTest : Angular2TestCase("library/forms/completion") {

  fun testNestedFormGroupControlAttribute() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "formGroupName=\"<caret>name\"",
      "formControlName=\"<caret>email\"",
      "formControlName=\"<caret>first\"",
      "formGroupName=\"<caret>foo\"",
      "formControlName=\"<caret>bar\"",
      "formControlName=\"<caret>baz\"",
    ))

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

  fun testNestedFormGroupGetArrayLiteral() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "get(['<caret>name', 'foo', 'bar'])",
      "get(['name', '<caret>foo', 'bar'])",
      "get(['name', 'foo', '<caret>bar'])",
      "get(['email', '<caret>first'])",
      "get(['foo', '<caret>first'])",
    ))

  fun testNestedFormArrayControlAttribute() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "formArrayName=\"<caret>first\"",
      "formControlName=\"<caret>12\"",
      "formGroupName=\"<caret>group\"",
      "formArrayName=\"<caret>members\"",
      "formControlName=\"<caret>23\"",
    ))

  fun testNestedFormArrayGetArrayLiteral() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "get(['<caret>first', '12'])",
      "get(['first', '<caret>12'])",
      "get(['group', '<caret>members', '12'])",
      "get(['group', 'members', '<caret>12'])",
    ))

  fun testNestedFormArrayGetLiteral() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "get('<caret>first.12')",
      "get('first.<caret>12')",
      "get('group.<caret>members.12')",
      "get('group.members.<caret>12')",
    ))

  fun testFormBuilderInFieldInitializer() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "get(\"<caret>name.more.foo\")",
      "get(\"name.<caret>more.foo\")",
      "get(\"name.more.<caret>foo\")",
      "get([\"name\", \"more\", \"<caret>foo\"])",
      "formGroupName=\"<caret>more\"",
    ))

  fun testFormBuilderInConstructor() =
    doLookupTest(Angular2TestModule.ANGULAR_FORMS_17_3_0, Angular2TestModule.ANGULAR_CORE_17_3_0, locations = listOf(
      "get(\"<caret>name.more.foo\")",
      "get(\"name.<caret>more.foo\")",
      "get(\"name.more.<caret>foo\")",
      "get([\"name\", \"more\", \"<caret>foo\"])",
      "formGroupName=\"<caret>more\"",
    ))
}