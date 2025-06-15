package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInsight.daemon.impl.PROBLEM_DESCRIPTOR_TAG
import com.intellij.codeInsight.daemon.impl.RELATED_LOCATIONS
import com.intellij.codeInsight.daemon.impl.RELATED_PROBLEMS_CHILD_HASH
import com.intellij.codeInsight.daemon.impl.RELATED_PROBLEMS_ROOT_HASH
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.util.UserDataHolderEx
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Region
import com.jetbrains.qodana.sarif.model.Result
import org.jdom.Element
import org.jetbrains.qodana.staticAnalysis.sarif.ElementToSarifConverter
import org.jetbrains.qodana.staticAnalysis.sarif.ElementToSarifConverter.toSarifLocation
import org.jetbrains.qodana.staticAnalysis.sarif.PROBLEM_HAS_CLEANUP
import org.jetbrains.qodana.staticAnalysis.sarif.PROBLEM_HAS_FIXES
import org.jetbrains.qodana.staticAnalysis.sarif.PROBLEM_TYPE
import org.jetbrains.qodana.staticAnalysis.sarif.RELATED_PROBLEMS_CHILD_HASH_PROP
import org.jetbrains.qodana.staticAnalysis.sarif.RELATED_PROBLEMS_ROOT_HASH_PROP
import org.jetbrains.qodana.staticAnalysis.sarif.getOrAssignProperties

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
                          private val hasCleanup: Boolean = false,
                          private val hasFixes: Boolean = false,
                          private val userData: UserDataHolderEx? = null): Problem {
  override suspend fun getSarif(macroManager: PathMacroManager, database: QodanaToolResultDatabase): Result {
    macroManager.collapsePathsRecursively(element)
    return ElementToSarifConverter.convertFromXmlFormat(element, macroManager) { result ->
      val props = result.properties ?: PropertyBag()
      addRelatedProblemsHashes(props)
      addHasFixes(props, hasFixes, hasCleanup)
      result.properties = props
      props.tags.addAll(userData?.getUserData(PROBLEM_DESCRIPTOR_TAG) ?: emptyList())
      result.relatedLocations = userData?.getUserData(RELATED_LOCATIONS)
        ?.map { it.toSarifLocation(macroManager, result) }
        ?.toSet()

      when {
        result.ruleId == INCORRECT_FORMATTING_INSPECTION_ID && !result.relatedLocations.isNullOrEmpty() -> {
          if (isRelatedLocationsValidForIncorrectFormatting(result.relatedLocations)) {
            result.getOrAssignProperties()[PROBLEM_TYPE] = ProblemType.INCORRECT_FORMATTING
            // only for IncorrectFormattingInspection with relatedLocation for the main problem (incorrect formatted file)
            // startLine = 0, startColumn = 0, charLength = 0 because it is necessary to be able to open the file in ide from web-ui
            result.locations.forEach { loc ->
              loc.physicalLocation.region = Region().withStartLine(0).withStartColumn(0).withCharLength(0)
            }
          } else {
            QodanaException("Related locations are invalid for inspection: $INCORRECT_FORMATTING_INSPECTION_ID")
          }
        }
        else -> result.getOrAssignProperties()[PROBLEM_TYPE] = ProblemType.REGULAR
      }
    }
  }

  override fun getFile(): String? = element.getChildText("file")

  override fun getModule(): String? = element.getChildText("module")

  override fun getRelatedProblemHashFrom(): String? {
    return userData?.getUserData(RELATED_PROBLEMS_CHILD_HASH)
  }

  private fun isRelatedLocationsValidForIncorrectFormatting(relatedLocations: Iterable<Location>): Boolean {
    return relatedLocations.all { it.physicalLocation != null } &&
           relatedLocations.all {
             it.physicalLocation.artifactLocation?.uri ==
               relatedLocations.first().physicalLocation.artifactLocation?.uri
           } &&
           relatedLocations.all {
             it.physicalLocation.region.sourceLanguage ==
               relatedLocations.first().physicalLocation.region.sourceLanguage
           }
  }

  private fun addRelatedProblemsHashes(props: PropertyBag) {
    userData?.getUserData(RELATED_PROBLEMS_CHILD_HASH)?.let {
      props += RELATED_PROBLEMS_CHILD_HASH_PROP to it
    }
    userData?.getUserData(RELATED_PROBLEMS_ROOT_HASH)?.let {
      props += RELATED_PROBLEMS_ROOT_HASH_PROP to it
    }
  }

  private fun addHasFixes(props: PropertyBag, hasFixes: Boolean, hasCleanup: Boolean) {
    if (hasFixes) {
      props += PROBLEM_HAS_FIXES to true
    }
    if (hasCleanup) {
      props += PROBLEM_HAS_CLEANUP to true
    }
  }
}
