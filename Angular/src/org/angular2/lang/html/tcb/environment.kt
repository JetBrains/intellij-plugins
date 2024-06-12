package org.angular2.lang.html.tcb

import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.angular2.codeInsight.config.Angular2TypeCheckingConfig
import org.angular2.entities.Angular2ClassBasedDirective
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2Pipe
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


internal class Environment(val config: Angular2TypeCheckingConfig,
                           val file: PsiFile) : Angular2TemplateTranspiler.FileContext {

  private val importCache = ConcurrentHashMap<PsiElement, Expression>()
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
    importCache.computeIfAbsent(ref) {
      if (ref.containingFile == file) {
        return@computeIfAbsent Expression(ref.qualifiedName ?: ref.name!!)
      }
      val importDescriptor = ES6CreateImportUtil.getImportDescriptor(
        null, ref, ref.containingFile.virtualFile, file, true)
                             ?: throw RuntimeException("Cannot import class ${ref.qualifiedName ?: ref.name} from file ${ref.containingFile.virtualFile}")
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

      val importInfo = ImportInfo(importDescriptor.moduleName, importDescriptor.importType.isDefault)
      val importName = packageImport2name.computeIfAbsent(importInfo) { "_i" + nextImportNameId.getAndIncrement() }
      if (importInfo.isDefault) {
        return@computeIfAbsent Expression(importName)
      }
      else {
        return@computeIfAbsent Expression(importName + "." + importDescriptor.effectiveName)
      }
    }

  fun referenceExternalType(packageName: String, symbol: String): Expression {
    // TODO record an import
    return Expression(symbol)
  }

  fun referenceExternalSymbol(moduleName: String, name: String): Expression {
    return Expression(name)
  }

  fun isExplicitlyDeferred(directive: Angular2Directive): Boolean {
    // TODO support blocks
    return false
  }

  fun typeCtorFor(dir: Angular2Directive): Expression {
    val name = dir2ctor.computeIfAbsent(dir) {
      "_ctor" + nextDirNameId.getAndIncrement()
    }
    return Expression(name)
  }

  fun pipeInst(pipeInfo: Angular2Pipe): Expression {
    var name = pipe2ctor.computeIfAbsent(pipeInfo) {
      "_pipe" + nextPipeNameId.getAndIncrement()
    }
    return Expression(name)
  }

  fun canReferenceType(ref: TypeScriptSingleType): Boolean {
    return true
  }

  override fun getCommonCode(): String = Expression {
    statements {
      getImportStatements().takeIf { it.isNotEmpty() }?.let {
        appendStatement { append("/* Imports */") }
        it.forEach(::appendStatement)
      }
      getPipeStatements().takeIf { it.isNotEmpty() }?.let {
        appendStatement { append("/* Pipes */") }
        it.forEach(::appendStatement)
      }
      getDirectiveStatements().takeIf { it.isNotEmpty() }?.let {
        appendStatement { append("/* Directives */") }
        it.forEach(::appendStatement)
      }
    }
  }.toString()

  private fun getImportStatements(): List<Statement> =
    packageImport2name.entries.asSequence().sortedBy { it.value }.map { (importInfo, varName) ->
      if (importInfo.isDefault) {
        Statement { append("import $varName from \"${importInfo.packageName}\";") }
      }
      else {
        Statement { append("import * as $varName from \"${importInfo.packageName}\";") }
      }
    }.toList()

  private fun getPipeStatements(): List<Statement> =
    pipe2ctor.entries.asSequence().sortedBy { it.value }.map {
      tsDeclareVariable(Identifier(it.value), it.key.entityJsType
                                              ?: JSAnyType.get(JSTypeSource.EMPTY_TS_EXPLICITLY_DECLARED))
    }.toList()


  private fun getDirectiveStatements(): List<Statement> =
    dir2ctor.entries.asSequence().sortedBy { it.value }.map { (directive, name) ->
      val typeName = directive.entitySourceName
      val cls = (directive as? Angular2ClassBasedDirective)?.typeScriptClass?.takeIf { it.typeParameters.isNotEmpty() }
                ?: return@map Statement { append("const ${name} = null! as () => ${typeName};") }
      return@map Statement {
        // const _ctor1 = null! as <T = any>(init: Pick<TestIf<T>, "ngIf" | "ngIfThen">) => TestIf<T>;
        append("const ${name} = null! as <")
        cls.typeParameters.forEachIndexed { index, typeScriptTypeParameter ->
          if (index > 0) {
            append(", ")
          }
          append(typeScriptTypeParameter.text)
          if (typeScriptTypeParameter.default == null) {
            append(" = any")
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
        directive.bindings.inputs
          .asSequence()
          .mapNotNull { it.fieldName }
          .distinct()
          .forEachIndexed { index, fieldName ->
            if (index > 0) {
              append(" | ")
            }
            append("\"$fieldName\"")
          }
        append(">) => $typeName<")
        cls.typeParameters.forEachIndexed { index, typeScriptTypeParameter ->
          if (index > 0) {
            append(", ")
          }
          append(typeScriptTypeParameter.name ?: "?")
        }
        append(">;")
      }
    }.toList()

  private data class ImportInfo(val packageName: String, val isDefault: Boolean)

  private data class TypeScriptImportData(
    override val symbolName: String,
    override val packageName: String,
    override val isDefault: Boolean
  ) : Angular2TemplateTranspiler.TypeScriptImport
}

internal class OutOfBandDiagnosticRecorder {
  fun deferredComponentUsedEagerly(id: Any, node: TmplAstElement) {
  }

  fun suboptimalTypeInference(id: TemplateId, variables: Collection<TmplAstVariable>) {
  }

  fun duplicateTemplateVar(id: TemplateId, v: TmplAstVariable, firstDecl: TmplAstVariable) {
  }

  fun missingReferenceTarget(id: TemplateId, ref: TmplAstReference) {
  }

  fun splitTwoWayBinding(id: TemplateId, input: TmplAstBoundAttribute, output: TmplAstBoundEvent,
                         directive: Angular2Directive, outputConsumer: `Angular2Directive|TmplAstElement`) {
  }

  fun missingPipe(id: TemplateId, ast: AST) {
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