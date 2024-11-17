package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.ex.Tools
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.search.scope.packageSet.NamedScope
import com.intellij.psi.search.scope.packageSet.NamedScopeManager
import com.intellij.testFramework.VfsTestUtil
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.license.QodanaLicenseType
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.config.DEFAULT_EXCLUDE_SCOPE_MODIFIER
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.newProfileWithInspections
import org.jetbrains.qodana.staticAnalysis.profile.MainInspectionGroup
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import org.jetbrains.qodana.staticAnalysis.script.createGlobalInspectionContext
import org.junit.Test
import java.nio.file.Path

class QodanaInspectionScopesIntegrationTest : QodanaTestCase() {

  var scopes: Array<NamedScope> = emptyArray()

  lateinit var inspectionContext: QodanaGlobalInspectionContext

  override fun setUp() {
    super.setUp()
    scopes = NamedScopeManager.getInstance(project).scopes
  }

  override fun tearDown() {
    try {
      runBlocking {
        inspectionContext.closeQodanaContext()
      }
      NamedScopeManager.getInstance(project).scopes = scopes
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  @Test
  fun exclude() {
    val excludedInspectionName = "UNUSED_IMPORT"

    val wrappers = provideTest(excludedInspections = mapOf(excludedInspectionName to emptyList())).invoke(dummyPsiFile())

    assertDoesntContain(wrappers.map { it.shortName }, excludedInspectionName)
  }

  @Test
  fun include() {
    val includedInspectionName = "JavaReflectionInvocation"

    val wrappers = provideTest(includedInspections = mapOf(includedInspectionName to emptyList())).invoke(dummyPsiFile())

    assertContainsElements(wrappers.map { it.shortName }, includedInspectionName)
  }

  @Test
  fun `exclude for part of project`() {
    val excludedInspectionName = "UNUSED_IMPORT"
    val excludedPaths = listOf("a", "b")
    val wrappersByFile = provideTest(excludedInspections = mapOf(excludedInspectionName to excludedPaths))

    val wrappersInExcludedPaths = excludedPaths.map { wrappersByFile.invoke(dummyPsiFile(it)) }

    assertDoesntContain(wrappersInExcludedPaths[0].map { it.shortName }, excludedInspectionName)
    assertDoesntContain(wrappersInExcludedPaths[1].map { it.shortName }, excludedInspectionName)
    assertContainsElements(wrappersByFile.invoke(dummyPsiFile("c")).map { it.shortName }, excludedInspectionName)
  }

  @Test
  fun `include for part of project`() {
    val includedInspectionName = "PhpDivisionByZeroInspection"
    val includedPaths = listOf("a", "b")
    val wrappersByFile = provideTest(includedInspections = mapOf(includedInspectionName to includedPaths))

    val wrappersInIncludedPaths = includedPaths.map { wrappersByFile.invoke(dummyPsiFile(it)) }

    assertContainsElements(wrappersInIncludedPaths[0].map { it.shortName }, includedInspectionName)
    assertContainsElements(wrappersInIncludedPaths[1].map { it.shortName }, includedInspectionName)
    assertDoesntContain(wrappersByFile.invoke(dummyPsiFile("c")).map { it.shortName }, includedInspectionName)
  }

  @Test
  fun `exclude for subdirectory`() {
    val excludedInspectionName = "UNUSED_IMPORT"
    val excludedDir = "a"
    val wrappersByFile = provideTest(excludedInspections = mapOf(excludedInspectionName to listOf(excludedDir)))

    val wrappersInExcludedPaths = wrappersByFile.invoke(dummyPsiFile("$excludedDir/b"))

    assertDoesntContain(wrappersInExcludedPaths.map { it.shortName }, excludedInspectionName)
    assertContainsElements(wrappersByFile.invoke(dummyPsiFile("c")).map { it.shortName }, excludedInspectionName)
  }

  @Test
  fun `exclude and include`() {
    val inspectionName = "UNUSED_IMPORT"
    val excludedDir = "a"
    val includedDir = "b"
    val wrappersByFile = provideTest(
      excludedInspections = mapOf(inspectionName to listOf(excludedDir)),
      includedInspections = mapOf(inspectionName to listOf(includedDir)))

    val wrappersInExcludedPaths = wrappersByFile.invoke(dummyPsiFile(excludedDir))
    val wrappersInIncludedPaths = wrappersByFile.invoke(dummyPsiFile(includedDir))

    assertDoesntContain(wrappersInExcludedPaths.map { it.shortName }, inspectionName)
    assertContainsElements(wrappersInIncludedPaths.map { it.shortName }, inspectionName)
  }

  @Test
  fun `include all, exclude a subdirectory`() {
    val inspectionName = "UNUSED_IMPORT"
    val excludedDir = "a"
    val wrappersByFile = provideTest(
      excludedInspections = mapOf(inspectionName to listOf(excludedDir)),
      includedInspections = mapOf(inspectionName to emptyList()))

    val wrappersInExcludedPaths = wrappersByFile.invoke(dummyPsiFile(excludedDir))
    val wrappersInIncludedPaths = wrappersByFile.invoke(dummyPsiFile("b"))

    assertDoesntContain(wrappersInExcludedPaths.map { it.shortName }, inspectionName)
    assertContainsElements(wrappersInIncludedPaths.map { it.shortName }, inspectionName)
  }

  @Test
  fun `exclude two inspections with different paths`() {
    val firstInspectionName = "UNUSED_IMPORT"
    val firstInspectionPath = "a"
    val secondInspectionName = "JavaReflectionInvocation"
    val secondInspectionPath = "b"
    val wrappersByFile = provideTest(
      excludedInspections = mapOf(
        firstInspectionName to listOf(firstInspectionPath),
        secondInspectionName to listOf(secondInspectionPath)))
    val firstWrappers = wrappersByFile.invoke(dummyPsiFile(firstInspectionPath))
    val secondWrappers = wrappersByFile.invoke(dummyPsiFile(secondInspectionPath))

    assertDoesntContain(firstWrappers.map { it.shortName }, firstInspectionName)
    assertContainsElements(firstWrappers.map { it.shortName }, secondInspectionName)

    assertDoesntContain(secondWrappers.map { it.shortName }, secondInspectionName)
    assertContainsElements(secondWrappers.map { it.shortName }, firstInspectionName)
  }

  @Test
  fun `include two inspections with different paths`() {
    val firstInspectionName = "UNUSED_IMPORT"
    val firstInspectionPath = "a"
    val secondInspectionName = "JavaReflectionInvocation"
    val secondInspectionPath = "b"
    val wrappersByFile = provideTest(includedInspections = mapOf(firstInspectionName to listOf(firstInspectionPath),
                                                                 secondInspectionName to listOf(secondInspectionPath)))
    val firstWrappers = wrappersByFile.invoke(dummyPsiFile(firstInspectionPath))
    val secondWrappers = wrappersByFile.invoke(dummyPsiFile(secondInspectionPath))
    val wrappersForThirdPath = wrappersByFile.invoke(dummyPsiFile("c"))

    assertContainsElements(firstWrappers.map { it.shortName }, firstInspectionName)
    assertDoesntContain(firstWrappers.map { it.shortName }, secondInspectionName)

    assertContainsElements(secondWrappers.map { it.shortName }, secondInspectionName)
    assertDoesntContain(secondWrappers.map { it.shortName }, firstInspectionName)


    assertDoesntContain(wrappersForThirdPath.map { it.shortName }, firstInspectionName, secondInspectionName)
  }

  @Test
  fun `exclude All`() {
    val wrappersByFile = provideTest(
      excludedInspections = mapOf("All" to emptyList()),
      inspectionsInProfile = arrayOf("UNUSED_IMPORT", "HardCodedStringLiteral", "JavaReflectionInvocation"))

    val wrappers = wrappersByFile.invoke(dummyPsiFile())

    assertEmpty(wrappers)
  }

  @Test
  fun `exclude default exclusions`() {
    val inspections = arrayOf("UNUSED_IMPORT", "HardCodedStringLiteral", "JavaReflectionInvocation")
    val wrappersByFile = provideTest(
      excludedInspections = emptyMap(),
      addDefaultExclude = true,
      inspectionsInProfile = inspections)

    val wrappersInExcludedPaths = DEFAULT_EXCLUDE_SCOPE_MODIFIER.scope.paths.associateWith { wrappersByFile(dummyPsiFile(it)) }
    val nonIgnoredWrappers = wrappersByFile(dummyPsiFile())

    wrappersInExcludedPaths.forEach { (path, wrappers) ->
      assertEmpty(path, wrappers)
    }
    assertContainsElements(nonIgnoredWrappers.map { it.shortName }, *inspections)
  }

  private fun provideTest(
    excludedInspections: Map<String, List<String>> = emptyMap(),
    includedInspections: Map<String, List<String>> = emptyMap(),
    addDefaultExclude: Boolean = false,
    inspectionsInProfile: Array<String> = excludedInspections.keys.toTypedArray(),
  ): (PsiFile) -> List<InspectionToolWrapper<*, *>> {
    lateinit var ret: (PsiFile) -> List<InspectionToolWrapper<*, *>>

    runTest {
      val profile = newProfileWithInspections(*inspectionsInProfile)
      val outputPath = getTempOutputPath()

      val qodanaConfig = QodanaConfig.fromYaml(
        projectPath = Path.of(getSourceRoot().path),
        outPath = outputPath,
        yaml = QodanaYamlConfig.EMPTY_V1.copy(
          exclude = excludedInspections.map { InspectScope(it.key, it.value) },
          include = includedInspections.map { InspectScope(it.key, it.value) },
        ),
        includeAbsent = false,
      )

      val scope = QodanaAnalysisScope.fromConfigOrDefault(qodanaConfig, project) {
        error("Could not find configured project scope at path $it")
      }

      val qodanaProfile = QodanaProfile(MainInspectionGroup(profile).applyConfig(qodanaConfig, project, addDefaultExclude), emptyList(),
                                        project,
                                        QodanaLicenseType.ULTIMATE_PLUS)
      val runContext = QodanaRunContext(
        project,
        LoadedProfile(profile, "", ""),
        scope,
        qodanaProfile,
        qodanaConfig,
        this,
        QodanaMessageReporter.DEFAULT
      )
      inspectionContext = runContext.createGlobalInspectionContext(outputPath, qodanaProfile)
      val includeDoNotShow = inspectionContext.effectiveProfile.singleTool != null

      ret = { file: PsiFile ->
        val tools = initializeTools(inspectionContext)
        inspectionContext.getWrappersFromTools(tools, file, includeDoNotShow)
      }

    }

    return ret
  }

  private fun dummyPsiFile(relativePath: String = "", language: Language = JavaLanguage.INSTANCE): PsiFile {
    val extension = language.associatedFileType?.defaultExtension ?: "unknown"
    val className = if (relativePath.isNotBlank()) "Name-$relativePath" else "Name"
    val filename = "$relativePath/$className.$extension"
    val virtualFile = VfsTestUtil.createFile(getSourceRoot(), filename, "class $className{}")

    return object : PsiFileBase(psiManager.findViewProvider(virtualFile)!!, language) {
      override fun getVirtualFile() = virtualFile
      override fun getFileType() = virtualFile.fileType
    }
  }

  private fun initializeTools(context: QodanaGlobalInspectionContext): MutableList<Tools> {
    val localSimpleTools = mutableListOf<Tools>()
    val globalSimpleTools = mutableListOf<Tools>()
    context.initializeTools(mutableListOf(), localSimpleTools, globalSimpleTools)
    return (globalSimpleTools + localSimpleTools).toMutableList()
  }

  private fun getTempOutputPath(): Path {
    return FileUtil.createTempDirectory(getTestName(false), null, true).toPath()
  }
}
