package org.intellij.prisma.ide.schema.types

import org.intellij.prisma.lang.PrismaConstants.DatasourceTypes
import java.util.*

enum class PrismaDatasourceType(val presentation: String) {
  MYSQL(DatasourceTypes.MYSQL),
  POSTGRESQL(DatasourceTypes.POSTGRESQL),
  SQLITE(DatasourceTypes.SQLITE),
  SQLSERVER(DatasourceTypes.SQLSERVER),
  MONGODB(DatasourceTypes.MONGODB),
  COCKROACHDB(DatasourceTypes.COCKROACHDB);

  companion object {
    val ALL: Set<PrismaDatasourceType> = EnumSet.allOf(PrismaDatasourceType::class.java)

    fun fromString(s: String?): PrismaDatasourceType? =
      PrismaDatasourceType.values().find { it.presentation == s }

    fun except(vararg types: PrismaDatasourceType): Set<PrismaDatasourceType> {
      return ALL - types.toSet()
    }
  }
}