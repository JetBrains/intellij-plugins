// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.platform.util.coroutines.childScope
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.Result
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.BaselineEqualityV1
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.fingerprintOf

private val LOG = logger<AsyncInspectionToolResultWriter>()

internal class AsyncInspectionToolResultWriter(
  private val scope: CoroutineScope,
  val project: Project,
  private val database: QodanaToolResultDatabase,
  private val profileState: QodanaProfile.QodanaProfileState,
  private val macroManager: PathMacroManager
) {
  private val channel: Channel<List<Problem>> = Channel(1000)
  private val gson = SarifUtil.createGson()

  private val writerJob: Job =
    // WHY IO? – Database stuff, need to investigate and refactor
    // TODO – run database on IO dispatcher, not whole function stack
    scope.launch(StaticAnalysisDispatchers.IO, CoroutineStart.LAZY) {
      LOG.info("Async result writer started")
      for (problems in channel) {
        for (problem in problems) {
          writeProblem(problem, profileState)
        }
      }
    }

  private val consumerScope: CoroutineScope  = scope.childScope()

  fun batchConsume(problems: Iterable<Problem>, handler: (Problem) -> Boolean = { true }) {
    writerJob.start()
    val filteredProblems = problems.filter { handler(it) }
    if (filteredProblems.isEmpty()) return

    consumerScope.launch(StaticAnalysisDispatchers.Default, start = CoroutineStart.UNDISPATCHED) {
      channel.send(filteredProblems)
    }
  }

  suspend fun close() {
    val consumerJob = consumerScope.coroutineContext.job
    consumerJob.children.toList().joinAll()
    consumerJob.cancelAndJoin()

    channel.close()
    writerJob.join()

    scope.coroutineContext.job.cancelAndJoin()
  }

  private suspend fun writeProblem(problem: Problem, profileState: QodanaProfile.QodanaProfileState) {
    try {
      val sarif = problem.getSarif(macroManager, database) ?: return
      val inspectionGroup = profileState.stateByInspectionId[sarif.ruleId]!!.inspectionGroup.name
      val fingerprint = requireNotNull(sarif.fingerprintOf(BaselineEqualityV1)) { "Fingerprints not generated" }
      withContext(StaticAnalysisDispatchers.IO) {
        database.insert(inspectionGroup,
                        sarif.ruleId,
                        fingerprint,
                        gson.toJson(sarif, Result::class.java))

        val hashFrom = problem.getRelatedProblemHashFrom()
        if (hashFrom != null) {
          database.insertRelatedProblem(hashFrom, gson.toJson(sarif, Result::class.java))
        }
      }
    }
    catch (e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      LOG.warn(e)
    }
  }
}
