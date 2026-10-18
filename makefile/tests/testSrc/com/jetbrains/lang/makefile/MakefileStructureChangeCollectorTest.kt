// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.makefile

import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.vcs.FileStatus
import com.intellij.openapi.vcs.changes.StructureChange
import com.intellij.openapi.vcs.changes.StructureChangeCollector
import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@TestApplication
internal class MakefileStructureChangeCollectorTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  @Test
  fun `recipe changes retain the target and omit unchanged targets`(): Unit = timeoutRunBlocking {
    val before = "changed:\n\techo old\nremoved:\n\techo removed\nstable:\n\techo stable\n"
    val after = "changed:\n\techo new\nadded:\n\techo added\nstable:\n\techo stable\n"
    val changes = collect(before, after)

    assertEquals(
      mapOf("changed" to FileStatus.MODIFIED, "added" to FileStatus.ADDED, "removed" to FileStatus.DELETED),
      changes.associate { it.text to it.status },
    )
    assertEquals(after.indexOf("changed:"), changes.single { it.text == "changed" }.offset)
    assertEquals(after.indexOf("added:"), changes.single { it.text == "added" }.offset)
    assertNull(changes.single { it.text == "removed" }.offset)
  }

  @Test
  fun `targets that share a recipe retain separate navigation offsets`(): Unit = timeoutRunBlocking {
    val before = "first second:\n\techo old\n"
    val after = "first second:\n\techo new\n"
    val changes = collect(before, after)

    assertEquals(mapOf("first" to 0, "second" to 6), changes.associate { it.text to it.offset })
    assertEquals(mapOf("first" to FileStatus.MODIFIED, "second" to FileStatus.MODIFIED), changes.associate { it.text to it.status })
  }

  private suspend fun collect(before: String, after: String): List<StructureChange> = smartReadAction(projectFixture.get()) {
    val project = projectFixture.get()
    val factory = PsiFileFactory.getInstance(project)
    val original = factory.createFileFromText("Makefile", MakefileFileType, after, 0, true)
    val beforeFile = requireNotNull(StructureChangeCollector.createSnapshot(original, before))
    val afterFile = requireNotNull(StructureChangeCollector.createSnapshot(original, after))
    StructureChangeCollector.collect(beforeFile, afterFile)
  }
}
