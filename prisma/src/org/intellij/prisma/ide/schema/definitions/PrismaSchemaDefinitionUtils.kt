// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.definitions

import org.intellij.prisma.ide.schema.PrismaSchemaDeclaration
import org.intellij.prisma.ide.schema.types.PrismaDatasourceType
import org.intellij.prisma.lang.PrismaConstants
import java.util.*

fun PrismaSchemaDeclaration.Builder.sort(
  isOnField: Boolean = false,
  datasourceTypes: Set<PrismaDatasourceType>? = null
) {
  param {
    label = PrismaConstants.ParameterNames.SORT
    documentation =
      "Specify in which order the entries of the index are stored in the database. This can have an effect on whether the database is able to use an index for specific queries."
    type = PrismaConstants.Types.SORT_ORDER.optional()
    datasources = datasourceTypes
    isOnFieldLevel = isOnField

    variantsForType(PrismaConstants.Types.SORT_ORDER)
  }
}

fun PrismaSchemaDeclaration.Builder.length(isOnField: Boolean = false) {
  param {
    label = PrismaConstants.ParameterNames.LENGTH
    documentation =
      "Defines a maximum length for the subpart of the value to be indexed in cases where the full value would exceed MySQL's limits for index sizes. See https://dev.mysql.com/doc/refman/8.0/en/innodb-limits.html"
    type = "Int?"
    datasources = EnumSet.of(PrismaDatasourceType.MYSQL)
    isOnFieldLevel = isOnField
  }
}

fun PrismaSchemaDeclaration.Builder.clustered() {
  param {
    label = PrismaConstants.ParameterNames.CLUSTERED
    documentation =
      "An index, unique constraint or primary key can be created as clustered or non-clustered; altering the storage and retrieve behavior of the index."
    type = PrismaConstants.PrimitiveTypes.BOOLEAN.optional()
    datasources = EnumSet.of(PrismaDatasourceType.SQLSERVER)

    variantsForType(PrismaConstants.PrimitiveTypes.BOOLEAN)
  }
}