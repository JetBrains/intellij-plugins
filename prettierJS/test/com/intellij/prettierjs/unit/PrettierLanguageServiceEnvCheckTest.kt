// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.prettierjs.PrettierError
import com.intellij.prettierjs.PrettierLanguageServiceImpl
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.runInEdtAndGet
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

private const val TIMEOUT_SECONDS = 30L

/**
 * Checks that [PrettierLanguageServiceImpl] runs the Prettier package check off the caller thread.
 * The check reads the file system, and the caller may hold the read lock.
 */
class PrettierLanguageServiceEnvCheckTest : BasePlatformTestCase() {
  override fun runInDispatchThread(): Boolean = false

  fun testResolveConfigDoesNotWaitForPackageCheck() {
    assertCallerDoesNotWaitForPackageCheck { service, nodePackage ->
      service.resolveConfig("/fake/index.js", nodePackage).thenApply { it?.error }
    }
  }

  fun testFormatDoesNotWaitForPackageCheck() {
    assertCallerDoesNotWaitForPackageCheck { service, nodePackage ->
      service.format("/fake/index.js", null, "", nodePackage, null).thenApply { it?.error }
    }
  }

  /**
   * Sends a request with a package whose validity check blocks until the caller has returned.
   * The request must return a pending future, and the check must run on another thread.
   */
  private fun assertCallerDoesNotWaitForPackageCheck(
    request: (PrettierLanguageServiceImpl, NodePackage) -> CompletableFuture<String?>,
  ) {
    configureFakeNodeInterpreter()
    val checkStarted = CountDownLatch(1)
    val callerReturned = CountDownLatch(1)
    val checkThread = AtomicReference<Thread>()
    val nodePackage = object : NodePackage("/fake/node_modules/prettier") {
      override fun isValid(project: Project?, interpreter: NodeJsInterpreter?): Boolean {
        checkThread.set(Thread.currentThread())
        checkStarted.countDown()
        callerReturned.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        return false
      }
    }
    val workDir = runInEdtAndGet { myFixture.addFileToProject("index.js", "").virtualFile.parent }
    val service = PrettierLanguageServiceImpl(project, workDir)
    Disposer.register(testRootDisposable, service)

    val future = try {
      val future = request(service, nodePackage)
      assertTrue("The package check did not start", checkStarted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS))
      assertFalse("The caller must not wait for the package check", future.isDone)
      assertNotSame("The package check must not run on the caller thread", Thread.currentThread(), checkThread.get())
      future
    }
    finally {
      callerReturned.countDown()
    }

    val expectedError = PrettierError.InstallPackage()
    assertEquals(expectedError.message, future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
    assertEquals(expectedError, service.error)
  }

  /** The service checks the interpreter before the package. An executable file is a valid local interpreter. */
  private fun configureFakeNodeInterpreter() {
    val nodeFile = FileUtil.createTempFile("node", null, true)
    assertTrue(nodeFile.setExecutable(true))
    val interpreter = NodeJsLocalInterpreter(nodeFile.path)
    NodeJsInterpreterManager.getInstance(project).setInterpreterRef(interpreter.toRef(), testRootDisposable)
    VfsRootAccess.allowRootAccess(testRootDisposable, interpreter.interpreterSystemDependentPath)
  }
}
