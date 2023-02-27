/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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