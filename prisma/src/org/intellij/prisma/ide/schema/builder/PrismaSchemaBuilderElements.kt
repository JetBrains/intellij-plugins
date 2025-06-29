// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.builder

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.types.PrismaDatasourceProviderType

sealed interface PrismaSchemaElement {
  val label: String?
  val documentation: String?
  val insertHandler: InsertHandler<LookupElement>?
  val type: String?
}

class PrismaSchemaDeclaration(
  val kind: PrismaSchemaKind,
  override val label: String,
  override val documentation: String? = null,
  val signature: String? = null,
  override val insertHandler: InsertHandler<LookupElement>? = null,
  val params: List<PrismaSchemaParameter> = emptyList(),
  override val pattern: ElementPattern<out PsiElement>? = null,
  override val datasources: Set<PrismaDatasourceProviderType>? = null,
  override val variants: List<PrismaSchemaVariant> = emptyList(),
  override val type: String? = null,
  override val resolver: PrismaSchemaResolver? = null,
) : PrismaSchemaElement, PrismaSchemaPatternCapability, PrismaSchemaVariantsCapability, PrismaSchemaDatasourcesCapability, PrismaSchemaDeclarationResolverCapability {
  fun getAvailableParams(usedDatasources: Set<PrismaDatasourceProviderType>, isOnFieldLevel: Boolean): List<PrismaSchemaParameter> =
    params.filter { it.isAvailableForDatasources(usedDatasources) && it.isOnFieldLevel == isOnFieldLevel }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as PrismaSchemaDeclaration

    if (kind != other.kind) return false
    if (label != other.label) return false

    return true
  }

  override fun hashCode(): Int {
    var result = kind.hashCode()
    result = 31 * result + label.hashCode()
    return result
  }

  open class Builder(private val kind: PrismaSchemaKind) : SchemaDslBuilder<PrismaSchemaDeclaration> {
    var label: String? = null
    var documentation: String? = null
    var signature: String? = null
    var insertHandler: InsertHandler<LookupElement>? = null
    var pattern: ElementPattern<out PsiElement>? = null
    var datasources: Set<PrismaDatasourceProviderType>? = null
    var type: String? = null
    var resolver: PrismaSchemaResolver? = null

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
                   params, pattern, datasources, variants, type, resolver
                 )
               }
             ?: error("label is not specified")
    }
  }
}

class PrismaSchemaParameter(
  override val label: String,
  override val documentation: String?,
  override val insertHandler: InsertHandler<LookupElement>? = null,
  override val datasources: Set<PrismaDatasourceProviderType>? = null,
  override val variants: List<PrismaSchemaVariant> = emptyList(),
  override val type: String? = null,
  val isOnFieldLevel: Boolean = false,
  val skipInCompletion: Boolean = false,
  override val resolver: PrismaSchemaResolver? = null,
) : PrismaSchemaElement, PrismaSchemaVariantsCapability, PrismaSchemaDatasourcesCapability, PrismaSchemaDeclarationResolverCapability {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as PrismaSchemaParameter

    return label == other.label
  }

  override fun hashCode(): Int {
    return label.hashCode()
  }

  class Builder : SchemaDslBuilder<PrismaSchemaParameter> {
    var label: String? = null
    var documentation: String? = null
    var type: String? = null
    var insertHandler: InsertHandler<LookupElement>? = null
    var datasources: Set<PrismaDatasourceProviderType>? = null
    var isOnFieldLevel: Boolean = false
    var skipInCompletion: Boolean = false
    var resolver: PrismaSchemaResolver? = null

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
                   variants, type, isOnFieldLevel, skipInCompletion, resolver
                 )
               }
             ?: error("label is not specified")
    }
  }
}

class PrismaSchemaVariant(
  override val label: String?,
  override val documentation: String?,
  override val insertHandler: InsertHandler<LookupElement>? = null,
  override val type: String? = null,
  override val ref: PrismaSchemaRef? = null,
  override val datasources: Set<PrismaDatasourceProviderType>? = null,
  override val pattern: ElementPattern<out PsiElement>? = null,
  override val resolver: PrismaSchemaResolver? = null,
) : PrismaSchemaElement, PrismaSchemaRefCapability, PrismaSchemaPatternCapability, PrismaSchemaDatasourcesCapability, PrismaSchemaDeclarationResolverCapability {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as PrismaSchemaVariant

    if (label != other.label) return false
    if (ref != other.ref) return false

    return true
  }

  override fun hashCode(): Int {
    var result = label?.hashCode() ?: 0
    result = 31 * result + (ref?.hashCode() ?: 0)
    return result
  }

  class Builder : SchemaDslBuilder<PrismaSchemaVariant> {
    var label: String? = null
    var documentation: String? = null
    private var insertHandler: InsertHandler<LookupElement>? = null
    var type: String? = null
    var datasources: Set<PrismaDatasourceProviderType>? = null
    var pattern: ElementPattern<out PsiElement>? = null
    var resolver: PrismaSchemaResolver? = null

    private var ref: PrismaSchemaRef? = null

    fun ref(block: PrismaSchemaRef.Builder.() -> Unit) {
      val builder = PrismaSchemaRef.Builder()
      builder.block()
      ref = builder.build()
    }

    override fun build(): PrismaSchemaVariant {
      val label = ref?.label ?: label
      return PrismaSchemaVariant(label, documentation, insertHandler, type, ref, datasources, pattern, resolver)
    }
  }
}

data class PrismaSchemaRef(val kind: PrismaSchemaKind, val label: String?) {
  class Builder : SchemaDslBuilder<PrismaSchemaRef> {
    var kind: PrismaSchemaKind? = null
    var label: String? = null

    override fun build(): PrismaSchemaRef {
      return PrismaSchemaRef(
        kind ?: error("kind is not specified"),
        label
      )
    }
  }
}

fun schema(block: PrismaCompoundSchema.Builder.() -> Unit): PrismaCompoundSchema {
  val builder = PrismaCompoundSchema.Builder()
  builder.block()
  return builder.build()
}
