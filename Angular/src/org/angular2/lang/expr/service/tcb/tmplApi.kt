package org.angular2.lang.expr.service.tcb

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.javascript.web.html.XmlASTWrapperPsiElement
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfTypes
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList
import com.intellij.util.applyIf
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolDelegate
import com.intellij.xml.util.XmlTagUtil
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2InjectionUtils
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.codeInsight.blocks.*
import org.angular2.codeInsight.template.Angular2TemplateScope
import org.angular2.codeInsight.template.getTemplateElementsScopeFor
import org.angular2.codeInsight.template.isTemplateTag
import org.angular2.entities.*
import org.angular2.lang.Angular2LangUtil.OUTPUT_CHANGE_SUFFIX
import org.angular2.lang.expr.psi.*
import org.angular2.lang.expr.psi.impl.Angular2BlockParameterVariableImpl
import org.angular2.lang.html.Angular2HtmlFile
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType.*
import org.angular2.lang.html.psi.*
import org.angular2.lang.types.Angular2TypeUtils.possiblyGenericJsType
import java.util.*

internal sealed interface TmplAstNode

internal sealed interface TmplAstNodeWithChildren : TmplAstNode {
  val children: List<TmplAstNode>
}

internal sealed interface TemplateEntity : TmplAstNode

internal sealed interface TmplAstExpressionSymbol : TemplateEntity {
  val name: String
  val keySpan: TextRange?
  val value: String?
  val valueSpan: TextRange?
}

internal sealed interface TmplAstDirectiveContainer : TmplAstNodeWithChildren {
  val tag: XmlTag?
  val directives: Set<TmplDirectiveMetadata>
  val inputs: Map<String, TmplAstBoundAttribute>
  val outputs: Map<String, TmplAstBoundEvent>
  val attributes: Map<String, TmplAstTextAttribute>
  val references: Map<String, TmplAstReference>
  val startSourceSpan: TextRange?
  override val children: List<TmplAstNode>
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

internal class TmplAstLetDeclaration(
  val name: String,
  val nameSpan: TextRange?,
  val value: JSExpression?,
  val sourceSpan: TextRange,
) : TemplateEntity

internal class TmplAstElement(
  val name: String,
  override val tag: XmlTag?,
  override val directives: Set<TmplDirectiveMetadata>,
  override val inputs: Map<String, TmplAstBoundAttribute>,
  override val outputs: Map<String, TmplAstBoundEvent>,
  override val attributes: Map<String, TmplAstTextAttribute>,
  override val references: Map<String, TmplAstReference>,
  override val startSourceSpan: TextRange?,
  override val children: List<TmplAstNode>,
) : TmplAstDirectiveContainer

internal class TmplAstTemplate(
  val tagName: String?,
  override val tag: XmlTag,
  val templateAttrs: List<TmplAstAttribute>,
  override val directives: Set<TmplDirectiveMetadata>,
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
  val valueMappingOffset: Int,
  override val sourceSpan: TextRange,
  val isStructuralDirective: Boolean = false,
) : TmplAstAttribute

internal class TmplAstTextAttribute(
  override val name: String,
  override val keySpan: TextRange?,
  val value: String,
  override val sourceSpan: TextRange,
) : TmplAstAttribute

internal class TmplAstContent(
  override val children: List<TmplAstNode>,
) : TmplAstNodeWithChildren

internal class TmplAstBoundEvent(
  override val name: String,
  override val keySpan: TextRange?,
  val type: ParsedEventType,
  val handler: List<JSElement>,
  val handlerMappingOffset: Int,
  val target: String?,
  val phase: String?,
  override val sourceSpan: TextRange,
  val fromHostBinding: Boolean = false,
) : TmplAstAttribute

internal interface TmplAstBlockNode : TmplAstNode {
  val nameSpan: TextRange?
}

internal interface TmplAstBlockNodeWithChildren : TmplAstBlockNode, TmplAstNodeWithChildren {
}

internal class TmplAstForLoopBlock(
  override val nameSpan: TextRange?,
  val item: TmplAstVariable?,
  val contextVariables: Map<String, TmplAstVariable>,
  val empty: TmplAstForLoopBlockEmpty?,
  val expression: JSExpression?,
  val trackBy: JSExpression?,
  override val children: List<TmplAstNode>,
) : TmplAstBlockNodeWithChildren

internal class TmplAstForLoopBlockEmpty(
  override val nameSpan: TextRange?,
  override val children: List<TmplAstNode>,
) : TmplAstBlockNodeWithChildren

internal class TmplAstDeferredBlock(
  override val nameSpan: TextRange?,
  val triggers: TmplAstDeferredBlockTriggers,
  val prefetchTriggers: TmplAstDeferredBlockTriggers,
  val hydrateTriggers: TmplAstDeferredBlockTriggers,
  val error: TmplAstDeferredBlockError?,
  val loading: TmplAstDeferredBlockLoading?,
  val placeholder: TmplAstDeferredBlockPlaceholder?,
  override val children: List<TmplAstNode>,
) : TmplAstBlockNodeWithChildren

internal class TmplAstDeferredBlockError(
  override val nameSpan: TextRange?,
  override val children: List<TmplAstNode>,
) : TmplAstBlockNodeWithChildren

internal class TmplAstDeferredBlockLoading(
  override val nameSpan: TextRange?,
  override val children: List<TmplAstNode>,
) : TmplAstBlockNodeWithChildren

internal class TmplAstDeferredBlockPlaceholder(
  override val nameSpan: TextRange?,
  override val children: List<TmplAstNode>,
) : TmplAstBlockNodeWithChildren

internal sealed interface TmplAstDeferredTrigger : TmplAstBlockNode

internal class TmplAstBoundDeferredTrigger(
  override val nameSpan: TextRange?,
  val value: JSExpression?,
) : TmplAstDeferredTrigger

internal class TmplAstDeferredBlockTriggers(
  val `when`: TmplAstBoundDeferredTrigger?,
  // Most triggers do not produce any code in TCB - ignore them
  //val hover: TmplAstHoverDeferredTrigger?,
  //val interaction: TmplAstInteractionDeferredTrigger?,
  //val viewport: TmplAstViewportDeferredTrigger?,
  //val idle: TmplAstIdleDeferredTrigger?,
  //val immediate: TmplAstImmediateDeferredTrigger?,
  //val timer: TmplAstTimerDeferredTrigger?,
)

internal class TmplAstIfBlock(
  override val nameSpan: TextRange?,
  val branches: List<TmplAstIfBlockBranch>,
) : TmplAstBlockNode

internal class TmplAstIfBlockBranch(
  override val nameSpan: TextRange?,
  val expression: JSExpression?,
  val expressionAlias: TmplAstVariable?,
  override val children: List<TmplAstNode>,
) : TmplAstBlockNodeWithChildren

internal class TmplAstSwitchBlock(
  override val nameSpan: TextRange?,
  val expression: JSExpression?,
  val cases: List<TmplAstSwitchBlockCase>,
) : TmplAstBlockNode

internal class TmplAstSwitchBlockCase(
  override val nameSpan: TextRange?,
  val expression: JSExpression?,
  override val children: List<TmplAstNode>,
) : TmplAstBlockNodeWithChildren

internal class TmplAstLetBlock(
  override val nameSpan: TextRange?,
  val declaration: TmplAstLetDeclaration?,
) : TmplAstBlockNode

internal class TmplDirectiveMetadata(
  val directive: Angular2Directive,
  val isHostDirective: Boolean,
  val inputs: Map<String, Angular2DirectiveProperty>,
  val outputs: Map<String, Angular2DirectiveProperty>,
  val exportAs: Set<String>,
) {

  val typeScriptClass: TypeScriptClass?
    get() =
      (directive as? Angular2ClassBasedDirective)?.typeScriptClass

  val entityJsType: JSType?
    get() =
      typeScriptClass?.possiblyGenericJsType ?: directive.entityJsType

  val isGeneric: Boolean
    get() =
      typeScriptClass?.typeParameters?.isNotEmpty() == true

  val templateGuards: List<Angular2TemplateGuard>
    get() =
      directive.templateGuards

  val hasTemplateContextGuard: Boolean
    get() =
      directive.hasTemplateContextGuard

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

internal class BoundTarget(component: Angular2Component?) {

  private val referenceMap: Map<Any?, TemplateEntity>

  val pipes: Map<String, Angular2Pipe> = component
                                           ?.let { Angular2DeclarationsScope(it) }
                                           ?.importsOwner
                                           ?.declarationsInScope?.filterIsInstance<Angular2Pipe>()?.associateBy { it.getName() }
                                         ?: emptyMap()

  val templateFile: Angular2HtmlFile?

  val templateRoots: List<TmplAstNode>

  init {
    val referenceMap = mutableMapOf<Any, TemplateEntity>()
    templateFile = component?.templateFile as? Angular2HtmlFile
    templateRoots = templateFile?.let {
      buildTmplAst(it, object : ReferenceResolver {
        override fun set(element: PsiElement, localSymbol: TemplateEntity) {
          referenceMap[element] = localSymbol
        }


        override fun set(implicitSymbol: WebSymbol, source: PsiElement, localSymbol: TemplateEntity) {
          referenceMap[ImplicitSymbolWithSource(implicitSymbol, source)] = localSymbol
        }

      })
    }
                    ?: emptyList()
    this.referenceMap = Collections.unmodifiableMap(referenceMap)
  }

  /**
   * For a given template node (either an `Element` or a `Template`), get the set of directives
   * which matched the node, if any.
   */
  fun getDirectivesOfNode(node: `TmplAstElement|TmplAstTemplate`): Set<TmplDirectiveMetadata> {
    return node.directives
  }

  /**
   * For a given `Reference`, get the reference's target - either an `Element`, a `Template`, or
   * a directive on a particular node.
   */
  fun getReferenceTarget(ref: TmplAstReference): `TmplDirectiveMetadata|TmplAstElement|TmplAstTemplate`? {
    val exportAs = ref.value.takeIf { it.isNotBlank() }
    return if (exportAs != null) {
      ref.parent?.directives?.find { it.exportAs.contains(exportAs) }
    }
    else {
      ref.parent?.directives?.find { it.directive.isComponent }
      ?: ref.parent
    }
  }

  /**
   * For a given binding, get the entity to which the binding is being made.
   *
   * This will either be a directive or the node itself.
   */
  fun getConsumerOfBinding(
    binding: `TmplAstBoundAttribute|TmplAstBoundEvent|TmplAstTextAttribute`,
  ): `TmplDirectiveMetadata|TmplAstElement|TmplAstTemplate`? {
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
  fun getExpressionTarget(expr: JSReferenceExpression): TemplateEntity? {
    if (expr.qualifier != null && expr.qualifier !is JSThisExpression) {
      return null
    }
    val referencedName = expr.referenceName ?: return null
    var result: Any? = null
    var scope: Angular2TemplateScope? = getTemplateElementsScopeFor(expr)
    while (scope != null && result == null) {
      scope.resolve { resolveResult ->
        val element = resolveResult.element as? JSPsiElementBase
        if (result == null
            && resolveResult.isValidResult
            && element != null
            && referencedName == element.name
            && (expr.qualifier == null || !isLetDeclarationVariable(element))) {
          result = resolveResult
        }
      }
      for (symbol in scope.symbols) {
        if (result == null && symbol.name == referencedName) {
          result = ImplicitSymbolWithSource((symbol as? WebSymbolDelegate<*>)?.delegate ?: symbol, scope.source)
        }
      }
      scope = scope.parent
    }

    return when (result) {
      is ResolveResult -> {
        val element = (result as ResolveResult).element ?: return null
        val owner = element.parentOfTypes(
          XmlAttribute::class,
          Angular2TemplateBinding::class,
          XmlTag::class,
          Angular2TemplateBindings::class,
          Angular2BlockParameterVariableImpl::class,
          withSelf = true
        )
        referenceMap[owner]
      }
      is ImplicitSymbolWithSource -> referenceMap[result as ImplicitSymbolWithSource]
      else -> null
    }
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
   */
  fun getEagerlyUsedPipes(): Set<String> {
    return emptySet()
  }

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
  fun getDeferredTriggerTarget(block: TmplAstDeferredBlock, trigger: `TmplAstHoverDeferredTrigger|TmplAstInteractionDeferredTrigger|TmplAstViewportDeferredTrigger`): TmplAstElement? {
    return null
  }
  */

  /**
   * Whether a given node is located in a `@defer` block.
   */
  fun isDeferred(node: TmplAstNode): Boolean {
    return false
  }
}

private data class ImplicitSymbolWithSource(
  val symbol: WebSymbol,
  val source: PsiElement?,
)

private interface ReferenceResolver {
  operator fun set(element: PsiElement, localSymbol: TemplateEntity)
  operator fun set(implicitSymbol: WebSymbol, source: PsiElement, localSymbol: TemplateEntity)
}

internal fun buildHostBindingsAst(
  cls: TypeScriptClass,
): Pair<TmplAstNode, List<TextRange>>? {
  val decorator = Angular2DecoratorUtil.findDecorator(cls, true, Angular2DecoratorUtil.COMPONENT_DEC, Angular2DecoratorUtil.DIRECTIVE_DEC)
  val hostBindings = Angular2DecoratorUtil.getProperty(decorator, Angular2DecoratorUtil.HOST_PROP)?.value?.asSafely<JSObjectLiteralExpression>()
                     ?: return null

  val directive = Angular2EntitiesProvider.getDirective(decorator)
  val elementName = directive?.selector?.simpleSelectors
                      ?.map { it.elementName?.trim()?.takeIf { it.isNotEmpty() && it != "*" } }?.distinct()
                      ?.takeIf { it.size == 1 }?.firstOrNull()
                    ?: "div"
  val inlineCodeRanges = mutableListOf<TextRange>()
  val inputs = mutableMapOf<String, TmplAstBoundAttribute>()
  val outputs = mutableMapOf<String, TmplAstBoundEvent>()
  val attributes = mutableMapOf<String, TmplAstTextAttribute>()
  for (property in hostBindings.properties) {
    val attributeName = property.name?.let { Angular2AttributeNameParser.parse(it) } ?: continue
    val quotedLiteral = property.value.asSafely<JSLiteralExpression>()?.takeIf { it.isQuotedLiteral }
                        ?: continue
    val valueTextRange = quotedLiteral.textRange.let { TextRange(it.startOffset + 1, it.endOffset - 1) }
    when (attributeName.type) {
      REGULAR -> {
        attributes[attributeName.name] =
          TmplAstTextAttribute(attributeName.name, property.nameIdentifier?.textRange, quotedLiteral.stringValue ?: continue,
                               property.textRange)
      }
      EVENT -> {
        inlineCodeRanges.add(valueTextRange)
        outputs[attributeName.name] =
          TmplAstBoundEvent(attributeName.name, property.nameIdentifier?.textRange,
                            (attributeName as Angular2AttributeNameParser.EventInfo).tmplParsedEventType,
                            Angular2InjectionUtils.findInjectedAngularExpression(quotedLiteral, Angular2Action::class.java)?.statements?.toList()
                            ?: emptyList(), valueTextRange.startOffset, null, null, property.textRange, true)
      }
      PROPERTY_BINDING -> {
        inlineCodeRanges.add(valueTextRange)
        inputs[attributeName.name] =
          TmplAstBoundAttribute(attributeName.name, property.nameIdentifier?.textRange,
                                (attributeName as Angular2AttributeNameParser.PropertyBindingInfo).tmplAstBindingType,
                                Angular2InjectionUtils.findInjectedAngularExpression(quotedLiteral, Angular2SimpleBinding::class.java)?.expression,
                                valueTextRange.startOffset, valueTextRange, false)
      }
      else -> {}
    }
  }
  return if (inputs.isNotEmpty() || outputs.isNotEmpty() || attributes.isNotEmpty())
    Pair(TmplAstElement(elementName, null, emptySet(), inputs, outputs, attributes, emptyMap(), null, emptyList()), inlineCodeRanges)
  else
    null
}

private fun buildTmplAst(
  file: Angular2HtmlFile,
  referenceResolver: ReferenceResolver,
): List<TmplAstNode> {
  return file.document?.children?.mapNotNull { it.toTemplateAstNode(referenceResolver) }
         ?: emptyList()
}

private fun PsiElement.toTemplateAstNode(
  referenceResolver: ReferenceResolver,
): TmplAstNode? =
  when (this) {
    is XmlTag -> toTmplAstDirectiveContainer(referenceResolver)
    is Angular2HtmlExpansionForm -> toTmplAstContent()
    is ASTWrapperPsiElement -> toTmplAstBoundText()
    is Angular2HtmlBlock -> if (this.isPrimary) toTmplAstBlock(referenceResolver) else null
    else -> null
  }

private fun XmlTag.toTmplAstDirectiveContainer(
  referenceResolver: ReferenceResolver,
): TmplAstDirectiveContainer {
  val attributesByKind = attributes.asSequence()
    .map { attribute ->
      val info = Angular2AttributeNameParser.parse(attribute.name, this)
      Pair(attribute, info)
    }
    .groupBy { it.second.type }

  val isTemplateTag = isTemplateTag(this)
  val scope = Angular2DeclarationsScope(this)

  val templateBindingAttribute = attributesByKind[TEMPLATE_BINDINGS]?.firstOrNull()?.first
  val templateBindings = buildInfo(scope, templateBindingAttribute, referenceResolver)

  val directives = Angular2ApplicableDirectivesProvider(this, scope = scope)
    .matched
    .asSequence()
    .flatMap { directive -> buildMetadata(directive) }
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
          valueMappingOffset = 0,
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
          0,
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
          0,
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
        sourceSpan = attr.textRange,
      )
    })

  val references = (attributesByKind[REFERENCE]?.asSequence() ?: emptySequence())
    .associateBy({ it.second.name }, { (attr, info) ->
      TmplAstReference(
        name = info.name,
        keySpan = attr.nameElement.textRange.startOffset.let { TextRange(it + info.nameOffset, it + info.nameOffset + info.name.length) },
        value = attr.value ?: "",
        valueSpan = attr.valueElement?.textRange,
      ).apply {
        referenceResolver.set(attr, this)
      }
    })

  val lets = (attributesByKind[LET]?.asSequence() ?: emptySequence())
    .associateBy({ it.second.name }, { (attr, info) ->
      TmplAstVariable(
        name = info.name,
        keySpan = attr.nameElement.textRange.startOffset.let { TextRange(it + info.nameOffset, it + info.nameOffset + info.name.length) },
        value = attr.value ?: "",
        valueSpan = attr.valueTextRange.takeIf { it.length > 0 },
      ).apply {
        referenceResolver.set(attr, this)
      }
    })

  val startSourceSpan = XmlTagUtil.getStartTagNameElement(this)?.textRange
                        ?: XmlTagUtil.getStartTagRange(this)
                        ?: textRange
  val children = children.asSequence().mapNotNull { it.toTemplateAstNode(referenceResolver) }
    .plus(attributesByKind[REGULAR]?.asSequence()
            ?.mapNotNull { it.first.valueElement }
            ?.flatMap { it.children.asSequence() }
            ?.mapNotNull { it.toTemplateAstNode(referenceResolver) }
          ?: emptySequence())
    .toList()

  return if (isTemplateTag) {
    TmplAstTemplate(
      tagName = null,
      tag = this,
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
      tag = this,
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
          tag = null,
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
      tag = this,
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

internal fun buildMetadata(directive: Angular2Directive): List<TmplDirectiveMetadata> {
  val result = SmartList<TmplDirectiveMetadata>()
  val exportAs = directive.exportAs.entries.groupBy({ it.value.directive }, { it.key })
  val takenInputs = mutableSetOf<String>()
  val takenOutputs = mutableSetOf<String>()
  result.add(directive.toDirectiveMetadata(exportAs[directive], takenInputs, takenOutputs))
  for (hostDirective in directive.hostDirectives) {
    result.add(hostDirective.toDirectiveMetadata(exportAs[hostDirective.directive], takenInputs, takenOutputs))
  }
  return result
}

private fun Angular2HostDirective.toDirectiveMetadata(
  exportAs: List<String>?,
  takenInputs: MutableSet<String>,
  takenOutputs: MutableSet<String>,
): TmplDirectiveMetadata? {
  return TmplDirectiveMetadata(
    directive = directive ?: return null,
    isHostDirective = true,
    inputs = inputs.asSequence().filter { takenInputs.add(it.name) }.associateBy { it.name },
    outputs = outputs.asSequence().filter { takenOutputs.add(it.name) }.associateBy { it.name },
    exportAs = exportAs?.toSet() ?: emptySet()
  )
}

private fun Angular2Directive.toDirectiveMetadata(
  exportAs: Collection<String>?,
  takenInputs: MutableSet<String>,
  takenOutputs: MutableSet<String>,
) = TmplDirectiveMetadata(
  directive = this,
  isHostDirective = false,
  inputs = bindings.inputs.asSequence().filter { takenInputs.add(it.name) }.associateBy { it.name },
  outputs = bindings.outputs.asSequence().filter { takenOutputs.add(it.name) }.associateBy { it.name },
  exportAs = exportAs?.toSet() ?: emptySet()
)

private fun buildInfo(
  scope: Angular2DeclarationsScope,
  attribute: XmlAttribute?,
  referenceResolver: ReferenceResolver,
): TemplateBindingsInfo? {
  if (attribute == null) return null
  val template = Angular2TemplateBindings.get(attribute)
  val templateBindings = Angular2TemplateBindings.get(attribute).bindings
  val templateName = attribute.name.removePrefix("*")
  val hasDefaultBinding = templateBindings.any { !it.keyIsVar() && it.key == templateName }
  val attributeNameRange = attribute.nameElement?.textRange ?: return null
  return TemplateBindingsInfo(
    directives = Angular2ApplicableDirectivesProvider(template, scope = scope)
      .matched
      .asSequence()
      .flatMap { buildMetadata(it) }
      .toSet(),
    inputs = templateBindings.asSequence()
      .filter { !it.keyIsVar() }
      .map {
        Pair(it.key, TmplAstBoundAttribute(it.key, it.keyElement?.textRange ?: attributeNameRange,
                                           BindingType.Property, it.expression, 0, it.textRange, true))
      }
      .applyIf(!hasDefaultBinding) {
        this + Pair(templateName, TmplAstBoundAttribute(templateName, attributeNameRange, BindingType.Property,
                                                        null, 0, attributeNameRange, true))
      }
      .toMap(),
    variables = templateBindings.asSequence()
      .filter { it.keyIsVar() }
      .map {
        Pair(it.key, TmplAstVariable(it.key, it.name, it.variableDefinition?.textRange,
                                     it.keyElement?.textRange ?: it.variableDefinition?.textRange).apply {
          referenceResolver[it] = this
        })
      }
      .toMap(),
  )
}

private data class TemplateBindingsInfo(
  val directives: Set<TmplDirectiveMetadata>,
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

private fun PsiElement?.mapChildren(referenceResolver: ReferenceResolver): List<TmplAstNode> =
  this?.children?.mapNotNull { it.toTemplateAstNode(referenceResolver) } ?: emptyList()

private fun Angular2HtmlBlock.toTmplAstBlock(referenceResolver: ReferenceResolver): TmplAstBlockNode? =
  when (name) {
    BLOCK_IF -> TmplAstIfBlock(
      nameSpan = nameElement.textRange,
      branches = (sequenceOf(TmplAstIfBlockBranch(
        nameSpan = nameElement.textRange,
        expression = parameters.firstOrNull()?.expression,
        expressionAlias = parameters.getOrNull(1)?.variables?.firstOrNull()?.toTmplAstVariable(referenceResolver),
        children = contents.mapChildren(referenceResolver)
      )) + blockSiblingsForward().mapNotNull { it.toTmplAstBlock(referenceResolver) as? TmplAstIfBlockBranch }).toList()
    )
    BLOCK_ELSE_IF -> TmplAstIfBlockBranch(
      nameSpan = nameElement.textRange,
      expression = parameters.firstOrNull()?.expression,
      expressionAlias = null,
      children = contents.mapChildren(referenceResolver)
    )
    BLOCK_ELSE -> TmplAstIfBlockBranch(
      nameSpan = nameElement.textRange,
      expression = null,
      expressionAlias = null,
      children = contents.mapChildren(referenceResolver)
    )
    BLOCK_FOR -> TmplAstForLoopBlock(
      nameSpan = nameElement.textRange,
      item = parameters.firstOrNull()?.variables?.firstOrNull()?.toTmplAstVariable(referenceResolver),
      expression = parameters.getOrNull(0)?.expression,
      trackBy = parameters.find { it.name == PARAMETER_TRACK }?.expression,
      empty = blockSiblingsForward().find { it.name == BLOCK_EMPTY }?.toTmplAstBlock(referenceResolver) as? TmplAstForLoopBlockEmpty,
      contextVariables = buildContextVariables(this, referenceResolver),
      children = contents.mapChildren(referenceResolver),
    )
    BLOCK_EMPTY -> TmplAstForLoopBlockEmpty(
      nameSpan = nameElement.textRange,
      children = contents.mapChildren(referenceResolver),
    )
    BLOCK_SWITCH -> TmplAstSwitchBlock(
      nameSpan = nameElement.textRange,
      expression = parameters.getOrNull(0)?.expression,
      cases = contents?.children?.mapNotNull { (it as? Angular2HtmlBlock)?.toTmplAstBlock(referenceResolver) as? TmplAstSwitchBlockCase }
              ?: emptyList()
    )
    BLOCK_CASE -> TmplAstSwitchBlockCase(
      nameSpan = nameElement.textRange,
      expression = parameters.getOrNull(0)?.expression,
      children = contents.mapChildren(referenceResolver),
    )
    BLOCK_DEFAULT -> TmplAstSwitchBlockCase(
      nameSpan = nameElement.textRange,
      expression = null,
      children = contents.mapChildren(referenceResolver),
    )
    BLOCK_DEFER -> TmplAstDeferredBlock(
      nameSpan = nameElement.textRange,
      triggers = this.buildTriggers(null),
      prefetchTriggers = this.buildTriggers(PARAMETER_PREFIX_PREFETCH),
      hydrateTriggers = this.buildTriggers(PARAMETER_PREFIX_HYDRATE),
      error = blockSiblingsForward().find { it.name == BLOCK_ERROR }?.toTmplAstBlock(referenceResolver) as? TmplAstDeferredBlockError,
      loading = blockSiblingsForward().find { it.name == BLOCK_LOADING }?.toTmplAstBlock(referenceResolver) as? TmplAstDeferredBlockLoading,
      placeholder = blockSiblingsForward().find { it.name == BLOCK_PLACEHOLDER }?.toTmplAstBlock(referenceResolver) as? TmplAstDeferredBlockPlaceholder,
      children = contents.mapChildren(referenceResolver),
    )
    BLOCK_ERROR -> TmplAstDeferredBlockError(
      nameSpan = nameElement.textRange,
      children = contents.mapChildren(referenceResolver),
    )
    BLOCK_LOADING -> TmplAstDeferredBlockLoading(
      nameSpan = nameElement.textRange,
      children = contents.mapChildren(referenceResolver),
    )
    BLOCK_PLACEHOLDER -> TmplAstDeferredBlockPlaceholder(
      nameSpan = nameElement.textRange,
      children = contents.mapChildren(referenceResolver),
    )
    BLOCK_LET -> TmplAstLetBlock(
      nameSpan = nameElement.textRange,
      declaration = parameters.getOrNull(0)
        ?.variables?.firstOrNull()?.toTmplAstLetDeclaration(referenceResolver)
    )
    else -> null
  }

private fun Angular2HtmlBlock.buildTriggers(prefix: String?): TmplAstDeferredBlockTriggers =
  TmplAstDeferredBlockTriggers(
    `when` = parameters.find { it.name == PARAMETER_WHEN && it.prefix == prefix }?.let {
      TmplAstBoundDeferredTrigger(
        nameSpan = it.nameElement?.textRange,
        value = it.expression
      )
    }
  )

private fun hasPrefetch(parameter: Angular2BlockParameter) =
  parameter.prefix == PARAMETER_PREFIX_PREFETCH

private fun JSVariable.toTmplAstVariable(referenceResolver: ReferenceResolver): TmplAstVariable =
  TmplAstVariable(
    name = name!!,
    keySpan = node.firstChildNode.takeIf { it.elementType == JSTokenTypes.IDENTIFIER }?.textRange
              ?: textRange,
    valueSpan = null,
    value = null
  ).apply {
    referenceResolver.set(this@toTmplAstVariable, this)
  }

private fun WebSymbol.toTmplAstVariable(block: Angular2HtmlBlock, referenceResolver: ReferenceResolver): TmplAstVariable =
  TmplAstVariable(
    name = name,
    keySpan = null,
    valueSpan = null,
    value = null
  ).apply {
    referenceResolver.set(this@toTmplAstVariable, block, this)
  }

private fun JSVariable.toTmplAstLetDeclaration(referenceResolver: ReferenceResolver): TmplAstLetDeclaration =
  TmplAstLetDeclaration(
    name = name!!,
    nameSpan = nameIdentifier?.textRange,
    value = initializer,
    sourceSpan = textRange
  ).apply {
    referenceResolver.set(this@toTmplAstLetDeclaration, this)
  }

private fun buildContextVariables(forOfBlock: Angular2HtmlBlock, referenceResolver: ReferenceResolver): Map<String, TmplAstVariable> {
  val result = mutableMapOf<String, TmplAstVariable>()
  forOfBlock.parameters
    .asSequence()
    .filter { !it.isPrimaryExpression && it.name == PARAMETER_LET }
    .flatMap { it.variables }
    .mapNotNull { v -> v.initializer?.text?.trim()?.let { Pair(it, v.toTmplAstVariable(referenceResolver)) } }
    .toMap(result)

  val symbols = forOfBlock.definition?.implicitVariables?.associateBy { it.name }
                ?: emptyMap()
  Scope.forLoopContextVariableTypes.keys.filter { !result.containsKey(it) }.associateByTo(
    result, { it }, {
    (symbols[it] ?: throw IllegalStateException("Cannot find symbol for $it")).toTmplAstVariable(forOfBlock, referenceResolver)
  }
  )
  return result
}