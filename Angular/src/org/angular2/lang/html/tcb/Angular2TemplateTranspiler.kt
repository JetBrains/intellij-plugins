package org.angular2.lang.html.tcb

import org.angular2.codeInsight.config.Angular2Compiler
import org.angular2.entities.Angular2ClassBasedComponent
import org.angular2.entities.Angular2Component
import org.angular2.lang.html.Angular2HtmlFile
import java.util.concurrent.atomic.AtomicInteger

object Angular2TemplateTranspiler {

  private val nextId = AtomicInteger()

  fun transpileTemplate(component: Angular2Component): TranspiledTemplate {
    val boundTarget = BoundTarget(component)
    val context = Context(
      Environment(Angular2Compiler.getTypeCheckingConfig(component.sourceElement)),
      OutOfBandDiagnosticRecorder(), nextId.getAndIncrement().toString(),
      boundTarget,
    )

    val scope = Scope.forNodes(context, null, null, boundTarget.templateRoots, null)
    val statements = scope.render()

    return Expression {
      append("function _tcb_${context.id}")

      val cls = (component as? Angular2ClassBasedComponent)?.typeScriptClass
      if (cls != null) {
        val typeParameters = cls.typeParameters
        if (typeParameters.isNotEmpty()) {
          append("<")
          typeParameters.forEachIndexed { i, param ->
            if (i > 0) {
              append(", ")
            }
            append(param.text)
          }
          append(">")
        }
        append("(this: ").append(cls.name ?: "never")
        if (typeParameters.isNotEmpty()) {
          append("<")
          typeParameters.forEachIndexed { i, param ->
            if (i > 0) {
              append(", ")
            }
            append(param.name ?: "never")
          }
          append(">")
        }
        append(") ")
      } else {
        append("() ")
      }
      codeBlock {
        for (it in context.env.getPipeStatements() + context.env.getDirectiveStatements() + statements) {
          appendStatement(it)
        }
      }
    }.asTranspiledTemplate(
      (component.templateFile as? Angular2HtmlFile)?.text ?: "",
      emptyList()
    )
  }

  interface TranspiledTemplate {
    val sourceCode: String
    val generatedCode: String
    val sourceMappings: List<SourceMapping>
    val imports: List<TypeScriptImport>
  }

  interface SourceMapping {
    val sourceOffset: Int
    val sourceLength: Int
    val generatedOffset: Int
    val generatedLength: Int
  }

  interface TypeScriptImport {
    val symbolName: String
    val packageName: String
  }
}