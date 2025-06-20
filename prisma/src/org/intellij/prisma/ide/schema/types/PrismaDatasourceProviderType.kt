package org.intellij.prisma.ide.schema.types

import org.intellij.prisma.lang.PrismaConstants.DatasourceTypes
import java.util.*

enum class PrismaDatasourceProviderType(val presentation: String) {
  MYSQL(DatasourceTypes.MYSQL),
  POSTGRESQL(DatasourceTypes.POSTGRESQL),
  SQLITE(DatasourceTypes.SQLITE),
  SQLSERVER(DatasourceTypes.SQLSERVER),
  MONGODB(DatasourceTypes.MONGODB),
  COCKROACHDB(DatasourceTypes.COCKROACHDB);

  companion object {
    val ALL: Set<PrismaDatasourceProviderType> = EnumSet.allOf(PrismaDatasourceProviderType::class.java)

    fun fromString(s: String?): PrismaDatasourceProviderType? =
      PrismaDatasourceProviderType.entries.find { it.presentation == s }

    fun except(vararg types: PrismaDatasourceProviderType): Set<PrismaDatasourceProviderType> {
      return ALL - types.toSet()
    }
  }
}