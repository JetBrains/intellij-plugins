package org.angular2.lang.expr.service.tcb

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.service.withServiceTraceSpan
import com.intellij.lang.typescript.compiler.TypeScriptCompilerConfigUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.SmartList
import com.intellij.util.containers.MultiMap
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.html.Angular2HtmlDialect
import org.angular2.lang.expr.service.tcb.Angular2TemplateTranspiler.SourceMapping
import org.angular2.lang.expr.service.tcb.Angular2TemplateTranspiler.SourceMappingFlag
import org.angular2.lang.expr.service.tcb.Angular2TemplateTranspiler.TranspiledCode
import org.angular2.lang.expr.service.tcb.Angular2TemplateTranspiler.TranspiledHostBindings
import org.angular2.lang.expr.service.tcb.Angular2TemplateTranspiler.TranspiledTemplate
import java.util.*

object Angular2TranspiledDirectiveFileBuilder {

  private val mappingsComparator: Comparator<SourceMapping> =
    Comparator.comparingInt<SourceMapping?> { it.sourceOffset }.thenComparingInt { it.sourceLength }

  fun getTranspiledDirectiveAndTopLevelSourceFile(context: PsiElement): Pair<TranspiledDirectiveFile, PsiFile>? = withServiceTraceSpan("getTranspiledDirectiveAndTopLevelSourceFile") {
    if (DumbService.isDumb(context.project)) return@withServiceTraceSpan null
    val topLevelFile = InjectedLanguageManager.getInstance(context.project).getTopLevelFile(context)
    val directiveFile = if (topLevelFile.language is Angular2HtmlDialect)
      Angular2EntitiesProvider.findTemplateComponent(topLevelFile)?.sourceElement?.containingFile
      ?: return@withServiceTraceSpan null
    else
      topLevelFile
    return@withServiceTraceSpan getTranspiledDirectiveFile(directiveFile)
      ?.let { Pair(it, topLevelFile) }
  }

  fun findDirectiveFile(context: PsiElement): PsiFile? {
    val topLevelFile = InjectedLanguageManager.getInstance(context.project).getTopLevelFile(context)
    return if (topLevelFile.language is Angular2HtmlDialect)
      Angular2EntitiesProvider.findTemplateComponent(topLevelFile)?.sourceElement?.containingFile
      ?: return null
    else
      topLevelFile
  }

  fun getTranspiledComponentFileForTemplateFile(project: Project, templateFile: VirtualFile): TranspiledDirectiveFile? {
    val templatePsiFile = PsiManager.getInstance(project).findFile(templateFile)
                          ?: return null
    val componentFile = findDirectiveFile(templatePsiFile)
                        ?: return null
    return getTranspiledDirectiveFile(componentFile)
  }

  fun getTranspiledDirectiveFile(directiveFile: PsiFile): TranspiledDirectiveFile? =
    CachedValuesManager.getCachedValue(directiveFile) {
      CachedValueProvider.Result.create(getDirectiveFileCache(directiveFile)?.let {
        withTypeEvaluationLocation(directiveFile) {
          buildTranspiledDirectiveFile(directiveFile, it)
        }
      }, PsiModificationTracker.MODIFICATION_COUNT)
    }

  private fun buildTranspiledDirectiveFile(directiveFile: PsiFile, cache: DirectiveFileCache): TranspiledDirectiveFile = withServiceTraceSpan("buildTranspiledComponentFile") {
    val templates = cache.components.mapIndexedNotNull { index, cls ->
      CachedValuesManager.getCachedValue(cls) {
        val context = getDirectiveFileCache(cls.containingFile)!!.environment
        CachedValueProvider.Result.create(Angular2EntitiesProvider.getComponent(cls)?.let {
          Angular2TemplateTranspiler.transpileTemplate(context, it, (index + 1).toString())
        }, PsiModificationTracker.MODIFICATION_COUNT)
      }
    }
    val hostBindings = cache.directives.mapIndexedNotNull { index, cls ->
      CachedValuesManager.getCachedValue(cls) {
        val context = getDirectiveFileCache(cls.containingFile)!!.environment
        CachedValueProvider.Result.create(
          Angular2TemplateTranspiler.transpileHostBindings(context, cls, (index + 1).toString()),
          PsiModificationTracker.MODIFICATION_COUNT)
      }
    }
    return@withServiceTraceSpan buildTranspiledDirectiveFile(cache.environment, directiveFile, hostBindings, templates)
  }

  private fun getDirectiveFileCache(file: PsiFile): DirectiveFileCache? =
    CachedValuesManager.getCachedValue(file) {
      val directives = SmartList<TypeScriptClass>()
      val components = SmartList<TypeScriptClass>()
      file.acceptChildren(object : JSElementVisitor() {
        override fun visitTypeScriptClass(typeScriptClass: TypeScriptClass) {
          if (Angular2DecoratorUtil.findDecorator(typeScriptClass, true, Angular2DecoratorUtil.COMPONENT_DEC) != null) {
            components.add(typeScriptClass)
            directives.add(typeScriptClass)
          } else if (Angular2DecoratorUtil.findDecorator(typeScriptClass, true, Angular2DecoratorUtil.DIRECTIVE_DEC) != null) {
            directives.add(typeScriptClass)
          }
        }

        override fun visitES6ExportDefaultAssignment(node: ES6ExportDefaultAssignment) {
          node.acceptChildren(this)
        }
      })
      CachedValueProvider.Result.create(
        if (directives.isEmpty() && components.isEmpty())
          null
        else
          DirectiveFileCache(directives, components, Angular2TemplateTranspiler.createFileContext(file)),
        PsiModificationTracker.MODIFICATION_COUNT
      )
    }

  private class DirectiveFileCache(
    val directives: List<TypeScriptClass>,
    val components: List<TypeScriptClass>,
    val environment: Environment,
  )

  private fun buildTranspiledDirectiveFile(
    context: Angular2TemplateTranspiler.FileContext,
    directiveFile: PsiFile,
    directiveHostBindings: List<TranspiledHostBindings>,
    templates: List<TranspiledTemplate>,
  ): TranspiledDirectiveFile {
    val generatedCode = StringBuilder()
    val componentFileText = directiveFile.text
    generatedCode.append(componentFileText)

    val injectedLanguageManager = InjectedLanguageManager.getInstance(directiveFile.project)
    val inlineTemplateRanges = SmartList<TextRange>()

    val componentFileMappings = SmartList<SourceMapping>()
    val directiveFileContextVarMappings = mutableMapOf<TextRange, TextRange>()
    val componentFileDirectiveVarMappings = mutableMapOf<Pair<TextRange, Angular2Directive>, TextRange>()
    val diagnostics = MultiMap<PsiFile, Angular2TemplateTranspiler.Diagnostic>()
    val nameMaps = MultiMap<PsiFile, Pair<Int, Map<String, String>>>()
    val mappings = SmartList<FileMappings>()

    fun contributeInlineTranspilation(template: TranspiledCode, sourceFile: PsiFile, generatedMappingsOffset: Int, sourceMappingOffset: Int) {
      template.sourceMappings.mapTo(componentFileMappings) {
        it.offsetBy(sourceOffset = sourceMappingOffset, generatedOffset = generatedMappingsOffset)
      }
      template.contextVarMappings
        .associateTo(directiveFileContextVarMappings) { mapping ->
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
      diagnostics.putValues(sourceFile, template.diagnostics.map { it.offsetBy(sourceMappingOffset) })
      nameMaps.putValues(sourceFile, template.nameMappings.map { (offset, map) -> Pair(offset + sourceMappingOffset, map) })
    }

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
        contributeInlineTranspilation(template, injectionHost.containingFile, generatedMappingsOffset, sourceMappingOffset)
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
    for (hostBindings in directiveHostBindings) {
      generatedCode.append("\n/* TCB for host bindings of ")
        .append(hostBindings.cls.getName())
        .append(" */\n\n")
      val generatedMappingsOffset = generatedCode.length
      generatedCode.append(hostBindings.generatedCode)
      inlineTemplateRanges.addAll(hostBindings.inlineCodeRanges)
      contributeInlineTranspilation(hostBindings, hostBindings.cls.containingFile, generatedMappingsOffset, 0)
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
        flags = EnumSet.allOf(SourceMappingFlag::class.java),
      ))
      lastRangeEnd = inlineTemplateRange.endOffset
    }

    mappings.add(FileMappings(directiveFile, componentFileMappings, directiveFileContextVarMappings, componentFileDirectiveVarMappings))
    return TranspiledDirectiveFile(
      directiveFile,
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

  data class TranspiledDirectiveFile(
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
    val fileName: String = this.sourceFile.viewProvider.virtualFile.let { TypeScriptCompilerConfigUtil.normalizeNameAndPath(it) }
                        ?: "<non-local>"
  }

}
