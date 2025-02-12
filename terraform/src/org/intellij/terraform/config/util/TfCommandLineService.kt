// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.util

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Disposer
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.headTailOrNull
import org.jetbrains.annotations.TestOnly
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap

interface TfCommandLineService {

  fun wrapCommandLine(commandLine: GeneralCommandLine): GeneralCommandLine

}

class TfCommandLineServiceImpl : TfCommandLineService {
  override fun wrapCommandLine(commandLine: GeneralCommandLine): GeneralCommandLine = commandLine
}

class TfCommandLineServiceMock : TfCommandLineService {

  private val mocks = ConcurrentHashMap<String, Process>()


  fun mockCommandLine(commandLine: String, stdout: String, disposable: Disposable): Unit =
    mockCommandLine(commandLine, stdout, 0, disposable)

  fun mockCommandLine(commandLine: String, stdout: String, exitCode: Int, disposable: Disposable) {
    mocks[commandLine] = object : Process() {
      override fun getOutputStream(): OutputStream = OutputStream.nullOutputStream()

      override fun getInputStream(): InputStream = stdout.byteInputStream()

      override fun getErrorStream(): InputStream = InputStream.nullInputStream()

      override fun waitFor(): Int = exitCode

      override fun exitValue(): Int = exitCode

      override fun destroy() {}
    }
    Disposer.register(disposable) {
      mocks.remove(commandLine)
      requests.clear()
    }
  }

  private val errors = ContainerUtil.createConcurrentList<Throwable>()

  private val requests = ContainerUtil.createConcurrentList<String>()

  override fun wrapCommandLine(commandLine: GeneralCommandLine): GeneralCommandLine = object : GeneralCommandLine() {
    override fun createProcess(): Process {
      val commandLineString = commandLine.commandLineString
      requests.add(commandLineString)
      return mocks[commandLineString] ?: throw AssertionError(
        "Missing mock for $commandLineString, available mocks = ${mocks.keys}"
      ).also { errors.add(it) }
    }
  }

  fun throwErrorsIfAny() {
    val (head, tail) = errors.toList().headTailOrNull() ?: return
    errors.clear()
    for (throwable in tail) {
      head.addSuppressed(throwable)
    }
    throw head
  }

  fun clear() {
    requests.clear()
    errors.clear()
  }

  fun requestsToVerify(): List<String> {
    val result = requests.toList()
    requests.clear()
    return result
  }

  companion object {

    @get:TestOnly
    val instance: TfCommandLineServiceMock
      get() = (service<TfCommandLineService>() as TfCommandLineServiceMock)

  }

}