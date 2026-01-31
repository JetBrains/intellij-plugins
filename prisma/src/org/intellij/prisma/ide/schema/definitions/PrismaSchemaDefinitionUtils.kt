// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.definitions

import org.intellij.prisma.ide.schema.builder.PrismaSchemaDeclaration
import org.intellij.prisma.ide.schema.builder.PrismaSchemaParameterLocation
import org.intellij.prisma.ide.schema.types.PrismaDatasourceProviderType
import org.intellij.prisma.ide.schema.types.PrismaDatasourceProviderType.MYSQL
import org.intellij.prisma.ide.schema.types.PrismaDatasourceProviderType.SQLSERVER
import org.intellij.prisma.lang.PrismaConstants.ParameterNames
import org.intellij.prisma.lang.PrismaConstants.PrimitiveTypes
import org.intellij.prisma.lang.PrismaConstants.Types
import java.util.EnumSet

fun PrismaSchemaDeclaration.Builder.sort(
  parameterLocation: PrismaSchemaParameterLocation = PrismaSchemaParameterLocation.DEFAULT,
  datasourceTypes: Set<PrismaDatasourceProviderType>? = null,
) {
  param {
    label = ParameterNames.SORT
    documentation =
      "Specify in which order the entries of the index are stored in the database. This can have an effect on whether the database is able to use an index for specific queries."
    type = Types.SORT_ORDER.optional()
    datasources = datasourceTypes
    location = parameterLocation

    variantsForType(Types.SORT_ORDER)
  }
}

fun PrismaSchemaDeclaration.Builder.length(parameterLocation: PrismaSchemaParameterLocation = PrismaSchemaParameterLocation.DEFAULT) {
  param {
    label = ParameterNames.LENGTH
    documentation =
      "Defines a maximum length for the subpart of the value to be indexed in cases where the full value would exceed MySQL's limits for index sizes. See https://dev.mysql.com/doc/refman/8.0/en/innodb-limits.html"
    type = "Int?"
    datasources = EnumSet.of(MYSQL)
    location = parameterLocation
  }
}

fun PrismaSchemaDeclaration.Builder.clustered() {
  param {
    label = ParameterNames.CLUSTERED
    documentation =
      "An index, unique constraint or primary key can be created as clustered or non-clustered; altering the storage and retrieve behavior of the index."
    type = PrimitiveTypes.BOOLEAN.optional()
    datasources = EnumSet.of(SQLSERVER)

    variantsForType(PrimitiveTypes.BOOLEAN)
  }
}