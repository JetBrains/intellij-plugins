package com.intellij.deno.roots

/**
 * Runs [DenoLibraryRootsTest] with the legacy [com.intellij.deno.DenoLibraryProvider].
 */
class DenoLegacyLibraryRootsTest : DenoLibraryRootsTest() {
  override val useWorkspaceModelRoots: Boolean = false
}
