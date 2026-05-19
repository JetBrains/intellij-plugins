package com.intellij.lang.javascript.linter.eslint

import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class EslintUtilFileKindScopeTest : BasePlatformTestCase() {
  private fun setFilesPattern(pattern: String) {
    val configuration = EslintConfiguration.getInstance(project)
    val prev = configuration.extendedState.getState()
    val newState = EslintState.Builder(prev).setFilesPattern(pattern).build()
    configuration.setExtendedState(configuration.isEnabled, newState)
  }

  fun testReturnsNullForFileOutsideContent() { // Match all JS files to isolate the isInContent() branch
    setFilesPattern("**/*.js")

    val psiFile = myFixture.addFileToProject("excluded/out.js", "var a = 1;")
    val excludedDir = psiFile.getVirtualFile().getParent()

    // exclude the folder from the content roots so ProjectFileIndex#isInContent returns false
    ModuleRootModificationUtil.updateModel(myFixture.getModule()) { model: ModifiableRootModel? ->
      for (entry in model!!.getContentEntries()) { // Exclude the exact folder that contains our file
        entry.addExcludeFolder(excludedDir.url)
      }
    }

    assertFalse(ProjectRootManager.getInstance(project).getFileIndex().isInContent(psiFile.getVirtualFile()))

    assertNull(EslintUtil.getFileKind(psiFile))
  }

  fun testReturnsNullWhenFileDoesNotMatchFilesPattern() { // Only allow files under src/**/*.js
    setFilesPattern("src/**/*.js")

    val notMatched = myFixture.addFileToProject("test/feature/foo.js", "console.log('nope')")

    // Sanity check: the file is in content
    assertTrue(ProjectRootManager.getInstance(project).getFileIndex().isInContent(notMatched.getVirtualFile()))

    // Should be filtered out by GlobPatternUtil
    assertNull(EslintUtil.getFileKind(notMatched))
  }

  fun testReturnsNonNullWhenFileInContentAndMatchesPattern() {
    setFilesPattern("src/**/*.js")

    val matched = myFixture.addFileToProject("src/app/index.js", "console.log('ok')")

    // In content and matches the glob; getFileKind should not return null
    assertTrue(ProjectRootManager.getInstance(project).getFileIndex().isInContent(matched.getVirtualFile()))

    assertNotNull(EslintUtil.getFileKind(matched))
  }
}
