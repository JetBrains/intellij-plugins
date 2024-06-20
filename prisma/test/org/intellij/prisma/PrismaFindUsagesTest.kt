// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma

class PrismaFindUsagesTest : PrismaTestCase("findUsages") {
  fun testTypeUsages() {
    val usages = myFixture.testFindUsagesUsingAction(getTestFileName())
    assertSameElements(usages.map { it.toString() }, setOf(
      "14|posts| |Post|[]",
      "21|posts| |Post|[]",
    ))
  }
}