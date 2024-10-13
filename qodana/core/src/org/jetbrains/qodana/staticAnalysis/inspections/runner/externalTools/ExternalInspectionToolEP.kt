package org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.InspectionEP
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.ui.DefaultInspectionToolPresentation
import com.intellij.openapi.extensions.PluginDescriptor

class ExternalInspectionToolEP(private val inspectionDescriptor: ExternalInspectionDescriptor, pluginDescriptor: PluginDescriptor) : InspectionEP() {
  init {
    presentation = DefaultInspectionToolPresentation::class.java.name
    displayName = inspectionDescriptor.displayName
    implementationClass = ""
    shortName = inspectionDescriptor.shortName
    enabledByDefault = inspectionDescriptor.isEnabledByDefault
    hasStaticDescription = true
    groupPath = inspectionDescriptor.groupPath
    groupDisplayName = inspectionDescriptor.groupDisplayName
    level = inspectionDescriptor.defaultLevel.name
    this.pluginDescriptor = pluginDescriptor
  }

  override fun instantiateTool(): InspectionProfileEntry {
    return object: ExternalInspectionTool() {
      override fun getGroupDisplayName(): String {
        return inspectionDescriptor.groupDisplayName
      }

      override fun getDisplayName(): String {
        return inspectionDescriptor.displayName
      }

      override fun getShortName(): String {
        return inspectionDescriptor.shortName
      }

      override fun getStaticDescription(): String {
        return inspectionDescriptor.description
      }

      override fun getDefaultLevel(): HighlightDisplayLevel {
        return inspectionDescriptor.defaultLevel
      }

      override fun isEnabledByDefault(): Boolean {
        return inspectionDescriptor.isEnabledByDefault
      }
    }
  }
}