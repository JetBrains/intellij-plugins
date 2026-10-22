// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.makefile

import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.vcs.FileStatus
import com.intellij.openapi.vcs.changes.HunkLines
import com.intellij.openapi.vcs.changes.HunkName
import com.intellij.openapi.vcs.changes.StructuralHunkNamer
import com.intellij.openapi.vcs.changes.StructureChangeCollector
import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
internal class MakefileStructuralHunkNamerTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  @Test
  fun `recipe edits name their targets`(): Unit = timeoutRunBlocking {
    val before = "changed:\n\techo old\nremoved:\n\trm -rf build\nstable:\n\techo stable\n"
    val after = "changed:\n\techo new\nadded:\n\ttouch added.txt\nstable:\n\techo stable\n"
    val name = nameHunks(before, after, HunkLines(1, 4, 1, 4)).single()

    assertEquals(listOf("changed" to FileStatus.MODIFIED, "added" to FileStatus.ADDED, "removed" to FileStatus.DELETED),
                 name.parts.map { it.text to it.status })
  }

  @Test
  fun `targets that share a recipe both name the hunk`(): Unit = timeoutRunBlocking {
    val name = nameHunks("first second:\n\techo old\n", "first second:\n\techo new\n", HunkLines(1, 2, 1, 2)).single()

    assertEquals(listOf("first" to FileStatus.MODIFIED, "second" to FileStatus.MODIFIED), name.parts.map { it.text to it.status })
  }

  private suspend fun nameHunks(before: String, after: String, vararg hunks: HunkLines): List<HunkName> = smartReadAction(projectFixture.get()) {
    val original = PsiFileFactory.getInstance(projectFixture.get()).createFileFromText("Makefile", MakefileFileType, after, 0, true)
    val beforeFile = requireNotNull(StructureChangeCollector.createSnapshot(original, before))
    val afterFile = requireNotNull(StructureChangeCollector.createSnapshot(original, after))
    StructuralHunkNamer.name(beforeFile, afterFile, hunks.asList())
  }
}
