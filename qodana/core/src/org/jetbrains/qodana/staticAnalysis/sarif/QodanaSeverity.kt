package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.lang.annotation.HighlightSeverity
import com.jetbrains.qodana.sarif.model.Level
import icons.QodanaIcons
import org.jetbrains.annotations.Nls
import javax.swing.Icon

private const val TYPO_SEVERITY_NAME = "TYPO"

enum class QodanaSeverity(@Nls private val myName: String, val weight: Int) {
  INFO("Info", 0),
  LOW("Low", 1),
  MODERATE("Moderate", 2),
  HIGH("High", 3),
  CRITICAL("Critical", 4);

  val icon: Icon
    get() {
      return when(this) {
        CRITICAL -> QodanaIcons.Icons.Critical
        HIGH -> QodanaIcons.Icons.High
        MODERATE -> QodanaIcons.Icons.Moderate
        LOW -> QodanaIcons.Icons.Low
        INFO -> QodanaIcons.Icons.Info
      }
    }

  @Nls
  override fun toString(): String = myName

  companion object {
    fun fromIdeaSeverity(ideaSeverity: String): QodanaSeverity {
      return when(ideaSeverity) {
        HighlightSeverity.ERROR.name -> CRITICAL
        HighlightSeverity.WARNING.name -> HIGH
        HighlightSeverity.WEAK_WARNING.name -> MODERATE
        TYPO_SEVERITY_NAME -> LOW
        else -> INFO
      }
    }

    fun fromSarifLevel(sarifLevel: Level): QodanaSeverity = fromIdeaSeverity(sarifLevel.toSeverity().name)
  }
}