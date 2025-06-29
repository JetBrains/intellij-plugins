// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.symbols

import com.intellij.find.usages.api.SearchTarget
import com.intellij.find.usages.api.UsageHandler
import com.intellij.icons.AllIcons
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.platform.backend.navigation.NavigationRequest
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.ElementManipulators
import com.intellij.psi.createSmartPointer
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.startOffset
import com.intellij.refactoring.rename.api.RenameTarget
import org.intellij.prisma.PrismaBundle
import org.intellij.prisma.ide.schema.builder.PrismaSchemaElement
import org.intellij.prisma.lang.psi.PrismaStringLiteralExpression
import org.intellij.prisma.lang.resolve.getSchemaScope

class PrismaSchemaSymbol(
  val declaration: PrismaStringLiteralExpression,
  val schemaElement: PrismaSchemaElement,
) : Symbol, SearchTarget, RenameTarget, NavigationTarget {
  override val targetName: String
    get() = ElementManipulators.getValueText(declaration)

  override val usageHandler: UsageHandler
    get() = UsageHandler.createEmptyUsageHandler(targetName)

  override val maximalSearchScope: SearchScope
    get() = getSchemaScope(declaration)

  override fun presentation(): TargetPresentation =
    TargetPresentation
      .builder(PrismaBundle.message("prisma.usage.type.schema", targetName))
      .withLocationIn(declaration.containingFile)
      .icon(AllIcons.Nodes.DataSchema)
      .presentation()

  override fun computePresentation(): TargetPresentation =
    presentation()

  override fun navigationRequest(): NavigationRequest? =
    NavigationRequest.sourceNavigationRequest(
      declaration.containingFile,
      ElementManipulators.getValueTextRange(declaration).shiftRight(declaration.startOffset)
    )

  override fun createPointer(): Pointer<out PrismaSchemaSymbol?> {
    val elementPtr = declaration.createSmartPointer()
    val schemaElement = schemaElement
    return Pointer { PrismaSchemaSymbol(elementPtr.dereference() ?: return@Pointer null, schemaElement) }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as PrismaSchemaSymbol

    if (declaration != other.declaration) return false
    if (schemaElement != other.schemaElement) return false

    return true
  }

  override fun hashCode(): Int {
    var result = declaration.hashCode()
    result = 31 * result + schemaElement.hashCode()
    return result
  }
}
