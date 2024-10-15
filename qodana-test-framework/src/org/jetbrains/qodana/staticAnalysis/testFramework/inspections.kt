@file:Suppress("TestOnlyProblems")

package org.jetbrains.qodana.staticAnalysis.testFramework

import com.intellij.codeInspection.ex.ApplicationInspectionProfileManager
import com.intellij.codeInspection.ex.InspectionToolRegistrar
import com.intellij.codeInspection.ex.ProjectInspectionToolRegistrar
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ComponentManagerEx
import com.intellij.openapi.observable.util.whenDisposed
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.platform.util.coroutines.childScope
import com.intellij.profile.codeInspection.InspectionProfileManager
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager
import com.intellij.testFramework.replaceService
import com.intellij.util.application
import kotlinx.coroutines.cancel
import org.jetbrains.qodana.inspectionKts.KtsInspectionsManager
import org.jetbrains.qodana.staticAnalysis.profile.*

fun reinstantiateInspectionRelatedServices(project: Project, testRootDisposable: Disposable) {
  val scope = (project as ComponentManagerEx).getCoroutineScope().childScope("qdtest-inspections-services")
  testRootDisposable.whenDisposed {
    scope.cancel()
  }

  // app
  val appToolRegistrar = InspectionToolRegistrar()
  Disposer.register(testRootDisposable, appToolRegistrar)
  application.replaceService(InspectionToolRegistrar::class.java, appToolRegistrar, testRootDisposable)

  val qodanaAppToolRegistrar = QodanaToolApplicationRegistrar()
  Disposer.register(testRootDisposable, qodanaAppToolRegistrar)
  application.replaceService(QodanaToolRegistrar::class.java, qodanaAppToolRegistrar, testRootDisposable)

  application.replaceService(InspectionProfileManager::class.java, ApplicationInspectionProfileManager(), testRootDisposable)

  application.replaceService(QodanaApplicationInspectionProfileManager::class.java, QodanaApplicationInspectionProfileManager(), testRootDisposable)

  // project
  val ktsInspectionManager = KtsInspectionsManager(project, scope.childScope("KtsInspectionsManager"))
  project.replaceService(KtsInspectionsManager::class.java, ktsInspectionManager, testRootDisposable)

  val platformProjectToolRegistrar = ProjectInspectionToolRegistrar(project, scope.childScope("ProjectInspectionToolRegistrar"))
  Disposer.register(testRootDisposable, platformProjectToolRegistrar)
  project.replaceService(ProjectInspectionToolRegistrar::class.java, platformProjectToolRegistrar, testRootDisposable)

  val qodanaProjectToolRegistrar = QodanaToolProjectRegistrar(project)
  Disposer.register(testRootDisposable, qodanaProjectToolRegistrar)
  project.replaceService(QodanaToolRegistrar::class.java, qodanaProjectToolRegistrar, testRootDisposable)

  val projectInspectionsManager = ProjectInspectionProfileManager(project)
  Disposer.register(testRootDisposable, projectInspectionsManager)
  project.replaceService(InspectionProfileManager::class.java, projectInspectionsManager, testRootDisposable)

  val qodanaProjectInspectionProfileManager = QodanaProjectInspectionProfileManager(project)
  Disposer.register(testRootDisposable, qodanaProjectInspectionProfileManager)
  project.replaceService(QodanaProjectInspectionProfileManager::class.java, qodanaProjectInspectionProfileManager, testRootDisposable)
}