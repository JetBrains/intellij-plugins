package org.angular2.lang.html.tcb

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.ecmascript6.psi.ES6ImportExportSpecifier.ImportExportSpecifierKind
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptType
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind.NG_PIPE
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.codeInsight.config.Angular2TypeCheckingConfig
import org.angular2.entities.Angular2ClassBasedDirective
import org.angular2.entities.Angular2ClassBasedEntity
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2EntityUtils.NG_ACCEPT_INPUT_TYPE_PREFIX
import org.angular2.entities.Angular2Pipe
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.Angular2PipeExpression
import org.angular2.lang.html.tcb.Angular2TemplateTranspiler.DiagnosticKind
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


internal class Environment(
  val config: Angular2TypeCheckingConfig,
  val file: PsiFile,
) : Angular2TemplateTranspiler.FileContext {

  private val importCache = ConcurrentHashMap<PsiElement, String>()
  private val packageImport2name = ConcurrentHashMap<ImportInfo, String>()
  private val dir2ctor = ConcurrentHashMap<Angular2Directive, String>()
  private val pipe2ctor = ConcurrentHashMap<Angular2Pipe, String>()

  private val nextImportNameId = AtomicInteger(1)
  private val nextDirNameId = AtomicInteger(1)
  private val nextPipeNameId = AtomicInteger(1)

  /**
   * Generate a `Expression` that references the given node.
   *
   * This may involve importing the node into the file if it's not declared there already.
   */
  fun reference(ref: TypeScriptClass): Expression =
    Expression(reference(ref, ImportExportSpecifierKind.IMPORT))

  fun referenceExternalType(packageName: String, symbol: String): Expression =
    Expression(getModuleImportName(packageName, false, ImportExportSpecifierKind.IMPORT) + "." + symbol)

  fun referenceExternalSymbol(moduleName: String, name: String): Expression =
    Expression(getModuleImportName(moduleName, false, ImportExportSpecifierKind.IMPORT) + "." + name)

  fun referenceType(dirTypeRef: JSType): Expression =
    // TODO detect stuff to import
    Expression(dirTypeRef.getTypeText(JSType.TypeTextFormat.CODE))

  private fun reference(element: JSQualifiedNamedElement, kind: ImportExportSpecifierKind): String =
    importCache.computeIfAbsent(element) {
      if (element.containingFile == file) {
        return@computeIfAbsent element.qualifiedName ?: element.name!!
      }
      val importDescriptor = ES6CreateImportUtil.getImportDescriptor(
        null, element, element.containingFile.virtualFile, file, true)
                             ?: throw RuntimeException("Cannot import class ${element.qualifiedName ?: element.name} from file ${element.containingFile.virtualFile}")
      assert(importDescriptor.importType.let {
        !it.isBare && !it.isComposite && !it.isNamespace && !it.isBindingAll && !it.isTypeScriptRequire
      }) {
        importDescriptor.importType
      }
      assert(importDescriptor.importType.let {
        it.isES6 && (it.isSpecifier || it.isDefault)
      }) {
        importDescriptor.importType
      }

      val importName = getModuleImportName(importDescriptor.moduleName, importDescriptor.importType.isDefault, kind)
      if (importDescriptor.importType.isDefault) {
        return@computeIfAbsent importName
      }
      else {
        return@computeIfAbsent importName + "." + importDescriptor.effectiveName
      }
    }

  private fun getModuleImportName(
    moduleName: String,
    isDefault: Boolean,
    kind: ImportExportSpecifierKind,
  ): String {
    val importInfo = ImportInfo(moduleName, isDefault, kind)
    return packageImport2name.computeIfAbsent(importInfo) { "_i" + nextImportNameId.getAndIncrement() }
  }

  fun isExplicitlyDeferred(directive: TmplDirectiveMetadata): Boolean {
    // TODO support blocks
    return false
  }

  fun typeCtorFor(dir: TmplDirectiveMetadata): Expression {
    val name = dir2ctor.computeIfAbsent(dir.directive) {
      "_ctor" + nextDirNameId.getAndIncrement()
    }
    return Expression(name)
  }

  fun pipeInst(pipeInfo: Angular2Pipe): Expression {
    val name = pipe2ctor.computeIfAbsent(pipeInfo) {
      "_pipe" + nextPipeNameId.getAndIncrement()
    }
    return Expression(name)
  }

  fun canReferenceType(ref: TypeScriptSingleType): Boolean {
    return true
  }

  override fun getCommonCode(): String = Expression {
    statements {
      val pipeStatements = getPipeStatements()
      val directiveStatements = getDirectiveStatements()
      getImportStatements().takeIf { it.isNotEmpty() }?.let {
        appendStatement { append("/* Imports */") }
        it.forEach(::appendStatement)
      }
      pipeStatements.takeIf { it.isNotEmpty() }?.let {
        appendStatement { append("/* Pipes */") }
        it.forEach(::appendStatement)
      }
      directiveStatements.takeIf { it.isNotEmpty() }?.let {
        appendStatement { append("/* Directives */") }
        it.forEach(::appendStatement)
      }
    }
  }.toString()

  private fun getImportStatements(): List<Statement> =
    packageImport2name.entries.asSequence().sortedBy { it.value }.map { (importInfo, varName) ->
      Statement {
        when (importInfo.kind) {
          ImportExportSpecifierKind.IMPORT -> append("import ")
          ImportExportSpecifierKind.IMPORT_TYPE -> append("import type ")
          ImportExportSpecifierKind.IMPORT_TYPEOF -> append("import typeof ")
          else -> throw IllegalStateException(importInfo.kind.toString())
        }
        if (importInfo.isDefault) {
          append(varName)
        }
        else {
          append("* as $varName")
        }
        append(" from \"${importInfo.packageName}\";")
      }
    }.toList()

  private fun getPipeStatements(): List<Statement> =
    pipe2ctor.entries.asSequence().sortedBy { it.value }.map { (pipe, name) ->
      tsDeclareVariable(Identifier(name, pipe.getName()), Expression {
        (pipe as? Angular2ClassBasedEntity)?.typeScriptClass
          ?.let { append(reference(it)) }
        ?: append("any")
      })
    }.toList()


  private fun getDirectiveStatements(): List<Statement> =
    dir2ctor.entries.asSequence().sortedBy { it.value }.map { (directive, name) ->
      val cls = (directive as? Angular2ClassBasedDirective)?.typeScriptClass?.takeIf { it.typeParameters.isNotEmpty() }
                ?: return@map Statement { append("const ${name} = null! as () => any;") }
      val typeName = reference(cls)
      return@map Statement {
        // const _ctor1 = null! as <T = any>(init: Pick<TestIf<T>, "ngIf" | "ngIfThen">) => TestIf<T>;
        append("const ${name} = null! as <")
        cls.typeParameters.forEachIndexed { index, typeScriptTypeParameter ->
          if (index > 0) {
            append(", ")
          }
          append(typeScriptTypeParameter.name ?: return@forEachIndexed)
          typeScriptTypeParameter.typeConstraint?.let { append(" extends ").append(it.toExpression()) }
          typeScriptTypeParameter.default.let {
            if (it == null)
              append(" = any")
            else
              append(" = ").append(it.toExpression())
          }
        }
        append(">(init: Pick<$typeName<")
        cls.typeParameters.forEachIndexed { index, typeScriptTypeParameter ->
          if (index > 0) {
            append(", ")
          }
          append(typeScriptTypeParameter.name ?: "?")
        }
        append(">, ")

        val coercedInputs = cls.staticJSType.asRecordType(cls)
          .properties
          .mapNotNullTo(LinkedHashSet()) { prop ->
            prop.memberName.takeIf { it.startsWith(NG_ACCEPT_INPUT_TYPE_PREFIX) }
              ?.substring(NG_ACCEPT_INPUT_TYPE_PREFIX.length)
          }

        directive.bindings.inputs
          .asSequence()
          .mapNotNull { input -> input.fieldName?.takeIf { !coercedInputs.contains(it) } }
          .distinct()
          .forEachIndexed { index, fieldName ->
            if (index > 0) {
              append(" | ")
            }
            append("\"$fieldName\"")
          }
        append(">")
        if (coercedInputs.isNotEmpty()) {
          append(" & {").newLine()
          for (input in coercedInputs) {
            append(input).append(": typeof $typeName.$NG_ACCEPT_INPUT_TYPE_PREFIX$input;").newLine()
          }
          append("}")
        }
        append(") => $typeName<")
        cls.typeParameters.forEachIndexed { index, typeScriptTypeParameter ->
          if (index > 0) {
            append(", ")
          }
          append(typeScriptTypeParameter.name ?: "?")
        }
        append(">;")
      }
    }.toList()

  private fun TypeScriptType.toExpression(): Expression = Expression {
    acceptChildren(object : JSRecursiveWalkingElementVisitor() {
      override fun visitElement(element: PsiElement) {
        if (element is LeafPsiElement) {
          append(element.text)
        }
        else {
          super.visitElement(element)
        }
      }

      override fun visitJSReferenceExpression(node: JSReferenceExpression) {
        if (node.qualifier == null) {
          var importSpecifierKind = ImportExportSpecifierKind.IMPORT
          val templateTarget = node.resolve().let { resolveResult ->
            if (resolveResult is ES6ImportSpecifier) {
              importSpecifierKind = resolveResult.specifierKind
              resolveResult.resolveOverAliases().firstOrNull { it.isValidResult && it.element != null }?.element
            }
            else
              resolveResult
          }
          if (templateTarget !is JSQualifiedNamedElement) {
            append(node.text, node.textRange, types = true)
          }
          else {
            append(reference(templateTarget, importSpecifierKind), node.textRange, types = true)
          }
        }
      }
    })
  }

  private data class ImportInfo(
    val packageName: String,
    val isDefault: Boolean,
    val kind: ImportExportSpecifierKind,
  )
}


internal class OutOfBandDiagnosticRecorder {

  private val diagnostics = mutableSetOf<Angular2TemplateTranspiler.Diagnostic>()

  fun getDiagnostics(): Set<Angular2TemplateTranspiler.Diagnostic> =
    diagnostics

  fun deferredComponentUsedEagerly(id: Any, node: TmplAstElement) {
  }

  fun suboptimalTypeInference(id: TemplateId, variables: Collection<TmplAstVariable>) {
  }

  fun duplicateTemplateVar(id: TemplateId, v: TmplAstVariable, firstDecl: TmplAstVariable) {
  }

  fun missingReferenceTarget(id: TemplateId, ref: TmplAstReference) {
  }

  fun splitTwoWayBinding(
    id: TemplateId, input: TmplAstBoundAttribute, output: TmplAstBoundEvent,
    directive: TmplDirectiveMetadata, outputConsumer: `TmplDirectiveMetadata|TmplAstElement`,
  ) {
  }

  fun missingPipe(pipeName: String?, pipeExpression: Angular2PipeExpression) {
    registerDiagnostics(
      DiagnosticKind.UnresolvedPipe,
      pipeExpression.methodExpression?.textRange ?: pipeExpression.textRange,
      Angular2Bundle.htmlMessage(
        "angular.inspection.unresolved-pipe.message",
        pipeName?.withColor(NG_PIPE, pipeExpression) ?: ""
      ),
      highlightType = ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
  }

  fun deferredPipeUsedEagerly(id: TemplateId, pipe: Angular2PipeExpression) {

  }

  fun illegalForLoopTrackAccess(id: TemplateId, block: TmplAstForLoopBlock, ast: JSReferenceExpression) {

  }

  fun controlFlowPreventingContentProjection(
    id: TemplateId, category: ts.DiagnosticCategory,
    child: TmplAstNode, componentName: String,
    originalSelector: String?, root: TmplAstBlockNodeWithChildren,
    hostPreserveWhitespaces: Boolean,
  ) {

  }

  fun conflictingDeclaration(id: TemplateId, second: TmplAstLetDeclaration) {

  }

  fun illegalWriteToLetDeclaration(id: TemplateId, node: JSReferenceExpression, target: TmplAstLetDeclaration) {

  }

  fun letUsedBeforeDefinition(id: TemplateId, node: JSReferenceExpression, target: TmplAstLetDeclaration) {

  }

  private fun registerDiagnostics(
    kind: DiagnosticKind,
    range: TextRange,
    message: String,
    category: String? = null,
    highlightType: ProblemHighlightType? = null,
  ) {
    diagnostics.add(DiagnosticData(
      kind, range.startOffset, range.length, message, category, highlightType
    ))
  }

}

internal fun requiresInlineTypeCtor(node: TypeScriptClass?, env: Environment): Boolean {
  // The class requires an inline type constructor if it has generic type bounds that can not be
  // emitted into the provided type-check environment.
  return node != null && !checkIfGenericTypeBoundsCanBeEmitted(node, env);
}

internal fun checkIfGenericTypeBoundsCanBeEmitted(node: TypeScriptClass, env: Environment): Boolean {
  // Generic type parameters are considered context free if they can be emitted into any context.
  val emitter = TypeParameterEmitter(node.typeParameters);
  return emitter.canEmit { ref -> env.canReferenceType(ref) };
}

internal data class DiagnosticData(
  override val kind: DiagnosticKind,
  override val startOffset: Int,
  override val length: Int,
  override val message: String,
  override val category: String? = null,
  override val highlightType: ProblemHighlightType? = null,
) : Angular2TemplateTranspiler.Diagnostic {
  override fun offsetBy(offset: Int): Angular2TemplateTranspiler.Diagnostic =
    copy(startOffset = startOffset + offset)
}
