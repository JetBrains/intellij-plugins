package org.angular2.lang.html.tcb

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.javascript.web.html.XmlASTWrapperPsiElement
import com.intellij.lang.javascript.psi.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfTypes
import com.intellij.psi.xml.*
import com.intellij.util.asSafely
import com.intellij.xml.util.XmlTagUtil
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.tags.Angular2ElementDescriptor
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider
import org.angular2.codeInsight.template.Angular2TemplateScopesResolver
import org.angular2.codeInsight.template.isTemplateTag
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2Pipe
import org.angular2.lang.Angular2LangUtil.OUTPUT_CHANGE_SUFFIX
import org.angular2.lang.expr.psi.*
import org.angular2.lang.html.Angular2HtmlFile
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType.*
import org.angular2.lang.html.psi.Angular2HtmlEvent
import org.angular2.lang.html.psi.Angular2HtmlExpansionForm
import org.angular2.lang.html.psi.Angular2HtmlExpansionFormCase
import org.angular2.lang.html.psi.PropertyBindingType
import java.util.*

internal sealed interface TmplAstNode

internal sealed interface TmplAstExpressionSymbol : TmplAstNode {
  val name: String
  val keySpan: TextRange?
  val value: String?
  val valueSpan: TextRange?
}

internal sealed interface TmplAstDirectiveContainer : TmplAstNode {
  val directives: Set<Angular2Directive>
  val inputs: Map<String, TmplAstBoundAttribute>
  val outputs: Map<String, TmplAstBoundEvent>
  val attributes: Map<String, TmplAstTextAttribute>
  val references: Map<String, TmplAstReference>
  val startSourceSpan: TextRange?
  val children: List<TmplAstNode>
}

internal sealed interface TmplAstAttribute : TmplAstNode {
  val name: String
  val keySpan: TextRange?
  val sourceSpan: TextRange
}

internal class TmplAstVariable(
  override val name: String,
  override val value: String?,
  override val keySpan: TextRange?,
  override val valueSpan: TextRange?,
) : TmplAstExpressionSymbol

internal class TmplAstElement(
  val name: String,
  override val directives: Set<Angular2Directive>,
  override val inputs: Map<String, TmplAstBoundAttribute>,
  override val outputs: Map<String, TmplAstBoundEvent>,
  override val attributes: Map<String, TmplAstTextAttribute>,
  override val references: Map<String, TmplAstReference>,
  override val startSourceSpan: TextRange?,
  override val children: List<TmplAstNode>,
) : TmplAstDirectiveContainer

internal class TmplAstTemplate(
  val tagName: String?,
  val templateAttrs: List<TmplAstAttribute>,
  override val directives: Set<Angular2Directive>,
  override val inputs: Map<String, TmplAstBoundAttribute>,
  override val outputs: Map<String, TmplAstBoundEvent>,
  override val attributes: Map<String, TmplAstTextAttribute>,
  override val references: Map<String, TmplAstReference>,
  val variables: Map<String, TmplAstVariable>,
  override val startSourceSpan: TextRange?,
  override val children: List<TmplAstNode>,
) : TmplAstDirectiveContainer

internal class TmplAstReference(
  var parent: TmplAstDirectiveContainer? = null,
  override val name: String,
  override val keySpan: TextRange,
  override val value: String,
  override val valueSpan: TextRange?,
) : TmplAstExpressionSymbol

internal data class TmplAstBoundText(
  val value: JSExpression?,
) : TmplAstNode

internal data class TmplAstBoundAttribute(
  override val name: String,
  override val keySpan: TextRange?,
  val type: BindingType,
  val value: JSExpression?,
  override val sourceSpan: TextRange,
) : TmplAstAttribute

internal class TmplAstTextAttribute(
  override val name: String,
  override val keySpan: TextRange?,
  val value: String,
  val valueSpan: TextRange?,
  override val sourceSpan: TextRange,
) : TmplAstAttribute

internal class TmplAstContent(
  val children: List<TmplAstNode>
) : TmplAstNode

internal class TmplAstBoundEvent(
  override val name: String,
  override val keySpan: TextRange?,
  val type: ParsedEventType,
  val handler: List<JSElement>,
  val handlerSpan: TextRange,
  val target: String?,
  val phase: String?,
  override val sourceSpan: TextRange,
) : TmplAstAttribute

internal interface TmplAstForLoopBlock : TmplAstNode {

}

internal interface TmplAstDeferredBlock : TmplAstNode {

}

internal sealed interface TmplAstDeferredTrigger : TmplAstNode {

}

internal interface TmplAstHoverDeferredTrigger : TmplAstDeferredTrigger {

}

internal interface TmplAstInteractionDeferredTrigger : TmplAstDeferredTrigger {

}

internal interface TmplAstViewportDeferredTrigger : TmplAstDeferredTrigger {

}

internal interface TmplAstIfBlock : TmplAstNode {

}

internal interface TmplAstIfBlockBranch : TmplAstNode {

}

internal interface TmplAstSwitchBlock : TmplAstNode {

}

internal interface TmplAstSwitchBlockCase : TmplAstNode {

}

internal enum class BindingType {
  // A regular binding to a property (e.g. `[property]="expression"`).
  Property,

  // A binding to an element attribute (e.g. `[attr.name]="expression"`).
  Attribute,

  // A binding to a CSS class (e.g. `[class.name]="condition"`).
  Class,

  // A binding to a style rule (e.g. `[style.rule]="expression"`).
  Style,

  // A binding to an animation reference (e.g. `[animate.key]="expression"`).
  Animation,

  // Property side of a two-way binding (e.g. `[(property)]="expression"`).
  TwoWay,
}

internal enum class ParsedEventType {
  // DOM or Directive event
  Regular,

  // Animation specific event
  Animation,

  // Event side of a two-way binding (e.g. `[(property)]="expression"`).
  TwoWay,
}

internal class BoundTarget(component: Angular2Component) {

  private val psiVar2tmplAst: Map<PsiElement, TmplAstExpressionSymbol>

  val pipes: Map<String, Angular2Pipe> = component.declarationsInScope.filterIsInstance<Angular2Pipe>().associateBy { it.getName() }

  val templateFile: Angular2HtmlFile?

  val templateRoots: List<TmplAstNode>

  init {
    val psiVar2tmplAst = mutableMapOf<PsiElement, TmplAstExpressionSymbol>()
    templateFile = component.templateFile as? Angular2HtmlFile
    templateRoots = templateFile?.let { buildTmplAst(it, psiVar2tmplAst) }
                    ?: emptyList()
    this.psiVar2tmplAst = Collections.unmodifiableMap(psiVar2tmplAst)
  }

  /**
   * For a given template node (either an `Element` or a `Template`), get the set of directives
   * which matched the node, if any.
   */
  fun getDirectivesOfNode(node: `TmplAstElement|TmplAstTemplate`): Set<Angular2Directive> {
    return node.directives
  }

  /**
   * For a given `Reference`, get the reference's target - either an `Element`, a `Template`, or
   * a directive on a particular node.
   */
  fun getReferenceTarget(ref: TmplAstReference): `Angular2Directive|TmplAstElement|TmplAstTemplate`? {
    ref.value.takeIf { it.isNotBlank() }?.let { exportAs ->
      return ref.parent?.directives?.firstNotNullOfOrNull { it.exportAs[exportAs]?.directive }
    }
    return ref.parent
  }

  /**
   * For a given binding, get the entity to which the binding is being made.
   *
   * This will either be a directive or the node itself.
   */
  fun getConsumerOfBinding(
    binding: `TmplAstBoundAttribute|TmplAstBoundEvent|TmplAstTextAttribute`,
  ): `Angular2Directive|TmplAstElement|TmplAstTemplate`? {
    // TODO
    return null
  }

  /**
   * If the given `AST` expression refers to a `Reference` or `Variable` within the `Target`, then
   * return that.
   *
   * Otherwise, returns `null`.
   *
   * This is only defined for `AST` expressions that read or write to a property of an
   * `ImplicitReceiver`.
   */
  fun getExpressionTarget(expr: JSReferenceExpression): TmplAstExpressionSymbol? {
    if (expr.qualifier != null && expr.qualifier !is JSThisExpression) {
      return null
    }
    val referencedName = expr.referenceName ?: return null
    var result: ResolveResult? = null
    Angular2TemplateScopesResolver.getScopes(expr, listOf(Angular2TemplateElementsScopeProvider()))
      .firstOrNull {
        it.resolveAllScopesInHierarchy { resolveResult ->
          val element = resolveResult.element as? JSPsiElementBase
          if (resolveResult.isValidResult && element != null && referencedName == element.name) {
            result = resolveResult
            false
          }
          else true
        }
      }
    val element = result?.element ?: return null

    val owner = element.parentOfTypes(XmlAttribute::class, Angular2TemplateBinding::class, XmlTag::class, Angular2TemplateBindings::class)
    return psiVar2tmplAst[owner]
  }

  /**
   * Given a particular `Reference` or `Variable`, get the `ScopedNode` which created it.
   *
   * All `Variable`s are defined on node, so this will always return a value for a `Variable`
   * from the `Target`. Returns `null` otherwise.
   *//*
  getDefinitionNodeOfSymbol(symbol: Reference | Variable): ScopedNode | null;

  */
  /**
   * Get the nesting level of a particular `ScopedNode`.
   *
   * This starts at 1 for top-level nodes within the `Target` and increases for nodes
   * nested at deeper levels.
   *//*
  getNestingLevel(node: ScopedNode): number;

  */
  /**
   * Get all `Reference`s and `Variables` visible within the given `ScopedNode` (or at the top
   * level, if `null` is passed).
   *//*
  getEntitiesInScope(node: ScopedNode | null): ReadonlySet<Reference | Variable>;

  */
  /**
   * Get a list of all the directives used by the target,
   * including directives from `@defer` blocks.
   *//*
  getUsedDirectives(): DirectiveT[];

  */
  /**
   * Get a list of eagerly used directives from the target.
   * Note: this list *excludes* directives from `@defer` blocks.
   *//*
  getEagerlyUsedDirectives(): DirectiveT[];

  */
  /**
   * Get a list of all the pipes used by the target,
   * including pipes from `@defer` blocks.
   *//*
  getUsedPipes(): string[];

  */
  /**
   * Get a list of eagerly used pipes from the target.
   * Note: this list *excludes* pipes from `@defer` blocks.
   *//*
  getEagerlyUsedPipes(): string[];

  */
  /**
   * Get a list of all `@defer` blocks used by the target.
   *//*
  getDeferBlocks(): DeferredBlock[];

  */
  /**
   * Gets the element that a specific deferred block trigger is targeting.
   * @param block Block that the trigger belongs to.
   * @param trigger Trigger whose target is being looked up.
   *//*
  getDeferredTriggerTarget(block: DeferredBlock, trigger: DeferredTrigger): Element | null;

  */
  /**
   * Whether a given node is located in a `@defer` block.
   */
  fun isDeferred(node: TmplAstNode): Boolean {
    return false
  }
}

private fun buildTmplAst(
  file: Angular2HtmlFile,
  psiVar2tmplAst: MutableMap<PsiElement, TmplAstExpressionSymbol>
): List<TmplAstNode> {
  return file.document?.children?.mapNotNull { it.toTemplateAstNode(psiVar2tmplAst) }
         ?: emptyList()
}

private fun PsiElement.toTemplateAstNode(
  psiVar2tmplAst: MutableMap<PsiElement, TmplAstExpressionSymbol>
): TmplAstNode? =
  when (this) {
    is XmlTag -> toTmplAstDirectiveContainer(psiVar2tmplAst)
    is Angular2HtmlExpansionForm -> toTmplAstContent()
    is ASTWrapperPsiElement -> toTmplAstBoundText()
    else -> null
  }

private fun XmlTag.toTmplAstDirectiveContainer(
  psiVar2tmplAst: MutableMap<PsiElement, TmplAstExpressionSymbol>
): TmplAstDirectiveContainer {
  val attributesByKind = attributes.asSequence()
    .map { attribute ->
      val info = Angular2AttributeNameParser.parse(attribute.name, this)
      Pair(attribute, info)
    }
    .groupBy { it.second.type }

  val isTemplateTag = isTemplateTag(this)

  val templateBindingAttribute = attributesByKind[TEMPLATE_BINDINGS]?.firstOrNull()?.first
  val templateBindings = buildInfo(templateBindingAttribute, psiVar2tmplAst)

  val directives = attributes.asSequence()
    .flatMap { attr ->
      attr.descriptor.asSafely<Angular2AttributeDescriptor>()?.takeIf { it.info.type != TEMPLATE_BINDINGS }?.sourceDirectives
      ?: emptyList()
    }.plus(descriptor.asSafely<Angular2ElementDescriptor>()?.sourceDirectives ?: emptyList())
    .toSet()

  val inputs =
    ((attributesByKind[PROPERTY_BINDING]?.asSequence() ?: emptySequence())
      .plus(attributesByKind[BANANA_BOX_BINDING]?.asSequence() ?: emptySequence())
      .map { (attr, info) ->
        Pair(info.name, TmplAstBoundAttribute(
          name = info.name,
          keySpan = attr.nameElement.textRange,
          type = (info as Angular2AttributeNameParser.PropertyBindingInfo).tmplAstBindingType,
          value = Angular2Binding.get(attr)?.expression,
          sourceSpan = attr.textRange
        ))
      })
      .toMap()

  val outputs =
    (attributesByKind[EVENT]?.asSequence() ?: emptySequence())
      .associateBy({ it.second.name }, { (attr, info) ->
        TmplAstBoundEvent(
          info.name,
          attr.nameElement?.textRange,
          (info as Angular2AttributeNameParser.EventInfo).tmplParsedEventType,
          Angular2Action.get(attr)?.statements?.toList() ?: emptyList(),
          attr.valueTextRange,
          null,
          info.animationPhase?.name,
          attr.textRange
        )
      }) + (attributesByKind[BANANA_BOX_BINDING]?.asSequence() ?: emptySequence())
      .associateBy({ it.second.name }, { (attr, info) ->
        TmplAstBoundEvent(
          info.name + OUTPUT_CHANGE_SUFFIX,
          attr.nameElement?.textRange,
          ParsedEventType.TwoWay,
          listOfNotNull(Angular2Binding.get(attr)?.expression),
          attr.valueTextRange,
          null,
          null,
          attr.textRange
        )
      })

  val attributes = (attributesByKind[REGULAR]?.asSequence() ?: emptySequence())
    .associateBy({ it.second.name }, { (attr, info) ->
      TmplAstTextAttribute(
        name = info.name,
        keySpan = attr.nameElement.textRange,
        value = attr.value ?: "",
        valueSpan = attr.valueTextRange,
        sourceSpan = attr.textRange
      )
    })

  val references = (attributesByKind[REFERENCE]?.asSequence() ?: emptySequence())
    .associateBy({ it.second.name }, { (attr, info) ->
      TmplAstReference(
        name = info.name,
        keySpan = attr.nameElement.textRange,
        value = attr.value ?: "",
        valueSpan = attr.valueElement?.textRange,
      ).apply {
        psiVar2tmplAst[attr] = this
      }
    })

  val lets = (attributesByKind[LET]?.asSequence() ?: emptySequence())
    .associateBy({ it.second.name }, { (attr, info) ->
      TmplAstVariable(
        name = info.name,
        keySpan = attr.nameElement.textRange,
        value = attr.value ?: "",
        valueSpan = attr.valueTextRange,
      ).apply {
        psiVar2tmplAst[attr] = this
      }
    })

  val startSourceSpan = XmlTagUtil.getStartTagNameElement(this)?.textRange
                        ?: XmlTagUtil.getStartTagRange(this)
                        ?: textRange
  val children = children.asSequence().mapNotNull { it.toTemplateAstNode(psiVar2tmplAst) }
    .plus(attributesByKind[REGULAR]?.asSequence()
            ?.mapNotNull { it.first.valueElement }
            ?.flatMap { it.children.asSequence() }
            ?.mapNotNull { it.toTemplateAstNode(psiVar2tmplAst) }
          ?: emptySequence())
    .toList()

  return if (isTemplateTag) {
    TmplAstTemplate(
      tagName = null,
      templateAttrs = emptyList(),
      directives = directives,
      inputs = inputs,
      outputs = outputs,
      attributes = attributes,
      references = references,
      variables = lets,
      startSourceSpan = startSourceSpan,
      children = children
    ).apply {
      references.forEach { it.value.parent = this }
    }
  }
  else if (templateBindings != null) {
    TmplAstTemplate(
      tagName = name,
      templateAttrs = emptyList(),
      directives = templateBindings.directives,
      inputs = templateBindings.inputs,
      outputs = emptyMap(),
      attributes = emptyMap(),
      references = references,
      variables = templateBindings.variables,
      startSourceSpan = templateBindingAttribute?.nameElement?.textRange ?: startSourceSpan,
      children = listOf(
        TmplAstElement(
          name = name,
          directives = directives,
          inputs = inputs,
          outputs = outputs,
          attributes = attributes,
          references = emptyMap(),
          startSourceSpan = startSourceSpan,
          children = children
        )
      )
    ).apply {
      references.forEach { it.value.parent = this }
    }
  }
  else {
    TmplAstElement(
      name = name,
      directives = directives,
      inputs = inputs,
      outputs = outputs,
      attributes = attributes,
      references = references,
      startSourceSpan = startSourceSpan,
      children = children
    ).apply {
      references.forEach { it.value.parent = this }
    }
  }
}

private fun buildInfo(
  attribute: XmlAttribute?,
  psiVar2tmplAst: MutableMap<PsiElement, TmplAstExpressionSymbol>
): TemplateBindingsInfo? {
  if (attribute == null) return null
  val templateBindings = Angular2TemplateBindings.get(attribute).bindings
  return TemplateBindingsInfo(
    directives = attribute.descriptor.asSafely<Angular2AttributeDescriptor>()?.sourceDirectives?.toSet() ?: emptySet(),
    inputs = templateBindings.asSequence()
      .filter { !it.keyIsVar() }
      .map { Pair(it.key, TmplAstBoundAttribute(it.key, it.keyElement?.textRange, BindingType.Property, it.expression, it.textRange)) }
      .toMap(),
    variables = templateBindings.asSequence()
      .filter { it.keyIsVar() }
      .map {
        Pair(it.key, TmplAstVariable(it.key, it.name, it.variableDefinition?.textRange, null).apply {
          psiVar2tmplAst[it] = this
        })
      }
      .toMap(),
  )
}

private data class TemplateBindingsInfo(
  val directives: Set<Angular2Directive>,
  val inputs: Map<String, TmplAstBoundAttribute>,
  val variables: Map<String, TmplAstVariable>,
)

private val Angular2AttributeNameParser.PropertyBindingInfo.tmplAstBindingType: BindingType
  get() =
    if (this.type == BANANA_BOX_BINDING)
      BindingType.TwoWay
    else when (this.bindingType) {
      PropertyBindingType.PROPERTY -> BindingType.Property
      PropertyBindingType.ANIMATION -> BindingType.Animation
      PropertyBindingType.ATTRIBUTE -> BindingType.Attribute
      PropertyBindingType.CLASS -> BindingType.Class
      PropertyBindingType.STYLE -> BindingType.Style
    }

private val Angular2AttributeNameParser.EventInfo.tmplParsedEventType: ParsedEventType
  get() =
    when (this.eventType) {
      Angular2HtmlEvent.EventType.REGULAR -> ParsedEventType.Regular
      Angular2HtmlEvent.EventType.ANIMATION -> ParsedEventType.Animation
    }


private fun Angular2HtmlExpansionForm.toTmplAstContent(): TmplAstContent =
  // Let's simplify stuff a little bit and don't create a real ICU node
  TmplAstContent(
    childrenOfType<ASTWrapperPsiElement>()
      .asSequence().plus(
        childrenOfType<Angular2HtmlExpansionFormCase>()
          .asSequence()
          .flatMap { it.childrenOfType<XmlASTWrapperPsiElement>() }
          .flatMap { it.childrenOfType<ASTWrapperPsiElement>() }
      )
      .flatMap { it.children.asSequence() }
      .mapNotNull {
        when (it) {
          is Angular2Binding -> TmplAstBoundText(it.expression)
          is Angular2Interpolation -> TmplAstBoundText(it.expression)
          else -> null
        }
      }
      .toList()
  )

private fun ASTWrapperPsiElement.toTmplAstBoundText(): TmplAstBoundText? =
  children.asSequence().firstNotNullOfOrNull { it as? Angular2Interpolation }
    ?.let { TmplAstBoundText(it.expression) }