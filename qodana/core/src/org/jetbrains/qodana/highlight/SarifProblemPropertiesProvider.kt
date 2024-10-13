package org.jetbrains.qodana.highlight

import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.problem.SarifProblemProperties
import org.jetbrains.qodana.problem.SarifProblemWithProperties

class SarifProblemPropertiesUpdater(
  val sarifProblem: SarifProblem,
  val update: (SarifProblemProperties) -> (SarifProblemProperties)
)

interface SarifProblemPropertiesProvider {
  val problemsWithProperties: Sequence<SarifProblemWithProperties>

  fun getProblemProperties(sarifProblem: SarifProblem): SarifProblemProperties
}

class MutableSarifProblemPropertiesProvider(
  private val allProblems: Set<SarifProblem>,
  private val problemsWithNotDefaultProperties: MutableMap<SarifProblem, SarifProblemProperties>
) : SarifProblemPropertiesProvider {
  override val problemsWithProperties: Sequence<SarifProblemWithProperties>
    get() = sequence {
      yieldAll(
        problemsWithNotDefaultProperties.asSequence()
          .map { SarifProblemWithProperties(it.key, it.value) }
      )
      yieldAll(
        allProblems.asSequence()
          .filter { it !in problemsWithNotDefaultProperties }
          .map { SarifProblemWithProperties(it, it.defaultProperties) }
      )
    }

  override fun getProblemProperties(sarifProblem: SarifProblem): SarifProblemProperties {
    return problemsWithNotDefaultProperties[sarifProblem] ?: sarifProblem.defaultProperties
  }

  fun toImmutableCopy(): SarifProblemPropertiesProvider {
    return MutableSarifProblemPropertiesProvider(allProblems, problemsWithNotDefaultProperties.toMutableMap())
  }

  fun updateProblemProperties(sarifProblemPropertiesUpdater: SarifProblemPropertiesUpdater): SarifProblemWithProperties? {
    val sarifProblem = sarifProblemPropertiesUpdater.sarifProblem
    val oldProperties = getProblemProperties(sarifProblem)
    val newProperties = sarifProblemPropertiesUpdater.update(oldProperties)
    if (oldProperties == newProperties) return null

    if (sarifProblem.defaultProperties == newProperties) {
      problemsWithNotDefaultProperties.remove(sarifProblem)
    } else {
      problemsWithNotDefaultProperties[sarifProblem] = newProperties
    }

    return SarifProblemWithProperties(sarifProblem, newProperties)
  }
}