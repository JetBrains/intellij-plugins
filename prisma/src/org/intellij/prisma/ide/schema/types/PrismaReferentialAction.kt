package org.intellij.prisma.ide.schema.types

enum class PrismaReferentialAction(val documentation: String, val datasources: Set<PrismaDatasourceType>) {
  Cascade(
    "Delete the child records when the parent record is deleted.",
    PrismaDatasourceType.ALL
  ),
  Restrict(
    "Prevent deleting a parent record as long as it is referenced.",
    PrismaDatasourceType.except(PrismaDatasourceType.SQLSERVER)
  ),
  NoAction(
    "Prevent deleting a parent record as long as it is referenced.",
    PrismaDatasourceType.ALL
  ),
  SetNull(
    "Set the referencing fields to NULL when the referenced record is deleted.",
    PrismaDatasourceType.ALL
  ),
  SetDefault(
    "Set the referencing field's value to the default when the referenced record is deleted.",
    PrismaDatasourceType.ALL
  );
}