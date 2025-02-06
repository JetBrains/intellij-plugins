package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInsight.daemon.impl.PROBLEM_DESCRIPTOR_TAG
import com.intellij.codeInsight.daemon.impl.RELATED_LOCATIONS
import com.intellij.codeInsight.daemon.impl.RELATED_PROBLEMS_CHILD_HASH
import com.intellij.codeInsight.daemon.impl.RELATED_PROBLEMS_ROOT_HASH
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.util.UserDataHolderEx
import com.jetbrains.qodana.sarif.model.Fix
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Result
import org.jdom.Element
import org.jetbrains.qodana.staticAnalysis.sarif.ElementToSarifConverter
import org.jetbrains.qodana.staticAnalysis.sarif.ElementToSarifConverter.toSarifLocation
import org.jetbrains.qodana.staticAnalysis.sarif.PROBLEM_TYPE
import org.jetbrains.qodana.staticAnalysis.sarif.RELATED_PROBLEMS_CHILD_HASH_PROP
import org.jetbrains.qodana.staticAnalysis.sarif.RELATED_PROBLEMS_ROOT_HASH_PROP

/**
 * Provides the contract of supplying issues, produced by global/external tools, in SARIF format.
 */
interface Problem {
  suspend fun getSarif(macroManager: PathMacroManager, database: QodanaToolResultDatabase): Result?
  fun getFile(): String?
  fun getModule(): String?
  fun getRelatedProblemHashFrom(): String? = null
}

internal class XmlProblem(private val element: Element,
                          private val fixes: Set<Fix> = emptySet(),
                          private val userData: UserDataHolderEx? = null): Problem {
  override suspend fun getSarif(macroManager: PathMacroManager, database: QodanaToolResultDatabase): Result {
    macroManager.collapsePathsRecursively(element)
    return ElementToSarifConverter.convertFromXmlFormat(element, macroManager) { result ->
      val props = result.properties ?: PropertyBag()
      addRelatedProblemsHashes(props)
      result.properties = props
      result.fixes = fixes.ifEmpty { null }
      props.tags.addAll(userData?.getUserData(PROBLEM_DESCRIPTOR_TAG) ?: emptyList())
      result.relatedLocations = userData?.getUserData(RELATED_LOCATIONS)
        ?.map { it.toSarifLocation(macroManager, result) }
        ?.toSet()
      if (props[PROBLEM_TYPE] == null) {
        props[PROBLEM_TYPE] = ProblemType.REGULAR
        result.properties = props
      }
    }
  }

  override fun getFile(): String? = element.getChildText("file")

  override fun getModule(): String? = element.getChildText("module")

  override fun getRelatedProblemHashFrom(): String? {
    return userData?.getUserData(RELATED_PROBLEMS_CHILD_HASH)
  }

  private fun addRelatedProblemsHashes(props: PropertyBag) {
    userData?.getUserData(RELATED_PROBLEMS_CHILD_HASH)?.let {
      props += RELATED_PROBLEMS_CHILD_HASH_PROP to it
    }
    userData?.getUserData(RELATED_PROBLEMS_ROOT_HASH)?.let {
      props += RELATED_PROBLEMS_ROOT_HASH_PROP to it
    }
  }
}
