// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.diagnostic.ThreadDumper
import com.intellij.diagnostic.dumpCoroutines
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.text.Formats
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.time.delay
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.utils.WCWidth.wcwidth
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.time.Duration
import kotlin.math.max

private val terminal: Terminal = TerminalBuilder.terminal()
private val LOG_INTERVAL: Duration = Duration.ofMinutes(10)
private val CONSOLE_INTERVAL: Duration = Duration.ofSeconds(30)
private const val DEFAULT_TERMINAL_LENGTH = 80

suspend fun <T> runTaskAndLogTime(progressName: String, action: suspend () -> T): T {
  return supervisorScope {
    val cookie = TimeCookie()
    ConsoleLog.info("Preparing for the $progressName stage ...")

    val standardOut = System.out
    val interactiveOut = InteractiveOutput(terminal, progressName, standardOut)
    if (isInteractiveOutput()) {
      System.setOut(interactiveOut)
    }

    val loggerJob = launch(StaticAnalysisDispatchers.IO) {
      launch {
        while (true) {
          delay(CONSOLE_INTERVAL)
          ConsoleLog.info("Keep running $progressName ... so far ${cookie.formatDuration()}")
        }
      }

      launch {
        while (true) {
          delay(LOG_INTERVAL)
          val prefix = "keep running $progressName ... so far ${cookie.formatDuration()}"
          dumpThreads(prefix, "too-long-wait-thread-dump-${System.currentTimeMillis()}.txt")
        }
      }
    }

    try {
      val result = action()
      loggerJob.cancelAndJoin()
      return@supervisorScope result
    }
    finally {
      if (isInteractiveOutput()) {
        System.setOut(standardOut)
      }
      if (isInteractiveOutput()) println()
      ConsoleLog.info("The $progressName stage completed in ${cookie.formatDuration()}")
    }
  }
}

fun dumpThreads(prefix: String, fileName: String) {
  val threadDump = ThreadDumper.dumpThreadsToString()
  val coroutines = dumpCoroutines()

  try {
    val logFile = File(PathManager.getLogPath(), fileName)
    logFile.parentFile?.mkdirs()
    logFile.writeText("$prefix\n\n$threadDump\n\nCoroutines dump: $coroutines")
  }
  catch (e: IOException) {
    logger<QodanaInspectionApplication>().warn("Failed writing thread dump file", e)
  }
}

class TimeCookie {
  private val now = System.currentTimeMillis()
  fun formatDuration(): String {
    val msMultiplier = 1000
    val duration = max(0L, TimeCookie().now - this.now)
    return if (duration < msMultiplier) {
      Formats.formatDuration(duration, "")
    }
    else {
      Formats.formatDuration(duration - duration % msMultiplier, "")
    }
  }
}

private fun String.cutToOneTerminalLine(maxLength: Int): String {
  var length = 0
  return this.takeWhile {
    length += max(wcwidth(it.code), 0)
    length < maxLength
  }
}

fun String.lengthOnTerminal() = sumOf { max(wcwidth(it.code), 0) }

class InteractiveOutput(private val terminal: Terminal,
                        progressName: String,
                        standardOutput: PrintStream) : PrintStream(standardOutput) {
  private val commonPrefix = "\r($progressName) "
  private val commonPrefixTerminalLength = commonPrefix.lengthOnTerminal()

  private fun <T> printlnImpl(message: T) {
    val terminalLineLength = if (terminal.width > 10) terminal.width - 1 else DEFAULT_TERMINAL_LENGTH
    val trimmedMessage = message.toString().trim().cutToOneTerminalLine(terminalLineLength - commonPrefixTerminalLength)
    if (trimmedMessage.isNotBlank()) {
      synchronized(this) {
        super.print("\r" + " ".repeat(terminalLineLength))
        print(trimmedMessage)
      }
    }
  }

  override fun print(s: String?) {
    super.print("$commonPrefix$s")
  }

  override fun print(f: Float) {
    super.print("$commonPrefix$f")
  }

  override fun print(b: Boolean) {
    super.print("$commonPrefix$b")
  }

  override fun print(c: Char) {
    super.print("$commonPrefix$c")
  }

  override fun print(i: Int) {
    super.print("$commonPrefix$i")
  }

  override fun print(l: Long) {
    super.print("$commonPrefix$l")
  }

  override fun print(d: Double) {
    super.print("$commonPrefix$d")
  }

  override fun print(s: CharArray) {
    super.print("$commonPrefix$s")
  }

  override fun print(obj: Any?) {
    super.print("$commonPrefix$obj")
  }

  override fun println(x: String) {
    if (x.isNotBlank()) {
      printlnImpl(x.trim())
    }
  }

  override fun println(x: Boolean) {
    printlnImpl(x)
  }

  override fun println(x: Any?) {
    printlnImpl(x)
  }

  override fun println(x: Char) {
    if (!x.isWhitespace()) printlnImpl(x)
  }

  override fun println(x: CharArray) {
    printlnImpl(x.toString())
  }

  override fun println(x: Double) {
    printlnImpl(x)
  }

  override fun println() {
  }

  override fun println(x: Float) {
    printlnImpl(x)
  }

  override fun println(x: Int) {
    printlnImpl(x)
  }

  override fun println(x: Long) {
    printlnImpl(x)
  }
}


object ConsoleLog {
  private val LOG = logger<ConsoleLog>()

  fun info(message: String) {
    LOG.info(message)
    println(message)
  }

  fun warn(message: String) {
    LOG.warn(message)
    println("WARN - $message")
  }

  fun error(message: String, cause: Throwable? = null) {
    LOG.error(message, cause)
    println("ERROR - $message")
    cause?.printStackTrace()
  }
}