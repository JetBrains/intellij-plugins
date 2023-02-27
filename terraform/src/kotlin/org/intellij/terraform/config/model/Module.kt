/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.config.model

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.common.LiteralExpression
import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.config.model.version.VersionConstraint
import org.intellij.terraform.config.patterns.TerraformPatterns

class Module private constructor(val item: PsiFileSystemItem) {
  companion object {
    private val LOG = Logger.getInstance(Module::class.java)

    fun getModule(file: PsiFile): Module {
      val directory = file.containingDirectory
      if (directory == null) {
        // File only in-memory, assume as only file in module
        return Module(file as HCLFile)
      } else {
        return Module(directory)
      }
    }

    fun getAsModuleBlock(moduleBlock: HCLBlock): Module? {
      return ModuleDetectionUtil.getAsModuleBlock(moduleBlock)
    }

    private class CollectVariablesVisitor(val name: String? = null) : HCLElementVisitor() {
      val collected: MutableSet<Variable> = HashSet()
      override fun visitBlock(o: HCLBlock) {
        if (!TerraformPatterns.VariableRootBlock.accepts(o)) return
        o.`object` ?: return
        if (name != null && name != o.getNameElementUnquoted(1)) return
        collected.add(Variable(o))
      }
    }

    private class CollectLocalsVisitor(val name: String? = null) : HCLElementVisitor() {
      val collected: MutableSet<Pair<String, HCLProperty>> = HashSet()
      override fun visitBlock(o: HCLBlock) {
        if (!TerraformPatterns.LocalsRootBlock.accepts(o)) return

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
            return CachedValueProvider.Result(false, module.item)
          }
        }
        return CachedValueProvider.Result(true, module.item)
      }

    }
  }

  constructor(file: HCLFile) : this(file as PsiFileSystemItem)

  constructor(directory: PsiDirectory) : this(directory as PsiFileSystemItem)

  fun getAllVariables(): List<Variable> {
    val visitor = CollectVariablesVisitor()
    process(PsiElementProcessor { file -> file.acceptChildren(visitor); true })
    return visitor.collected.toList()
  }

  @Deprecated("There may be declaration duplicates", replaceWith = ReplaceWith("findVariables(name)"))
  fun findVariable(name: String): Variable? {
    val visitor = CollectVariablesVisitor(name)
    process(PsiElementProcessor { file -> file.acceptChildren(visitor); visitor.collected.isEmpty() })
    return visitor.collected.firstOrNull()
  }

  fun findVariables(name: String): List<Variable> {
    val visitor = CollectVariablesVisitor(name)
    process(PsiElementProcessor { file -> file.acceptChildren(visitor); true })
    return visitor.collected.toList()
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

  // val helper = PsiSearchHelper.SERVICE.getInstance(position.project)
  // helper.processAllFilesWithWord()

  private fun process(processor: PsiElementProcessor<HCLFile>): Boolean {
    // TODO: Support json files (?)
    if (item is HCLFile) {
      if (item.language == TerraformLanguage) {
        return processor.execute(item)
      }
      return false
    }
    assert(item is PsiDirectory)
    return item.processChildren(PsiElementProcessor { element ->
      if (element !is HCLFile || element.language != TerraformLanguage) return@PsiElementProcessor true
      processor.execute(element)
    })
  }

  fun findResources(type: String?, name: String?): List<HCLBlock> {
    val found = ArrayList<HCLBlock>()
    process(PsiElementProcessor { file ->
      file.acceptChildren(object : HCLElementVisitor() {
        override fun visitBlock(o: HCLBlock) {
          if ("resource" != o.getNameElementUnquoted(0)) return
          val t = o.getNameElementUnquoted(1) ?: return
          if (type != null && type != t) return
          val n = o.getNameElementUnquoted(2) ?: return
          if (name == null || name == n) found.add(o)
        }
      }); true
    })
    return found
  }

  fun getDeclaredResources(): List<HCLBlock> {
    return findResources(null, null)
  }

  fun findDataSource(type: String?, name: String?): List<HCLBlock> {
    val found = ArrayList<HCLBlock>()
    process(PsiElementProcessor { file ->
      file.acceptChildren(object : HCLElementVisitor() {
        override fun visitBlock(o: HCLBlock) {
          if ("data" != o.getNameElementUnquoted(0)) return
          val t = o.getNameElementUnquoted(1) ?: return
          if (type != null && type != t) return
          val n = o.getNameElementUnquoted(2) ?: return
          if (name == null || name == n) found.add(o)
        }
      }); true
    })
    return found
  }

  fun getDeclaredDataSources(): List<HCLBlock> {
    return findDataSource(null, null)
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
          if ("provider" != o.getNameElementUnquoted(0)) return
          val tp = o.getNameElementUnquoted(1) ?: return
          val als = when (val value = o.`object`?.findProperty("alias")?.value) {
            is HCLStringLiteral -> value.value
            is HCLIdentifier -> value.id
            else -> null
          }
          if (alias == null && als == null) {
            if (type == tp) found.add(o)
          } else {
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
          if ("provider" != o.getNameElementUnquoted(0)) return
          val fqn = o.getProviderFQName() ?: return
          found.add(Pair(o, fqn))
        }
      }); true
    })
    return found
  }

  fun findModules(name: String): List<HCLBlock> {
    val found = ArrayList<HCLBlock>()
    process(PsiElementProcessor { file ->
      file.acceptChildren(object : HCLElementVisitor() {
        override fun visitBlock(o: HCLBlock) {
          if ("module" != o.getNameElementUnquoted(0)) return
          val n = o.getNameElementUnquoted(1) ?: return
          if (name == n) found.add(o)
        }
      }); true
    })
    return found
  }

  fun getDefinedModules(): List<HCLBlock> {
    val found = ArrayList<HCLBlock>()
    process(PsiElementProcessor { file ->
      file.acceptChildren(object : HCLElementVisitor() {
        override fun visitBlock(o: HCLBlock) {
          if ("module" != o.getNameElementUnquoted(0)) return
          o.getNameElementUnquoted(1) ?: return
          found.add(o)
        }
      }); true
    })
    return found
  }

  fun getDefinedOutputs(): List<HCLBlock> {
    val found = ArrayList<HCLBlock>()
    process(PsiElementProcessor { file ->
      file.acceptChildren(object : HCLElementVisitor() {
        override fun visitBlock(o: HCLBlock) {
          if ("output" != o.getNameElementUnquoted(0)) return
          o.getNameElementUnquoted(1) ?: return
          found.add(o)
        }
      }); true
    })
    return found
  }

  // Returns all 'terraform.required_version' defined in module.
  fun getTerraformRequiredVersion(): List<String> {
    val found = ArrayList<String>()
    process(PsiElementProcessor { file ->
      file.acceptChildren(object : HCLElementVisitor() {
        override fun visitBlock(o: HCLBlock) {
          if (!TerraformPatterns.TerraformRootBlock.accepts(o)) return
          val value = o.`object`?.findProperty(TypeModel.TerraformRequiredVersion.name)?.value ?: return
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
    return CachedValuesManager.getCachedValue(this.item, IsHCL2SupportedCachedValueProvider(this))
  }

  val model: TypeModel
    get() = TypeModelProvider.getModel(item.project)

  override fun equals(other: Any?): Boolean {
    if (other !is Module) return false
    val file = item.virtualFile
    return if (file != null) file == other.item.virtualFile
    else item == other.item
  }

  override fun hashCode(): Int {
    return (item.virtualFile ?: item).hashCode()
  }

  fun getType(): Type {
    val result = HashMap<String, Type?>()

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
