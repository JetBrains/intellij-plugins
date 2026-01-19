// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.concurrency.ConcurrentCollectionFactory
import com.intellij.lang.LanguageMatcher
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Attachment
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.advanced.AdvancedSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopes
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.PsiSearchScopeUtil
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.childrenOfType
import com.intellij.util.Processor
import com.intellij.util.indexing.IndexingIteratorsProvider
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_EPHEMERAL_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_MODULE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_OUTPUT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_RESOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_REQUIRED_PROVIDERS
import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.config.model.local.TfLocalSchemaService
import org.intellij.terraform.config.model.version.VersionConstraint
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.config.patterns.TfPsiPatterns.TfRequiredProvidersBlock
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.hcl.psi.common.LiteralExpression
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.withGuaranteedProgressIndicator

class Module private constructor(val moduleRoot: PsiFileSystemItem) {
  companion object {
    private val LOG = Logger.getInstance(Module::class.java)

    fun getModule(file: PsiFile): Module {
      val directory = file.containingDirectory
      if (directory == null) {
        // File only in-memory, assume as only file in module
        return Module(file as HCLFile)
      }
      else {
        return getModule(directory)
      }
    }

    fun getModule(directory: PsiDirectory): Module {
      if (isFallbackVariableSearchEnabled) return Module(directory)

      val moduleRoot = ModuleDetectionUtil.findModuleRoot(directory)
      val dir = moduleRoot?.let { directory.manager.findDirectory(it) }
      if (dir == null) {
        LOG.error("No module root for $directory",
                  Attachment("files.txt", directory.virtualFile.children.joinToString("\n") { it.name }))
      }
      return Module(dir ?: directory)
    }

    fun getAsModuleBlock(moduleBlock: HCLBlock): Module? {
      return ModuleDetectionUtil.getAsModuleBlock(moduleBlock)
    }

    private class CollectVariablesVisitor(val name: String? = null) : HCLElementVisitor() {
      val collected: MutableSet<Variable> = ConcurrentCollectionFactory.createConcurrentSet()
      override fun visitBlock(o: HCLBlock) {
        if (!TfPsiPatterns.VariableRootBlock.accepts(o)) return
        o.`object` ?: return
        if (name != null && name != o.getNameElementUnquoted(1)) return
        collected.add(Variable(o))
      }
    }

    private class CollectLocalsVisitor(val name: String? = null) : HCLElementVisitor() {
      val collected: MutableSet<Pair<String, HCLProperty>> = HashSet()
      override fun visitBlock(o: HCLBlock) {
        if (!TfPsiPatterns.LocalsRootBlock.accepts(o)) return

        val obj = o.`object` ?: return

        if (name != null) {
          val prop = obj.findProperty(name) ?: return
          collected.add(prop.name to prop)
          return
        }

        obj.propertyList.mapTo(collected) { it.name to it }
      }
    }

    private class IsHCL2SupportedCachedValueProvider(val module: Module) : CachedValueProvider<Boolean> {
      override fun compute(): CachedValueProvider.Result<Boolean> {
        val tfVersions = module.getTerraformRequiredVersion()
        if (tfVersions.isNotEmpty()) {
          val constraint = tfVersions.mapNotNull {
            ModuleDetectionUtil.getVersionConstraint(it, false)
          }.fold(VersionConstraint.parse(">=0.12")) { a, b -> VersionConstraint.intersect(a, b) }
          if (constraint.isEmpty()) {
            // Older than Terraform 0.12, should not suggest conversion
            return CachedValueProvider.Result(false, module.moduleRoot)
          }
        }
        return CachedValueProvider.Result(true, module.moduleRoot)
      }

    }
  }

  fun getAllVariables(): List<Variable> {
    val collected = ConcurrentCollectionFactory.createConcurrentSet<Variable>()
    processAllFilesWithVariables(Processor {
      collected.addAll(getAllVariablesDeclaredInFile(it))
      true
    })
    return collected.toList()
  }

  @Deprecated("There may be declaration duplicates", replaceWith = ReplaceWith("findVariables(name)"))
  fun findVariable(name: String): Variable? {
    val visitor = CollectVariablesVisitor(name)
    process(PsiElementProcessor { file -> file.acceptChildren(visitor); visitor.collected.isEmpty() })
    return visitor.collected.firstOrNull()
  }

  fun findVariables(name: String): List<Variable> {
    val collected = ConcurrentCollectionFactory.createConcurrentSet<Variable>()
    processAllFilesWithVariables(Processor {
      collected.addAll(getAllVariablesDeclaredInFile(it).asSequence().filter { it.name == name })
      true
    })
    return collected.toList()
  }

  private fun getModuleSearchScope(): GlobalSearchScope? {
    if (isFallbackVariableSearchEnabled) return fallbackCalculateModuleAwareSearchScope(moduleRoot)

    val moduleRootVf = moduleRoot.virtualFile ?: return null
    return GlobalSearchScopes.directoryScope(moduleRoot.project, moduleRootVf, false)
  }


  @Deprecated("Remove after 2024.3 release if no usages of `isFallbackVariableSearchEnabled` detected")
  private fun fallbackCalculateModuleAwareSearchScope(context: PsiFileSystemItem): GlobalSearchScope? {
    val currentFile = context.virtualFile ?: return null
    val currentFileDir = currentFile.takeIf { it.isDirectory } ?: currentFile.parent
    if (!isDeepVariableSearchEnabled) {
      return GlobalSearchScopes.directoryScope(context.project, currentFileDir, false)
    }

    val terraformDir = ModuleDetectionUtil.getTerraformDirSomewhere(currentFile, context.project)
    if (terraformDir != null && currentFileDir != null && VfsUtil.isAncestor(terraformDir, currentFile, true)) {
      return GlobalSearchScopes.directoryScope(context.project, currentFileDir, false)
    }

    val manifestRoots = terraformDir?.let { findRootsFromManifest(context, it) } ?: emptyList()
    val exactModuleRoot = findClosestRoot(manifestRoots, currentFile)
                          ?: context.project.service<TfLocalSchemaService>().findLockFile(currentFile)?.parent

    val dirToSearchIn = exactModuleRoot ?: currentFileDir ?: return null
    val otherModuleRoots = manifestRoots.filterNot { VfsUtil.isAncestor(it, dirToSearchIn, false) }
    val exclusion = GlobalSearchScopes.directoriesScope(context.project, true, *otherModuleRoots.toTypedArray())
    return GlobalSearchScopes.directoryScope(context.project, dirToSearchIn, exactModuleRoot != null)
      .intersectWith(GlobalSearchScope.notScope(exclusion))
  }

  private fun findClosestRoot(possibleRoots: List<VirtualFile>, currentFile: VirtualFile): VirtualFile? =
    possibleRoots
      .filter { VfsUtil.isAncestor(it, currentFile, false) }
      .maxByOrNull { it.path.length }

  private fun findRootsFromManifest(context: PsiFileSystemItem, dotTerraformDir: VirtualFile): List<VirtualFile> {
    val manifest = ModuleDetectionUtil.getManifestForDirectory(dotTerraformDir, context, context.project).value ?: return emptyList()
    return manifest.modules.mapNotNull { module ->
      val path = FileUtil.toSystemIndependentName(module.dir)
      manifest.context.findFileByRelativePath(path)
    }
  }

  fun getTerraformModuleScope(): GlobalSearchScope {
    val searchScope = getModuleSearchScope() ?: GlobalSearchScope.projectScope(moduleRoot.project)

    return searchScope.restrictToTerraformFiles(this@Module.moduleRoot.project)
  }

  private fun processAllFilesWithVariables(processor: Processor<PsiFile>): Boolean {
    val terraformScope = getTerraformModuleScope()

    return withGuaranteedProgressIndicator {
      if (!PsiSearchHelper.getInstance(moduleRoot.project).processAllFilesWithWord("variable", terraformScope, processor, true))
        return@withGuaranteedProgressIndicator false

      // also process all unindexed files in the same dir IJPL-148978
      val iteratorsProvider = IndexingIteratorsProvider.getInstance(moduleRoot.project)
      process(PsiElementProcessor { file ->
        if (!iteratorsProvider.shouldBeIndexed(file.virtualFile))
          processor.process(file)
        else
          true
      })
    }
  }

  private fun getAllVariablesDeclaredInFile(psiFile: PsiFile): Collection<Variable> {
    return CachedValuesManager.getCachedValue(psiFile, CachedValueProvider {
      val visitor = CollectVariablesVisitor()
      psiFile.acceptChildren(visitor)
      CachedValueProvider.Result.create(visitor.collected, psiFile)
    })
  }

  fun getAllLocals(): List<Pair<String, HCLProperty>> {
    val visitor = CollectLocalsVisitor()
    process(PsiElementProcessor { file -> file.acceptChildren(visitor); true })
    return visitor.collected.toList()
  }

  fun findLocal(name: String): Pair<String, HCLProperty>? {
    val visitor = CollectLocalsVisitor(name)
    process(PsiElementProcessor { file -> file.acceptChildren(visitor); visitor.collected.isEmpty() })
    return visitor.collected.firstOrNull()
  }

  private fun process(processor: PsiElementProcessor<HCLFile>): Boolean {
    // TODO: Support json files (?)
    if (moduleRoot is HCLFile) {
      if (moduleRoot.language == TerraformLanguage) {
        return processor.execute(moduleRoot)
      }
      return false
    }
    assert(moduleRoot is PsiDirectory)
    return moduleRoot.processChildren(PsiElementProcessor { element ->
      if (element !is HCLFile || element.language != TerraformLanguage) return@PsiElementProcessor true
      processor.execute(element)
    })
  }

  fun getDefinedResources(type: String? = null, name: String? = null): List<HCLBlock> {
    return getDefinedHclBlocks(HCL_RESOURCE_IDENTIFIER, numberOfArguments = 2, firstArgument = type, secondArgument = name)
  }

  fun getDefinedRequiredProviders(): List<HCLProperty>? {
    // `required_providers` should be only 1 in terraform module
    val requiredProvidersBlock = getDefinedHclBlocks(HCL_TERRAFORM_IDENTIFIER, numberOfArguments = 0).firstNotNullOfOrNull { terraformBlock ->
      terraformBlock.`object`?.blockList?.firstOrNull { it.name == HCL_TERRAFORM_REQUIRED_PROVIDERS }
    } ?: return null

    return requiredProvidersBlock.`object`?.propertyList
  }

  // numberOfArguments should be 0, 1 or 2:
  // for example, terraform {} - 0, variable "some_variable" {} - 1, data "some_provider" "name" {} - 2
  private fun getDefinedHclBlocks(
    identifier: String,
    numberOfArguments: Int,
    firstArgument: String? = null,
    secondArgument: String? = null,
  ): List<HCLBlock> {
    val found = mutableListOf<HCLBlock>()

    process(PsiElementProcessor { file ->
      file.acceptChildren(object : HCLElementVisitor() {
        override fun visitBlock(block: HCLBlock) {
          if (block.getNameElementUnquoted(0) != identifier) return
          if (numberOfArguments == 0) {
            found.add(block)
            return
          }

          val first = block.getNameElementUnquoted(1) ?: return
          if (firstArgument != null && first != firstArgument) return
          if (numberOfArguments == 1) {
            found.add(block)
          }
          else {
            val second = block.getNameElementUnquoted(2) ?: return
            if (secondArgument == null || secondArgument == second) {
              found.add(block)
            }
          }
        }
      })
      true
    })

    return found
  }

  fun getDefinedDataSources(type: String? = null, name: String? = null): List<HCLBlock> {
    return getDefinedHclBlocks(HCL_DATASOURCE_IDENTIFIER, numberOfArguments = 2, firstArgument = type, secondArgument = name)
  }

  fun getDefinedEphemeralResources(type: String? = null, name: String? = null): List<HCLBlock> {
    return getDefinedHclBlocks(HCL_EPHEMERAL_IDENTIFIER, numberOfArguments = 2, firstArgument = type, secondArgument = name)
  }

  // search is either 'type' or 'type.alias'
  fun findProviders(search: String): List<HCLBlock> {
    val split = search.split('.')
    val type = split[0]
    val alias = split.getOrNull(1)
    val found = ArrayList<HCLBlock>()
    process(PsiElementProcessor { file ->
      file.acceptChildren(object : HCLElementVisitor() {
        override fun visitBlock(o: HCLBlock) {
          if (HCL_PROVIDER_IDENTIFIER != o.getNameElementUnquoted(0)) return
          val tp = o.getNameElementUnquoted(1) ?: return
          val als = when (val value = o.`object`?.findProperty("alias")?.value) {
            is HCLStringLiteral -> value.value
            is HCLIdentifier -> value.id
            else -> null
          }
          if (alias == null && als == null) {
            if (type == tp) found.add(o)
          }
          else {
            if (alias == als) found.add(o)
          }
        }
      }); true
    })
    return found
  }

  fun getDefinedProviders(): List<Pair<HCLBlock, String>> {
    val found = ArrayList<Pair<HCLBlock, String>>()
    process(PsiElementProcessor { file ->
      file.acceptChildren(object : HCLElementVisitor() {
        override fun visitBlock(o: HCLBlock) {
          if (HCL_PROVIDER_IDENTIFIER != o.getNameElementUnquoted(0)) return
          val fqn = o.getProviderFQName() ?: return
          found.add(Pair(o, fqn))
        }
      }); true
    })
    return found
  }

  fun getDefinedModules(name: String? = null): List<HCLBlock> {
    return getDefinedHclBlocks(HCL_MODULE_IDENTIFIER, numberOfArguments = 1, firstArgument = name)
  }

  fun getDefinedOutputs(): List<HCLBlock> {
    return getDefinedHclBlocks(HCL_OUTPUT_IDENTIFIER, numberOfArguments = 1)
  }

  fun getTerraformBlockWithProvidersOrFirst(): HCLBlock? {
    val terraformBlocks = getDefinedHclBlocks(HCL_TERRAFORM_IDENTIFIER, numberOfArguments = 0)

    val blockWithRequiredProviders = terraformBlocks.firstOrNull { block ->
      block.childrenOfType<HCLBlock>().any { TfRequiredProvidersBlock.accepts(it) }
    }
    return blockWithRequiredProviders ?: terraformBlocks.firstOrNull()
  }

  // Returns all 'terraform.required_version' defined in module.
  fun getTerraformRequiredVersion(): List<String> {
    val found = ArrayList<String>()
    process(PsiElementProcessor { file ->
      file.acceptChildren(object : HCLElementVisitor() {
        override fun visitBlock(o: HCLBlock) {
          if (!TfPsiPatterns.TerraformRootBlock.accepts(o)) return
          val value = o.`object`?.findProperty(TfTypeModel.TerraformRequiredVersion.name)?.value ?: return
          if (value is LiteralExpression) {
            found.add(value.unquotedText)
          }
          return
        }
      }); true
    })
    return found
  }

  fun isHCL2Supported(): Boolean {
    return CachedValuesManager.getCachedValue(this.moduleRoot, IsHCL2SupportedCachedValueProvider(this))
  }

  val model: TfTypeModel
    get() = TypeModelProvider.globalModel

  override fun equals(other: Any?): Boolean {
    if (other !is Module) return false
    val file = moduleRoot.virtualFile
    return if (file != null) file == other.moduleRoot.virtualFile
    else moduleRoot == other.moduleRoot
  }

  override fun hashCode(): Int {
    return (moduleRoot.virtualFile ?: moduleRoot).hashCode()
  }

  fun getType(): HclType {
    val result = HashMap<String, HclType?>()

    val outputs = getDefinedOutputs()
    for (output in outputs) {
      val value = output.`object`?.findProperty("value")?.value
      result[output.name] = value.getType()
    }

    // TODO: Should variables be in type?
    val variables = getAllVariables()
    for (variable in variables) {
      result[variable.name] = variable.getCombinedType()
    }

    return ObjectType(result)
  }
}

internal fun GlobalSearchScope.restrictToTerraformFiles(project: Project): GlobalSearchScope = PsiSearchScopeUtil.restrictScopeToFileLanguage(
  project,
  this,
  LanguageMatcher.matchWithDialects(HCLLanguage)
) as GlobalSearchScope

internal val isDeepVariableSearchEnabled: Boolean
  get() = AdvancedSettings.getBoolean("org.intellij.terraform.config.variables.deep.search")

internal val isFallbackVariableSearchEnabled: Boolean
  get() = AdvancedSettings.getBoolean("org.intellij.terraform.variables.search.fallback")

internal fun createDisableDeepVariableSearchQuickFix(): LocalQuickFix? {
  if (!isFallbackVariableSearchEnabled) return null
  if (!isDeepVariableSearchEnabled) return null

  return object : LocalQuickFix {

    override fun startInWriteAction(): Boolean = false

    override fun getFamilyName(): String = HCLBundle.message("disable.deep.variable.search")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      AdvancedSettings.setBoolean("org.intellij.terraform.config.variables.deep.search", false)
      DaemonCodeAnalyzer.getInstance(project).restart("createDisableDeepVariableSearchQuickFix")
    }

  }
}

