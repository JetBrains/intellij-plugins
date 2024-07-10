package org.angular2.lang.html.tcb

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.typescript.compiler.TypeScriptCompilerConfigUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.SmartList
import com.intellij.util.containers.MultiMap
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.html.Angular2HtmlDialect
import org.angular2.lang.html.tcb.Angular2TemplateTranspiler.SourceMapping
import org.angular2.lang.html.tcb.Angular2TemplateTranspiler.TranspiledTemplate
import java.util.*
import java.util.function.Supplier
import kotlin.Comparator

object Angular2TranspiledComponentFileBuilder {

  private val mappingsComparator: Comparator<SourceMapping> =
    Comparator.comparingInt<SourceMapping?> { it.sourceOffset }.thenComparingInt { it.sourceLength }

  fun getTranspiledComponentAndTopLevelTemplateFile(context: PsiElement): Pair<TranspiledComponentFile, PsiFile>? {
    val templateFile = InjectedLanguageManager.getInstance(context.project).getTopLevelFile(context)
    val componentFile = if (templateFile.language is Angular2HtmlDialect)
      Angular2EntitiesProvider.findTemplateComponent(templateFile)?.sourceElement?.containingFile
      ?: return null
    else
      templateFile
    return getTranspiledComponentFile(componentFile)
      ?.let { Pair(it, templateFile) }
  }

  fun findComponentFile(context: PsiElement): PsiFile? {
    val templateFile = InjectedLanguageManager.getInstance(context.project).getTopLevelFile(context)
    return if (templateFile.language is Angular2HtmlDialect)
      Angular2EntitiesProvider.findTemplateComponent(templateFile)?.sourceElement?.containingFile
      ?: return null
    else
      templateFile
  }

  fun getTranspiledComponentFile(componentFile: PsiFile): TranspiledComponentFile? =
    CachedValuesManager.getCachedValue(componentFile) {
      CachedValueProvider.Result.create(getComponentFileCache(componentFile)?.let {
        buildTranspiledComponentFile(componentFile, it)
      }, PsiModificationTracker.MODIFICATION_COUNT)
    }

  private fun buildTranspiledComponentFile(componentFile: PsiFile, cache: ComponentFileCache): TranspiledComponentFile {
    val templates = cache.components.mapIndexedNotNull { index, cls ->
      CachedValuesManager.getCachedValue(cls) {
        val context = getComponentFileCache(cls.containingFile)!!.environment
        CachedValueProvider.Result.create(Angular2EntitiesProvider.getComponent(cls)?.let {
          JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(cls, Supplier {
            Angular2TemplateTranspiler.transpileTemplate(context, it, (index + 1).toString())
          })
        }, PsiModificationTracker.MODIFICATION_COUNT)
      }
    }
    return buildTranspiledComponentFile(cache.environment, componentFile, templates)
  }

  private fun getComponentFileCache(file: PsiFile): ComponentFileCache? =
    CachedValuesManager.getCachedValue(file) {
      val result = SmartList<TypeScriptClass>()
      file.acceptChildren(object : JSElementVisitor() {
        override fun visitTypeScriptClass(typeScriptClass: TypeScriptClass) {
          if (Angular2DecoratorUtil.findDecorator(typeScriptClass, Angular2DecoratorUtil.COMPONENT_DEC) != null)
            result.add(typeScriptClass)
        }

        override fun visitES6ExportDefaultAssignment(node: ES6ExportDefaultAssignment) {
          node.acceptChildren(this)
        }
      })
      CachedValueProvider.Result.create(
        if (result.isEmpty()) null else ComponentFileCache(result, Angular2TemplateTranspiler.createFileContext(file)),
        PsiModificationTracker.MODIFICATION_COUNT
      )
    }

  private class ComponentFileCache(
    val components: List<TypeScriptClass>,
    val environment: Environment,
  )

  private fun buildTranspiledComponentFile(
    context: Angular2TemplateTranspiler.FileContext,
    componentFile: PsiFile,
    templates: List<TranspiledTemplate>,
  ): TranspiledComponentFile {
    val generatedCode = StringBuilder()
    val componentFileText = componentFile.text
    generatedCode.append(componentFileText)

    val injectedLanguageManager = InjectedLanguageManager.getInstance(componentFile.project)
    val inlineTemplateRanges = SmartList<TextRange>()

    val componentFileMappings = SmartList<SourceMapping>()
    val componentFileContextVarMappings = mutableMapOf<TextRange, TextRange>()
    val componentFileDirectiveVarMappings = mutableMapOf<Pair<TextRange, Angular2Directive>, TextRange>()
    val diagnostics = MultiMap<PsiFile, Angular2TemplateTranspiler.Diagnostic>()
    val nameMaps = MultiMap<PsiFile, Pair<Int, Map<String, String>>>()
    val mappings = SmartList<FileMappings>()

    generatedCode.append("\n\n/* Angular type checking code */\n")
    generatedCode.append(context.getCommonCode())

    for (template in templates) {
      generatedCode.append("\n/* TCB for ")
        .append(template.templateFile.name)
        .append(" */\n\n")
      val generatedMappingsOffset = generatedCode.length
      generatedCode.append(template.generatedCode)

      val injectionHost = injectedLanguageManager.getInjectionHost(template.templateFile)
      if (injectionHost != null) {
        val hostRange = injectionHost.textRange
        inlineTemplateRanges.add(hostRange)
        val sourceMappingOffset = hostRange.startOffset + 1
        template.sourceMappings.mapTo(componentFileMappings) {
          it.offsetBy(sourceOffset = sourceMappingOffset, generatedOffset = generatedMappingsOffset)
        }
        template.contextVarMappings
          .associateTo(componentFileContextVarMappings) { mapping ->
            Pair(mapping.getElementNameRangeWithOffset(sourceMappingOffset),
                 mapping.getGeneratedRangeWithOffset(generatedMappingsOffset))
          }
        template.directiveVarMappings
          .associateTo(componentFileDirectiveVarMappings) { mapping ->
            Pair(
              Pair(mapping.getElementNameRangeWithOffset(sourceMappingOffset), mapping.directive),
              mapping.getGeneratedRangeWithOffset(generatedMappingsOffset)
            )
          }
        diagnostics.putValues(injectionHost.containingFile, template.diagnostics.map { it.offsetBy(sourceMappingOffset) })
        nameMaps.putValues(injectionHost.containingFile, template.nameMappings.map { (offset, map) -> Pair(offset + sourceMappingOffset, map) })
      }
      else {
        val fileMappings = template.sourceMappings.map {
          it.offsetBy(generatedOffset = generatedMappingsOffset)
        }
        val contextVarMappings = template.contextVarMappings.associate { mapping ->
          Pair(mapping.getElementNameRangeWithOffset(0), mapping.getGeneratedRangeWithOffset(generatedMappingsOffset))
        }
        val directiveVarMappings = template.directiveVarMappings.associate { mapping ->
          Pair(Pair(mapping.getElementNameRangeWithOffset(0), mapping.directive), mapping.getGeneratedRangeWithOffset(generatedMappingsOffset))
        }
        mappings.add(FileMappings(template.templateFile, fileMappings.sorted(), contextVarMappings, directiveVarMappings))
        diagnostics.putValues(template.templateFile, template.diagnostics)
        nameMaps.putValues(template.templateFile, template.nameMappings)
      }
    }
    inlineTemplateRanges.sortBy { it.startOffset }
    var lastRangeEnd = 0
    for (inlineTemplateRange in inlineTemplateRanges + TextRange(componentFileText.length, componentFileText.length)) {
      val sourceLength = inlineTemplateRange.startOffset - lastRangeEnd
      componentFileMappings.add(SourceMappingData(
        lastRangeEnd,
        sourceLength,
        lastRangeEnd,
        sourceLength,
        diagnosticsOffset = lastRangeEnd,
        diagnosticsLength = sourceLength,
        types = true,
      ))
      lastRangeEnd = inlineTemplateRange.endOffset
    }

    mappings.add(FileMappings(componentFile, componentFileMappings, componentFileContextVarMappings, componentFileDirectiveVarMappings))
    return TranspiledComponentFile(
      componentFile,
      generatedCode.toString(),
      mappings.associateBy { it.sourceFile },
      diagnostics.entrySet().associateBy({ it.key }, { it.value.toList() }),
      nameMaps.entrySet().associateBy({ it.key }, { pair -> pair.value.associateByTo(TreeMap(), { it.first }, { it.second }) }),
    ).also {
      if (ApplicationManager.getApplication().isUnitTestMode) it.verifyMappings()
    }
  }

  private fun List<SourceMapping>.sorted(): List<SourceMapping> =
    sortedWith(mappingsComparator)

  data class TranspiledComponentFile(
    val originalFile: PsiFile,
    val generatedCode: String,
    val fileMappings: Map<PsiFile, FileMappings>,
    val diagnostics: Map<PsiFile, List<Angular2TemplateTranspiler.Diagnostic>>,
    val nameMaps: Map<PsiFile, NavigableMap<Int, Map<String, String>>>,
  )

  class FileMappings(
    val sourceFile: PsiFile,
    val sourceMappings: List<SourceMapping>,
    val contextVarMappings: Map<TextRange, TextRange>,
    val directiveVarMappings: Map<Pair<TextRange, Angular2Directive>, TextRange>,
  ) {
    val fileName = this.sourceFile.viewProvider.virtualFile.let { TypeScriptCompilerConfigUtil.normalizeNameAndPath(it) }
                   ?: "<non-local>"
  }

}
