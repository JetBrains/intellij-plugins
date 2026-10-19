package com.intellij.deno.roots

import com.intellij.deno.DenoSettings
import com.intellij.deno.UseDeno
import com.intellij.deno.service.DenoTypings
import com.intellij.deno.setUseDenoLibrary
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.impl.ResolveScopeManager

open class DenoLibraryRootsTest : DenoLibraryRootsTestBase() {

  fun testDepsFiles() {
    val hashFile = createCacheFile("deps/https/deno.land/${hashName('a')}")
    val metadataFile = createCacheFile("deps/https/deno.land/${hashName('a')}.metadata.json")
    val definitionFile = createCacheFile("deps/https/deno.land/mod.d.ts")
    val jsFile = createCacheFile("deps/https/deno.land/mod.js")
    updateLibraries()

    assertTrue(isInLibraries(hashFile))
    assertTrue(isInLibraries(metadataFile))
    assertTrue(isInLibraries(definitionFile))
    assertFalse(isInLibraries(jsFile))
  }

  fun testNpmFiles() {
    val definitionFile = createCacheFile("npm/registry.npmjs.org/chalk/5.3.0/index.d.ts")
    updateLibraries()

    assertTrue(isInLibraries(definitionFile))
  }

  fun testTypings() {
    val typings = DenoTypings.getInstance(project).getDenoTypingsVirtualFile()!!

    assertTrue(isInLibraries(typings))
  }

  fun testResolveScope() {
    val hashFile = createCacheFile("deps/https/deno.land/${hashName('b')}")
    updateLibraries()
    val typings = DenoTypings.getInstance(project).getDenoTypingsVirtualFile()!!
    myFixture.configureByText("main.ts", "")

    val scope = ReadAction.compute<_, RuntimeException> { ResolveScopeManager.getElementResolveScope(myFixture.file) }
    assertTrue(scope.contains(hashFile))
    assertTrue(scope.contains(typings))
  }

  fun testDisableDeno() {
    val hashFile = createCacheFile("deps/https/deno.land/${hashName('c')}")
    updateLibraries()
    assertTrue(isInLibraries(hashFile))

    setUseDenoLibrary(project, value = false, updateLib = false)
    DenoSettings.getService(project).setUseDenoAndReload(UseDeno.DISABLE)

    assertFalse(isInLibraries(hashFile))
  }
}
