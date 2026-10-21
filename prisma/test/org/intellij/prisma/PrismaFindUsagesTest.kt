// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma

import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@TestApplication
class PrismaFindUsagesTest {
  private val prisma: PrismaFixture by prismaFixture("findUsages")

  @Test
  fun testTypeUsages() {
    doTest(
      "14|posts| |Post|[]",
      "21|posts| |Post|[]",
    )
  }

  @Test
  fun testSchemaUsages() {
    doTest(
      """15|@@|schema|(|"|base-schema|"|)""",
      """21|@@|schema|(|"|base-schema|"|)"""
    )
  }

  private fun doTest(vararg expectedUsages: String) {
    val usages = prisma.fixture.testFindUsagesUsingAction(prisma.getTestFileName())
    assertThat(usages.map { it.toString() }).containsExactlyInAnyOrderElementsOf(expectedUsages.toSet())
  }
}