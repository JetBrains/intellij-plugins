// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.platform.util.coroutines.childScope
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        // Producers such as Rider send one problem per message, so take whatever is already queued
        // and write it in one transaction.
        val batch = ArrayList(problems)
        while (batch.size < MAX_BATCH_SIZE) {
          batch.addAll(channel.tryReceive().getOrNull() ?: break)
        }
        writeProblems(batch, profileState)
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

  private class Row(val inspectionGroup: String, val ruleId: String, val fingerprint: String, val json: String, val relatedHashFrom: String?)

  private suspend fun writeProblems(problems: List<Problem>, profileState: QodanaProfile.QodanaProfileState) {
    // Conversion may read the database, so it runs before the transaction opens.
    // It loads file texts and builds JSON, which takes longer than the insert, so it runs on several workers;
    // the chunks keep their order, so the rows go to the database in the order the problems came.
    val chunkSize = (problems.size + CONVERSION_PARALLELISM - 1) / CONVERSION_PARALLELISM
    val rows = coroutineScope {
      problems.chunked(chunkSize.coerceAtLeast(1))
        .map { chunk -> async(StaticAnalysisDispatchers.Default) { chunk.mapNotNull { toRow(it, profileState) } } }
        .awaitAll()
        .flatten()
    }
    if (rows.isEmpty()) return
    withContext(StaticAnalysisDispatchers.IO) {
      // A failed BEGIN or COMMIT loses this batch only; the writer goes on with the next one.
      try {
        database.inTransaction {
          for (row in rows) {
            try {
              database.insert(row.inspectionGroup, row.ruleId, row.fingerprint, row.json)
              if (row.relatedHashFrom != null) {
                database.insertRelatedProblem(row.relatedHashFrom, row.json)
              }
            }
            catch (e: Exception) {
              LOG.warn(e)
            }
          }
        }
      }
      catch (e: Exception) {
        LOG.warn(e)
      }
    }
  }

  private suspend fun toRow(problem: Problem, profileState: QodanaProfile.QodanaProfileState): Row? {
    try {
      val sarif = problem.getSarif(macroManager, database) ?: return null
      val inspectionGroup = profileState.getInspectionState(sarif.ruleId)!!.inspectionGroup.name
      val fingerprint = requireNotNull(sarif.fingerprintOf(BaselineEqualityV1)) { "Fingerprints not generated" }
      return Row(inspectionGroup, sarif.ruleId, fingerprint, gson.toJson(sarif, Result::class.java), problem.getRelatedProblemHashFrom())
    }
    catch (e: CancellationException) {
      throw e
    }
    catch (e: Exception) {
      LOG.warn(e)
      return null
    }
  }
}

private const val MAX_BATCH_SIZE = 1000
private val CONVERSION_PARALLELISM = Runtime.getRuntime().availableProcessors().coerceIn(1, 4)
