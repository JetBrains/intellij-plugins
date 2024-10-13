package org.jetbrains.qodana.ui.problemsView.tree.model

import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import org.jetbrains.qodana.problem.SarifProblemWithProperties
import org.jetbrains.qodana.problem.navigatable
import org.jetbrains.qodana.problem.openFileDescriptor

fun QodanaTreeProblemNode.navigatable(project: Project): Navigatable {
  return SarifProblemWithProperties(primaryData.sarifProblem, sarifProblemProperties).navigatable(project)
}

fun QodanaTreeProblemNode.openFileDescriptor(project: Project): OpenFileDescriptor? {
  return SarifProblemWithProperties(primaryData.sarifProblem, sarifProblemProperties).openFileDescriptor(project)
}

fun QodanaTreeFileNode.openFileDescriptor(project: Project): OpenFileDescriptor {
  return OpenFileDescriptor(project, virtualFile)
}

fun QodanaTreeNode<*, *, *>.openFileDescriptor(project: Project): OpenFileDescriptor? {
  return when(this) {
    is QodanaTreeFileNode -> openFileDescriptor(project)
    is QodanaTreeProblemNode -> openFileDescriptor(project)
    else -> null
  }
}