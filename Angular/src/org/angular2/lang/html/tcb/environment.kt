package org.angular2.lang.html.tcb

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSTypeSource
import org.angular2.codeInsight.config.Angular2TypeCheckingConfig
import org.angular2.entities.Angular2ClassBasedDirective
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2Pipe


class Environment(val config: Angular2TypeCheckingConfig) {

  private val dir2ctor = mutableMapOf<Angular2Directive, String>()
  private val pipe2ctor = mutableMapOf<Angular2Pipe, String>()

  /**
   * Generate a `ts.Expression` that references the given node.
   *
   * This may involve importing the node into the file if it's not declared there already.
   */
  fun reference(ref: TypeScriptClass): ts.Expression {
    // Disable aliasing for imports generated in a template type-checking context, as there is no
    // guarantee that any alias re-exports exist in the .d.ts files. It's safe to use direct imports
    // in these cases as there is no strict dependency checking during the template type-checking
    // pass.
    return ts.Expression().append(ref.name!!)
  }

  fun isExplicitlyDeferred(directive: Angular2Directive): Boolean {
    // TODO support blocks
    return false
  }

  fun referenceExternalType(packageName: String, symbol: String): ts.Expression {
    return ts.Expression().append(symbol)
  }

  fun typeCtorFor(dir: Angular2Directive): ts.Expression {
    var name = dir2ctor[dir]
    if (name == null) {
      name = "_ctor" + (dir2ctor.size + 1)
      dir2ctor[dir] = name
    }
    return ts.Expression().append(name)
  }

  fun pipeInst(pipeInfo: Angular2Pipe): ts.Expression {
    var name = pipe2ctor[pipeInfo]
    if (name == null) {
      name = "_pipe" + (pipe2ctor.size + 1)
      pipe2ctor[pipeInfo] = name
    }
    return ts.Expression().append(name)
  }

  fun canReferenceType(ref: TypeScriptSingleType): Boolean {
    return true
  }

  fun referenceExternalSymbol(moduleName: String, name: String): ts.Expression {
    return ts.Expression().append(name)
  }

  fun getPipeStatements(): List<ts.Statement> =
    pipe2ctor.map {
      tsDeclareVariable(ts.Identifier(it.value), it.key.entityJsType ?: JSAnyType.get(JSTypeSource.EMPTY_TS_EXPLICITLY_DECLARED))
    }

  fun getDirectiveStatements(): List<ts.Statement> =
    dir2ctor.map { (directive, name) ->
      val typeName = directive.entitySourceName
      val cls = (directive as? Angular2ClassBasedDirective)?.typeScriptClass?.takeIf { it.typeParameters.isNotEmpty() }
                ?: return@map ts.Statement(ts.Expression().append("const ${name} = null! as () => ${typeName};"))
      return@map ts.Statement(ts.Expression().apply {
        // const _ctor1 = null! as <T = any>(init: Pick<TestIf<T>, "ngIf" | "ngIfThen">) => TestIf<T>;
        append("const ${name} = null! as <")
        cls.typeParameters.forEachIndexed { index, typeScriptTypeParameter ->
          if (index > 0) {
            append(", ")
          }
          append(typeScriptTypeParameter.text)
          append(" = any")
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
      })
    }

}

class OutOfBandDiagnosticRecorder {
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

fun requiresInlineTypeCtor(node: TypeScriptClass?, env: Environment): Boolean {
  // The class requires an inline type constructor if it has generic type bounds that can not be
  // emitted into the provided type-check environment.
  return node != null && !checkIfGenericTypeBoundsCanBeEmitted(node, env);
}

fun checkIfGenericTypeBoundsCanBeEmitted(node: TypeScriptClass, env: Environment): Boolean {
  // Generic type parameters are considered context free if they can be emitted into any context.
  val emitter = TypeParameterEmitter(node.typeParameters);
  return emitter.canEmit { ref -> env.canReferenceType(ref) };
}