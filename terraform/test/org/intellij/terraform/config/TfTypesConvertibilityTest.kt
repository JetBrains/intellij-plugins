// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.testFramework.UsefulTestCase
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.model.Types.Any
import org.intellij.terraform.config.model.Types.Boolean
import org.intellij.terraform.config.model.Types.Null
import org.intellij.terraform.config.model.Types.Number
import org.intellij.terraform.config.model.Types.String

class TfTypesConvertibilityTest : UsefulTestCase() {
  fun testPrimitiveTypes() {
    doTest(String, Number, true)
    doTest(String, Number, true)
    doTest(String, Boolean, true)
    doTest(String, Null, false)
    doTest(String, Any, true)

    doTest(Number, String, true)
    doTest(Number, String, true)
    doTest(Boolean, String, true)
    doTest(Null, String, true)
    doTest(Any, String, true)

    doTest(Number, Boolean, false)
    doTest(Boolean, Number, false)
  }

  fun testListTypes() {
    doTest(ListType(null), ListType(null), true)
    doTest(ListType(null), ListType(Any), true)
    doTest(ListType(Any), ListType(Any), true)
    doTest(ListType(Any), ListType(null), true)
    doTest(ListType(ListType(Any)), ListType(null), true)
    doTest(ListType(ListType(Any)), ListType(Any), true)

    doTest(ListType(Number), ListType(null), true)
    doTest(ListType(Number), ListType(String), true)
    doTest(ListType(Number), ListType(Any), true)
    doTest(ListType(Any), ListType(Number), true)

    doTest(ListType(Number), ListType(Boolean), false)
    doTest(ListType(Boolean), ListType(Number), false)
  }

  fun testSetTypes() {
    doTest(SetType(null), SetType(null), true)
    doTest(SetType(null), SetType(Any), true)
    doTest(SetType(Any), SetType(Any), true)
    doTest(SetType(Any), SetType(null), true)

    doTest(SetType(Number), SetType(null), true)
    doTest(SetType(Number), SetType(String), true)
    doTest(SetType(Number), SetType(Any), true)
    doTest(SetType(Any), SetType(Number), true)

    doTest(SetType(Number), SetType(Boolean), false)
    doTest(SetType(Boolean), SetType(Number), false)
  }

  fun testListToSetTypes() {
    doTest(ListType(null), SetType(null), true)
    doTest(ListType(null), SetType(Any), true)
    doTest(ListType(Any), SetType(Any), true)
    doTest(ListType(Any), SetType(null), true)
    doTest(ListType(Number), SetType(null), true)
    doTest(ListType(Number), SetType(String), true)
    doTest(ListType(Number), SetType(Any), true)
    doTest(ListType(Any), SetType(Number), true)
    doTest(ListType(Number), SetType(Boolean), false)
    doTest(ListType(Boolean), SetType(Number), false)
  }

  fun testSetToListTypes() {
    doTest(SetType(null), ListType(null), true)
    doTest(SetType(null), ListType(Any), true)
    doTest(SetType(Any), ListType(Any), true)
    doTest(SetType(Any), ListType(null), true)
    doTest(SetType(Number), ListType(null), true)
    doTest(SetType(Number), ListType(String), true)
    doTest(SetType(Number), ListType(Any), true)
    doTest(SetType(Any), ListType(Number), true)
    doTest(SetType(Number), ListType(Boolean), false)
    doTest(SetType(Boolean), ListType(Number), false)
  }

  fun testObjectTypes() {
    doTest(ObjectType(null), ObjectType(null), true)

    doTest(ObjectType(null), ObjectType(mapOf("x" to Any)), true)
    doTest(ObjectType(mapOf("x" to Any)), ObjectType(null), true)

    doTest(ObjectType(mapOf("x" to Any)), ObjectType(mapOf("x" to null)), true)
    doTest(ObjectType(mapOf("x" to null)), ObjectType(mapOf("x" to Any)), true)
    doTest(ObjectType(mapOf("x" to Number)), ObjectType(mapOf("x" to String)), true)
    doTest(ObjectType(mapOf("x" to String)), ObjectType(mapOf("x" to Number)), true)

    doTest(ObjectType(mapOf("x" to Any)), ObjectType(mapOf("y" to Any)), false)
    doTest(ObjectType(mapOf("y" to Any)), ObjectType(mapOf("x" to Any)), false)

    doTest(ObjectType(mapOf("x" to Any)), ObjectType(mapOf("x" to Any, "y" to Any)), false)
    doTest(ObjectType(mapOf("x" to Any, "y" to Any)), ObjectType(mapOf("x" to Any)), true)
  }

  fun testMapTypes() {
    doTest(MapType(null), MapType(null), true)
    doTest(MapType(Any), MapType(null), true)
    doTest(MapType(null), MapType(Any), true)
    doTest(MapType(Any), MapType(Any), true)

    doTest(MapType(Any), MapType(String), true)
    doTest(MapType(String), MapType(Any), true)
    doTest(MapType(String), MapType(Number), true)
    doTest(MapType(Number), MapType(String), true)
  }

  fun testObjectToMapTypes() {
    doTest(ObjectType(null), MapType(null), true)
    doTest(ObjectType(null), MapType(Any), true)
    doTest(ObjectType(mapOf("x" to Any)), MapType(null), true)
    doTest(ObjectType(mapOf("x" to Any)), MapType(Any), true)

    doTest(ObjectType(mapOf("x" to String)), MapType(Any), true)
    doTest(ObjectType(mapOf("x" to Number)), MapType(Any), true)

    // same primitive
    doTest(ObjectType(mapOf("x" to String)), MapType(String), true)
    doTest(ObjectType(mapOf("x" to Number)), MapType(Number), true)

    // convert primitive
    doTest(ObjectType(mapOf("x" to Number)), MapType(String), true)
    doTest(ObjectType(mapOf("x" to String)), MapType(Number), true)
    doTest(ObjectType(mapOf("x" to Number)), MapType(Boolean), false)
  }

  fun testMapToObjectTypes() {
    doTest(MapType(null), ObjectType(null), true)
    doTest(MapType(Any), ObjectType(null), true)
    doTest(MapType(null), ObjectType(mapOf("x" to Any)), true)
    doTest(MapType(Any), ObjectType(mapOf("x" to Any)), true)

    doTest(MapType(Any), ObjectType(mapOf("x" to String)), true)
    doTest(MapType(Any), ObjectType(mapOf("x" to Number)), true)

    // same primitive
    doTest(MapType(String), ObjectType(mapOf("x" to String)), true)
    doTest(MapType(Number), ObjectType(mapOf("x" to Number)), true)

    // convert primitive
    doTest(MapType(String), ObjectType(mapOf("x" to Number)), true)
    doTest(MapType(Number), ObjectType(mapOf("x" to String)), true)
    doTest(MapType(Number), ObjectType(mapOf("x" to Boolean)), false)
  }

  fun testOptionalTypes() {
    doTest(String, OptionalType(String), true)
    doTest(ObjectType(mapOf("x" to String)), ObjectType(mapOf("x" to String, "y" to OptionalType(String))), true)
    doTest(ObjectType(mapOf("x" to String, "y" to OptionalType(String))), ObjectType(mapOf("x" to String)), true)
    doTest(ObjectType(mapOf("x" to String, "y" to OptionalType(String))), ObjectType(mapOf("x" to String, "z" to OptionalType(String))), true)
  }

  private fun doTest(from: Type, to: Type, expected: Boolean) {
    // self check
    assertTrue(from.isConvertibleTo(from))
    assertTrue(to.isConvertibleTo(to))

    assertEquals(expected, from.isConvertibleTo(to))

    // everything should be convertible to Types.Any
    assertTrue(from.isConvertibleTo(Any))
    assertTrue(to.isConvertibleTo(Any))
  }
}