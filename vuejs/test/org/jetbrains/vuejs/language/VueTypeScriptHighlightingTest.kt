package org.jetbrains.vuejs.language

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.javascript.typescript.TypeScriptHighlightingTest
import com.intellij.lang.typescript.inspections.TypeScriptValidateJSTypesInspection
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.profile.codeInspection.InspectionProfileManager
import com.intellij.testFramework.PlatformTestCase

/**
 * @author Irina.Chernushina on 10/24/2017.
 */
class VueTypeScriptHighlightingTest : TypeScriptHighlightingTest() {
  private val muted = setOf("RxUsagePerformance",
                            "ModuleVisibilityNestedClass",
                            "CastIntentionWithPrimitive",
                            "ExportTypeAlias",
                            "ExportInterface",
                            "ImportModuleAsFunctionParameter",
                            "RedundantFunctionReturnTypes",
                            "NodeDefTypes153",
                            "InsertTypeGuardIntention",
                            "GuessedTypes",
                            "AnyTypeWithPropertySignature",
                            "ModuleMergeWithVariable",
                            "GenericsOverloads",
                            "ExportPromise",
                            "GenericsResolve2",
                            "NonTypeSymbols",
                            "CastIntentionWithClassInModule",
                            "ExtendingBuiltInTypes",
                            "AnyTypeSecondLevelContextType",
                            "KeepTypeofType",
                            "ClassExpressionInstance",
                            "ModuleNestedWithSameName",
                            "ObjectPropertiesSimple",
                            "IntermediateResultsNotCachedForRecursiveTypes",
                            "ThisTypeForExpressionClass",
                            "ModuleWithOwnStandardClass",
                            "Constructors",
                            "InterfaceImplementation2",
                            "MergeClassWithModule",
                            "InheritMergedInterface",
                            "GenericsWithConstraintType",
                            "MakeStatic",
                            "CastIntentionWithClass",
                            "OptionalParameters",
                            "SimpleAsyncWithModifiers",
                            "InterfacePropertyWithDuplicateTypeName",
                            "ExportVisibility",
                            "ExportDefaultInterface",
                            "NewSignatureWithGenerics",
                            "GeneratorFunctionsOverload",
                            "EnumAssignableExtendNumber",
                            "ImportInternalInterfaceCases",
                            "ModulesWithDottedNames",
                            "ModuleAsFunctionParam",
                            "HighlightingErrorLevels",
                            "RecursiveMarker",
                            "TypeOfForClasses",
                            "MergeOptionalSignatures",
                            "ModifiersErrors",
                            "Modules4",
                            "Modules5",
                            "Modules7",
                            "AsyncReparse",
                            "UnionTypeWithModuleClasses",
                            "CreateConstructorFromSuper",
                            "CastIntentionWithClassInContextModule",
                            "RequireReturnTypeImplicit",
                            "ProtectedMemberFromNestedModule",
                            "PrimitiveTypesAssignments",
                            "TypesAssignmentsToClass",
                            "OverrideWithSubclass",
                            "GlobalAugmentationAddFunctionMethod",
                            "SuperConstructorInOverloadDeclaration",
                            "NodeDefTypes",
                            "Modules",
                            "ClassExpressionRecursive",
                            "Generics2",
                            "ExternalModulesMany",
                            "CreateConstructorFromNew",
                            "CheckTypeContextInterfaceWithVariableName",
                            "ClassExpressionChain",
                            "StaticFactory",
                            "AbstractClassesKeywords",
                            "Raytracer",
                            "AbstractClassesInstance",
                            "AnyTypeContextType",
                            "ArrayInitializerWithMethodCall",
                            "RenameFileToTSX",
                            "Overriding",
                            "ModuleMergeAsFunctionParam",
                            "ExtendStandardInterface",
                            "ReExportAllModule")

  override fun doHighlightingWithInvokeFixAndCheckResult(fixName: String?,
                                                         ext: String?,
                                                         vararg files: String?): MutableCollection<HighlightInfo> {
    LOG.info("Running overridden code for vue")
    if (muted.contains(getTestName(false))) {
      LOG.info("Skipping muted test")
      return mutableListOf()
    }
    return super.doHighlightingWithInvokeFixAndCheckResult(fixName, ext, *files)
  }

  override fun doTestFor(checkWeakWarnings: Boolean, vararg fileNames: String?): MutableCollection<HighlightInfo> {
    LOG.info("Running overridden code for vue")
    if (muted.contains(getTestName(false))) {
      LOG.info("Skipping muted test")
      return mutableListOf()
    }
    if (fileNames.size == 1 && fileNames[0]!!.endsWith(".d.ts")) {
      LOG.info("Skipping because only .d.ts file for test")
      return mutableListOf()
    }
    if (fileNames.size > 1) {
      LOG.info("Skipping because several files")
      return mutableListOf()
    }

    val manager = InspectionProfileManager.getInstance(project)
    val profile = manager.currentProfile
    val key = HighlightDisplayKey.find(TypeScriptValidateJSTypesInspection().shortName)
    val wasLevel = profile.getToolDefaultState(TypeScriptValidateJSTypesInspection().shortName, project).level
    try {
      profile.setErrorLevel(key, HighlightDisplayLevel.ERROR, project)
      return super.doTestFor(checkWeakWarnings, *fileNames)
    }
    finally {
      profile.setErrorLevel(key, wasLevel, project)
    }
  }

  override fun findVirtualFile(filePath: String): VirtualFile {
    val original = super.findVirtualFile(filePath)
    if (filePath.endsWith(".d.ts")) return original

    val text = VfsUtil.loadText(original)
    val withoutExtension = filePath.substringBeforeLast(".", filePath)
    val ioFile = createTempFile(withoutExtension.substringAfterLast("/", withoutExtension) + ".vue",
                                "<script lang=\"ts\">\n" + text + "\n</script>")
    PlatformTestCase.myFilesToDelete.add(ioFile)
    return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(ioFile)!!
  }

  // these tests need to be ignored with additional code:
  override fun testKeepTypeofType() {
    LOG.info("Skipping muted test")
  }

  override fun testIntermediateResultsNotCachedForRecursiveTypes() {
    LOG.info("Skipping muted test")
  }

  override fun testAsyncReparse() {
    LOG.info("Skipping muted test")
  }
}