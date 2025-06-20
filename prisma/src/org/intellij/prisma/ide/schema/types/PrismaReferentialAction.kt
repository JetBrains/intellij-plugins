package org.intellij.prisma.ide.schema.types

import org.intellij.prisma.ide.schema.types.PrismaDatasourceProviderType.SQLSERVER

enum class PrismaReferentialAction(val documentation: String, val datasources: Set<PrismaDatasourceProviderType>) {
  Cascade(
    "Delete the child records when the parent record is deleted.",
    PrismaDatasourceProviderType.ALL
  ),
  Restrict(
    "Prevent deleting a parent record as long as it is referenced.",
    PrismaDatasourceProviderType.except(SQLSERVER)
  ),
  NoAction(
    "Prevent deleting a parent record as long as it is referenced.",
    PrismaDatasourceProviderType.ALL
  ),
  SetNull(
    "Set the referencing fields to NULL when the referenced record is deleted.",
    PrismaDatasourceProviderType.ALL
  ),
  SetDefault(
    "Set the referencing field's value to the default when the referenced record is deleted.",
    PrismaDatasourceProviderType.ALL
  );
}