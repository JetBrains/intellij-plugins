// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil

import org.intellij.terraform.hil.GoUtil.isBoolean
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoUtilTest {
  @Test
  fun testIsBoolean() {
    assertTrue(isBoolean("1"))
    assertTrue(isBoolean("0"))
    assertTrue(isBoolean("t"))
    assertTrue(isBoolean("f"))
    assertTrue(isBoolean("T"))
    assertTrue(isBoolean("F"))
    assertTrue(isBoolean("true"))
    assertTrue(isBoolean("True"))
    assertTrue(isBoolean("TRUE"))
    assertTrue(isBoolean("false"))
    assertTrue(isBoolean("False"))
    assertTrue(isBoolean("FALSE"))

    assertFalse(isBoolean("  FALSE  "))
    assertFalse(isBoolean("10"))
    assertFalse(isBoolean("-1"))
    assertFalse(isBoolean("yes"))
    assertFalse(isBoolean("no"))
    assertFalse(isBoolean(""))
  }
}