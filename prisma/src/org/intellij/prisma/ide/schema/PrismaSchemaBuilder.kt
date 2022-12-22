package org.intellij.prisma.ide.schema

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ProcessingContext
import org.intellij.prisma.ide.schema.types.PrismaDatasourceType

class PrismaCompoundSchema(private val groups: List<PrismaSchemaElementGroup>,
                           private val factories: List<PrismaSchemaDynamicFactory>) {
  fun evaluate(evaluationContext: PrismaSchemaEvaluationContext): PrismaEvaluatedSchema {
    val newGroups = mutableMapOf<PrismaSchemaKind, PrismaSchemaElementGroup.Builder>()

    factories.asSequence()
      .map { it.invoke(evaluationContext) }
      .plus(groups)
      .forEach { group ->
        newGroups
          .computeIfAbsent(group.kind) { PrismaSchemaElementGroup.Builder(group.kind) }
          .compose(group)
      }

    return PrismaEvaluatedSchema(newGroups.mapValues { it.value.build() })
  }

  class Builder : SchemaDslBuilder<PrismaCompoundSchema> {
    private val groups: MutableList<PrismaSchemaElementGroup> = mutableListOf()
    private val factories: MutableList<PrismaSchemaDynamicFactory> = mutableListOf()

    fun group(kind: PrismaSchemaKind, block: PrismaSchemaElementGroup.Builder.() -> Unit) {
      val groupBuilder = PrismaSchemaElementGroup.Builder(kind)
      groupBuilder.block()
      val group = groupBuilder.build()
      groups.add(group)
    }

    fun dynamic(kind: PrismaSchemaKind, block: PrismaSchemaElementGroup.Builder.(PrismaSchemaEvaluationContext) -> Unit) {
      factories.add { ctx ->
        val builder = PrismaSchemaElementGroup.Builder(kind)
        builder.block(ctx)
        builder.build()
      }
    }

    fun compose(schema: PrismaCompoundSchema) {
      groups.addAll(schema.groups)
      factories.addAll(schema.factories)
    }

    override fun build(): PrismaCompoundSchema {
      return PrismaCompoundSchema(groups, factories)
    }
  }
}

class PrismaEvaluatedSchema(private val groups: Map<PrismaSchemaKind, PrismaSchemaElementGroup>) {
  fun getElementsByKind(kind: PrismaSchemaKind): Collection<PrismaSchemaDeclaration> {
    return groups[kind]?.elements?.values ?: emptyList()
  }

  fun getElement(kind: PrismaSchemaKind, label: String?): PrismaSchemaDeclaration? {
    return groups[kind]?.elements?.get(label)
  }

  fun expandRefs(elements: List<PrismaSchemaElement>): List<PrismaSchemaElement> {
    return elements.mapNotNull {
      if (it.ref != null) {
        this.getElement(it.ref.kind, it.ref.label)
      }
      else {
        it
      }
    }
  }

  fun match(element: PsiElement?): PrismaSchemaElement? {
    if (element is PrismaSchemaFakeElement) {
      return element.schemaElement
    }

    val context = PrismaSchemaContext.forElement(element) ?: return null
    return match(context)
  }

  private fun match(context: PrismaSchemaContext): PrismaSchemaElement? {
    return when (context) {
      is PrismaSchemaDeclarationContext -> {
        getElement(context.kind, context.label)
      }

      is PrismaSchemaParameterContext -> {
        val declaration = match(context.parent) as? PrismaSchemaDeclaration
        val params = declaration?.params
        if (context is PrismaSchemaDefaultParameterContext) {
          params?.firstOrNull()
        }
        else {
          params?.find { it.label == context.label }
        }
      }

      is PrismaSchemaVariantContext -> {
        val parent = match(context.parent)
        parent?.variants?.find { it.label == context.label }
      }
    }
  }
}

private typealias PrismaSchemaDynamicFactory = (PrismaSchemaEvaluationContext) -> PrismaSchemaElementGroup

class PrismaSchemaElementGroup(val kind: PrismaSchemaKind, val elements: Map<String, PrismaSchemaDeclaration>) {

  class Builder(private val kind: PrismaSchemaKind) : SchemaDslBuilder<PrismaSchemaElementGroup> {
    private val elements: MutableMap<String, PrismaSchemaDeclaration> = mutableMapOf()

    fun element(block: PrismaSchemaDeclaration.Builder.() -> Unit) {
      val elementBuilder = PrismaSchemaDeclaration.Builder(kind)
      elementBuilder.block()
      val schemaElement = elementBuilder.build()
      elements[schemaElement.label] = schemaElement
    }

    fun compose(group: PrismaSchemaElementGroup) {
      require(kind == group.kind)
      elements.putAll(group.elements)
    }

    override fun build(): PrismaSchemaElementGroup {
      return PrismaSchemaElementGroup(kind, elements)
    }
  }

}

sealed class PrismaSchemaElement(
  val label: String,
  val documentation: String? = null,
  val insertHandler: InsertHandler<LookupElement>? = null,
  val pattern: ElementPattern<out PsiElement>? = null,
  val datasources: Set<PrismaDatasourceType>? = null,
  val variants: List<PrismaSchemaVariant> = emptyList(),
  val type: String? = null,
  val ref: PrismaSchemaRef? = null,
) {
  fun isAvailableForDatasource(usedDatasource: PrismaDatasourceType?): Boolean {
    // filter only when datasource provider is specified
    return datasources == null ||
           usedDatasource == null ||
           datasources.contains(usedDatasource)
  }

  fun isAcceptedByPattern(element: PsiElement?, processingContext: ProcessingContext?): Boolean {
    return pattern?.accepts(element, processingContext ?: ProcessingContext()) ?: true
  }
}

open class PrismaSchemaDeclaration(
  val kind: PrismaSchemaKind,
  label: String,
  documentation: String? = null,
  val signature: String? = null,
  insertHandler: InsertHandler<LookupElement>? = null,
  val params: List<PrismaSchemaParameter> = emptyList(),
  pattern: ElementPattern<out PsiElement>? = null,
  datasources: Set<PrismaDatasourceType>? = null,
  variants: List<PrismaSchemaVariant> = emptyList(),
  type: String? = null,
) : PrismaSchemaElement(label, documentation, insertHandler, pattern, datasources, variants, type = type) {

  fun getAvailableParams(
    usedDatasource: PrismaDatasourceType?,
    isOnFieldLevel: Boolean,
  ): List<PrismaSchemaParameter> {
    return params.filter { it.isAvailableForDatasource(usedDatasource) && it.isOnFieldLevel == isOnFieldLevel }
  }

  open class Builder(private val kind: PrismaSchemaKind) : SchemaDslBuilder<PrismaSchemaDeclaration> {
    var label: String? = null
    var documentation: String? = null
    var signature: String? = null
    var insertHandler: InsertHandler<LookupElement>? = null
    var pattern: ElementPattern<out PsiElement>? = null
    var datasources: Set<PrismaDatasourceType>? = null
    var type: String? = null

    private var params: MutableList<PrismaSchemaParameter> = mutableListOf()
    private var variants: MutableList<PrismaSchemaVariant> = mutableListOf()

    fun param(block: PrismaSchemaParameter.Builder.() -> Unit) {
      val builder = PrismaSchemaParameter.Builder()
      builder.block()
      params.add(builder.build())
    }

    fun variant(block: PrismaSchemaVariant.Builder.() -> Unit) {
      val builder = PrismaSchemaVariant.Builder()
      builder.block()
      variants.add(builder.build())
    }

    override fun build(): PrismaSchemaDeclaration {
      return label
               ?.takeIf { it.isNotBlank() }
               ?.let {
                 PrismaSchemaDeclaration(
                   kind, it, documentation, signature, insertHandler,
                   params, pattern, datasources, variants, type
                 )
               }
             ?: error("label is not specified")
    }
  }
}

class PrismaSchemaParameter(
  label: String,
  documentation: String?,
  insertHandler: InsertHandler<LookupElement>? = null,
  datasources: Set<PrismaDatasourceType>? = null,
  variants: List<PrismaSchemaVariant> = emptyList(),
  type: String? = null,
  val isOnFieldLevel: Boolean = false,
  val skipInCompletion: Boolean = false,
) : PrismaSchemaElement(
  label,
  documentation,
  insertHandler = insertHandler,
  datasources = datasources,
  variants = variants,
  type = type,
) {
  class Builder : SchemaDslBuilder<PrismaSchemaParameter> {
    var label: String? = null
    var documentation: String? = null
    var type: String? = null
    var insertHandler: InsertHandler<LookupElement>? = null
    var datasources: Set<PrismaDatasourceType>? = null
    var isOnFieldLevel: Boolean = false
    var skipInCompletion: Boolean = false

    private var variants: MutableList<PrismaSchemaVariant> = mutableListOf()

    fun variant(block: PrismaSchemaVariant.Builder.() -> Unit) {
      val builder = PrismaSchemaVariant.Builder()
      builder.block()
      variants.add(builder.build())
    }

    override fun build(): PrismaSchemaParameter {
      return label
               ?.takeIf { it.isNotBlank() }
               ?.let {
                 PrismaSchemaParameter(
                   it, documentation, insertHandler, datasources,
                   variants, type, isOnFieldLevel, skipInCompletion
                 )
               }
             ?: error("label is not specified")
    }
  }
}

class PrismaSchemaVariant(
  label: String,
  documentation: String?,
  insertHandler: InsertHandler<LookupElement>? = null,
  type: String? = null,
  ref: PrismaSchemaRef? = null,
  datasources: Set<PrismaDatasourceType>? = null,
  pattern: ElementPattern<out PsiElement>? = null,
) : PrismaSchemaElement(
  label,
  documentation,
  insertHandler,
  ref = ref,
  type = type,
  datasources = datasources,
  pattern = pattern
) {
  class Builder : SchemaDslBuilder<PrismaSchemaVariant> {
    var label: String? = null
    var documentation: String? = null
    var insertHandler: InsertHandler<LookupElement>? = null
    var type: String? = null
    var ref: PrismaSchemaRef? = null
    var datasources: Set<PrismaDatasourceType>? = null
    var pattern: ElementPattern<out PsiElement>? = null

    override fun build(): PrismaSchemaVariant {
      val label = ref?.label ?: label
      return label
               ?.takeIf { it.isNotBlank() }
               ?.let { PrismaSchemaVariant(it, documentation, insertHandler, type, ref, datasources, pattern) }
             ?: error("label is not specified")
    }
  }
}

fun schema(block: PrismaCompoundSchema.Builder.() -> Unit): PrismaCompoundSchema {
  val builder = PrismaCompoundSchema.Builder()
  builder.block()
  return builder.build()
}

class PrismaSchemaEvaluationContext(val position: PsiElement?, val file: PsiFile?) {
  companion object {
    fun forElement(element: PsiElement?): PrismaSchemaEvaluationContext {
      return PrismaSchemaEvaluationContext(element, element?.containingFile)
    }
  }
}

@DslMarker
annotation class SchemaDslBuilderMarker

@SchemaDslBuilderMarker
interface SchemaDslBuilder<out T> {
  fun build(): T

  fun String.list(): String {
    return "$this[]"
  }

  fun String.optional(): String {
    return "$this?"
  }
}

data class PrismaSchemaRef(val kind: PrismaSchemaKind, val label: String)