package org.intellij.prisma.ide.schema.types

import org.intellij.prisma.ide.schema.types.PrismaNativeTypeConstructor.Companion.withArgs
import org.intellij.prisma.ide.schema.types.PrismaNativeTypeConstructor.Companion.withOptionalArgs
import org.intellij.prisma.ide.schema.types.PrismaNativeTypeConstructor.Companion.withoutArgs
import org.intellij.prisma.lang.types.*

object PrismaNativeType {
  fun findConstructorsByType(datasourceType: PrismaDatasourceType): List<PrismaNativeTypeConstructor> {
    return when (datasourceType) {
      PrismaDatasourceType.SQLITE -> emptyList()
      PrismaDatasourceType.MYSQL -> MySQL.CONSTRUCTORS
      PrismaDatasourceType.POSTGRESQL -> PostgreSQL.CONSTRUCTORS
      PrismaDatasourceType.SQLSERVER -> SQLServer.CONSTRUCTORS
      PrismaDatasourceType.MONGODB -> MongoDB.CONSTRUCTORS
      PrismaDatasourceType.COCKROACHDB -> CockroachDB.CONSTRUCTORS
    }
  }

  object PostgreSQL {
    const val SMALL_INT_TYPE_NAME = "SmallInt"
    const val INTEGER_TYPE_NAME = "Integer"
    const val BIG_INT_TYPE_NAME = "BigInt"
    const val DECIMAL_TYPE_NAME = "Decimal"
    const val MONEY_TYPE_NAME = "Money"
    const val INET_TYPE_NAME = "Inet"
    const val CITEXT_TYPE_NAME = "Citext"
    const val OID_TYPE_NAME = "Oid"
    const val REAL_TYPE_NAME = "Real"
    const val DOUBLE_PRECISION_TYPE_NAME = "DoublePrecision"
    const val VARCHAR_TYPE_NAME = "VarChar"
    const val CHAR_TYPE_NAME = "Char"
    const val TEXT_TYPE_NAME = "Text"
    const val BYTE_A_TYPE_NAME = "ByteA"
    const val TIMESTAMP_TYPE_NAME = "Timestamp"
    const val TIMESTAMP_TZ_TYPE_NAME = "Timestamptz"
    const val DATE_TYPE_NAME = "Date"
    const val TIME_TYPE_NAME = "Time"
    const val TIME_TZ_TYPE_NAME = "Timetz"
    const val BOOLEAN_TYPE_NAME = "Boolean"
    const val BIT_TYPE_NAME = "Bit"
    const val VAR_BIT_TYPE_NAME = "VarBit"
    const val UUID_TYPE_NAME = "Uuid"
    const val XML_TYPE_NAME = "Xml"
    const val JSON_TYPE_NAME = "Json"
    const val JSON_B_TYPE_NAME = "JsonB"

    val CONSTRUCTORS = listOf(
      withoutArgs(SMALL_INT_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(INTEGER_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(BIG_INT_TYPE_NAME, listOf(PrismaBigIntType)),
      withOptionalArgs(DECIMAL_TYPE_NAME, 2, listOf(PrismaDecimalType)),
      withoutArgs(MONEY_TYPE_NAME, listOf(PrismaDecimalType)),
      withoutArgs(INET_TYPE_NAME, listOf(PrismaStringType)),
      withoutArgs(CITEXT_TYPE_NAME, listOf(PrismaStringType)),
      withoutArgs(OID_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(REAL_TYPE_NAME, listOf(PrismaFloatType)),
      withoutArgs(DOUBLE_PRECISION_TYPE_NAME, listOf(PrismaFloatType)),
      withOptionalArgs(VARCHAR_TYPE_NAME, 1, listOf(PrismaStringType)),
      withOptionalArgs(CHAR_TYPE_NAME, 1, listOf(PrismaStringType)),
      withoutArgs(TEXT_TYPE_NAME, listOf(PrismaStringType)),
      withoutArgs(BYTE_A_TYPE_NAME, listOf(PrismaBytesType)),
      withOptionalArgs(TIMESTAMP_TYPE_NAME, 1, listOf(PrismaDateTimeType)),
      withOptionalArgs(TIMESTAMP_TZ_TYPE_NAME, 1, listOf(PrismaDateTimeType)),
      withoutArgs(DATE_TYPE_NAME, listOf(PrismaDateTimeType)),
      withOptionalArgs(TIME_TYPE_NAME, 1, listOf(PrismaDateTimeType)),
      withOptionalArgs(TIME_TZ_TYPE_NAME, 1, listOf(PrismaDateTimeType)),
      withoutArgs(BOOLEAN_TYPE_NAME, listOf(PrismaBooleanType)),
      withOptionalArgs(BIT_TYPE_NAME, 1, listOf(PrismaStringType)),
      withOptionalArgs(VAR_BIT_TYPE_NAME, 1, listOf(PrismaStringType)),
      withoutArgs(UUID_TYPE_NAME, listOf(PrismaStringType)),
      withoutArgs(XML_TYPE_NAME, listOf(PrismaStringType)),
      withoutArgs(JSON_TYPE_NAME, listOf(PrismaJsonType)),
      withoutArgs(JSON_B_TYPE_NAME, listOf(PrismaJsonType)),
    )
  }

  object MySQL {
    const val INT_TYPE_NAME = "Int"
    const val UNSIGNED_INT_TYPE_NAME = "UnsignedInt"
    const val SMALL_INT_TYPE_NAME = "SmallInt"
    const val UNSIGNED_SMALL_INT_TYPE_NAME = "UnsignedSmallInt"
    const val TINY_INT_TYPE_NAME = "TinyInt"
    const val UNSIGNED_TINY_INT_TYPE_NAME = "UnsignedTinyInt"
    const val MEDIUM_INT_TYPE_NAME = "MediumInt"
    const val UNSIGNED_MEDIUM_INT_TYPE_NAME = "UnsignedMediumInt"
    const val BIG_INT_TYPE_NAME = "BigInt"
    const val UNSIGNED_BIG_INT_TYPE_NAME = "UnsignedBigInt"
    const val DECIMAL_TYPE_NAME = "Decimal"
    const val FLOAT_TYPE_NAME = "Float"
    const val DOUBLE_TYPE_NAME = "Double"
    const val BIT_TYPE_NAME = "Bit"
    const val CHAR_TYPE_NAME = "Char"
    const val VAR_CHAR_TYPE_NAME = "VarChar"
    const val BINARY_TYPE_NAME = "Binary"
    const val VAR_BINARY_TYPE_NAME = "VarBinary"
    const val TINY_BLOB_TYPE_NAME = "TinyBlob"
    const val BLOB_TYPE_NAME = "Blob"
    const val MEDIUM_BLOB_TYPE_NAME = "MediumBlob"
    const val LONG_BLOB_TYPE_NAME = "LongBlob"
    const val TINY_TEXT_TYPE_NAME = "TinyText"
    const val TEXT_TYPE_NAME = "Text"
    const val MEDIUM_TEXT_TYPE_NAME = "MediumText"
    const val LONG_TEXT_TYPE_NAME = "LongText"
    const val DATE_TYPE_NAME = "Date"
    const val TIME_TYPE_NAME = "Time"
    const val DATETIME_TYPE_NAME = "DateTime"
    const val TIMESTAMP_TYPE_NAME = "Timestamp"
    const val YEAR_TYPE_NAME = "Year"
    const val JSON_TYPE_NAME = "Json"

    val CONSTRUCTORS = listOf(
      withoutArgs(INT_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(UNSIGNED_INT_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(SMALL_INT_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(UNSIGNED_SMALL_INT_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(TINY_INT_TYPE_NAME, listOf(PrismaBooleanType, PrismaIntType)),
      withoutArgs(UNSIGNED_TINY_INT_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(MEDIUM_INT_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(UNSIGNED_MEDIUM_INT_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(BIG_INT_TYPE_NAME, listOf(PrismaBigIntType)),
      withoutArgs(UNSIGNED_BIG_INT_TYPE_NAME, listOf(PrismaBigIntType)),
      withOptionalArgs(DECIMAL_TYPE_NAME, 2, listOf(PrismaDecimalType)),
      withoutArgs(FLOAT_TYPE_NAME, listOf(PrismaFloatType)),
      withoutArgs(DOUBLE_TYPE_NAME, listOf(PrismaFloatType)),
      withArgs(BIT_TYPE_NAME, 1, listOf(PrismaBooleanType, PrismaBytesType)),
      withArgs(CHAR_TYPE_NAME, 1, listOf(PrismaStringType)),
      withArgs(VAR_CHAR_TYPE_NAME, 1, listOf(PrismaStringType)),
      withArgs(BINARY_TYPE_NAME, 1, listOf(PrismaBytesType)),
      withArgs(VAR_BINARY_TYPE_NAME, 1, listOf(PrismaBytesType)),
      withoutArgs(TINY_BLOB_TYPE_NAME, listOf(PrismaBytesType)),
      withoutArgs(BLOB_TYPE_NAME, listOf(PrismaBytesType)),
      withoutArgs(MEDIUM_BLOB_TYPE_NAME, listOf(PrismaBytesType)),
      withoutArgs(LONG_BLOB_TYPE_NAME, listOf(PrismaBytesType)),
      withoutArgs(TINY_TEXT_TYPE_NAME, listOf(PrismaStringType)),
      withoutArgs(TEXT_TYPE_NAME, listOf(PrismaStringType)),
      withoutArgs(MEDIUM_TEXT_TYPE_NAME, listOf(PrismaStringType)),
      withoutArgs(LONG_TEXT_TYPE_NAME, listOf(PrismaStringType)),
      withoutArgs(DATE_TYPE_NAME, listOf(PrismaDateTimeType)),
      withOptionalArgs(TIME_TYPE_NAME, 1, listOf(PrismaDateTimeType)),
      withOptionalArgs(DATETIME_TYPE_NAME, 1, listOf(PrismaDateTimeType)),
      withOptionalArgs(TIMESTAMP_TYPE_NAME, 1, listOf(PrismaDateTimeType)),
      withoutArgs(YEAR_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(JSON_TYPE_NAME, listOf(PrismaJsonType)),
    )
  }

  object SQLServer {
    const val TINY_INT_TYPE_NAME = "TinyInt"
    const val SMALL_INT_TYPE_NAME = "SmallInt"
    const val INT_TYPE_NAME = "Int"
    const val BIG_INT_TYPE_NAME = "BigInt"
    const val DECIMAL_TYPE_NAME = "Decimal"
    const val NUMERIC_TYPE_NAME = "Numeric"
    const val MONEY_TYPE_NAME = "Money"
    const val SMALL_MONEY_TYPE_NAME = "SmallMoney"
    const val BIT_TYPE_NAME = "Bit"
    const val FLOAT_TYPE_NAME = "Float"
    const val REAL_TYPE_NAME = "Real"
    const val DATE_TYPE_NAME = "Date"
    const val TIME_TYPE_NAME = "Time"
    const val DATETIME_TYPE_NAME = "DateTime"
    const val DATETIME2_TYPE_NAME = "DateTime2"
    const val DATETIME_OFFSET_TYPE_NAME = "DateTimeOffset"
    const val SMALL_DATETIME_TYPE_NAME = "SmallDateTime"
    const val CHAR_TYPE_NAME = "Char"
    const val NCHAR_TYPE_NAME = "NChar"
    const val VARCHAR_TYPE_NAME = "VarChar"
    const val TEXT_TYPE_NAME = "Text"
    const val NVARCHAR_TYPE_NAME = "NVarChar"
    const val NTEXT_TYPE_NAME = "NText"
    const val BINARY_TYPE_NAME = "Binary"
    const val VAR_BINARY_TYPE_NAME = "VarBinary"
    const val IMAGE_TYPE_NAME = "Image"
    const val XML_TYPE_NAME = "Xml"
    const val UNIQUE_IDENTIFIER_TYPE_NAME = "UniqueIdentifier"

    val CONSTRUCTORS = listOf(
      withoutArgs(TINY_INT_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(SMALL_INT_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(INT_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(BIG_INT_TYPE_NAME, listOf(PrismaBigIntType)),
      withOptionalArgs(DECIMAL_TYPE_NAME, 2, listOf(PrismaDecimalType)),
      withOptionalArgs(NUMERIC_TYPE_NAME, 2, listOf(PrismaDecimalType)),
      withoutArgs(MONEY_TYPE_NAME, listOf(PrismaFloatType)),
      withoutArgs(SMALL_MONEY_TYPE_NAME, listOf(PrismaFloatType)),
      withoutArgs(BIT_TYPE_NAME, listOf(PrismaBooleanType, PrismaIntType)),
      withOptionalArgs(FLOAT_TYPE_NAME, 1, listOf(PrismaFloatType)),
      withoutArgs(REAL_TYPE_NAME, listOf(PrismaFloatType)),
      withoutArgs(DATE_TYPE_NAME, listOf(PrismaDateTimeType)),
      withoutArgs(TIME_TYPE_NAME, listOf(PrismaDateTimeType)),
      withoutArgs(DATETIME_TYPE_NAME, listOf(PrismaDateTimeType)),
      withoutArgs(DATETIME2_TYPE_NAME, listOf(PrismaDateTimeType)),
      withoutArgs(DATETIME_OFFSET_TYPE_NAME, listOf(PrismaDateTimeType)),
      withoutArgs(SMALL_DATETIME_TYPE_NAME, listOf(PrismaDateTimeType)),
      withOptionalArgs(CHAR_TYPE_NAME, 1, listOf(PrismaStringType)),
      withOptionalArgs(NCHAR_TYPE_NAME, 1, listOf(PrismaStringType)),
      withOptionalArgs(VARCHAR_TYPE_NAME, 1, listOf(PrismaStringType)),
      withoutArgs(TEXT_TYPE_NAME, listOf(PrismaStringType)),
      withOptionalArgs(NVARCHAR_TYPE_NAME, 1, listOf(PrismaStringType)),
      withoutArgs(NTEXT_TYPE_NAME, listOf(PrismaStringType)),
      withOptionalArgs(BINARY_TYPE_NAME, 1, listOf(PrismaBytesType)),
      withOptionalArgs(VAR_BINARY_TYPE_NAME, 1, listOf(PrismaBytesType)),
      withoutArgs(IMAGE_TYPE_NAME, listOf(PrismaBytesType)),
      withoutArgs(XML_TYPE_NAME, listOf(PrismaStringType)),
      withoutArgs(UNIQUE_IDENTIFIER_TYPE_NAME, listOf(PrismaStringType)),
    )
  }

  object CockroachDB {
    const val BIT_TYPE_NAME = "Bit"
    const val BOOL_TYPE_NAME = "Bool"
    const val BYTES_TYPE_NAME = "Bytes"
    const val CHAR_TYPE_NAME = "Char"
    const val DATE_TYPE_NAME = "Date"
    const val DECIMAL_TYPE_NAME = "Decimal"
    const val FLOAT4_TYPE_NAME = "Float4"
    const val FLOAT8_TYPE_NAME = "Float8"
    const val INET_TYPE_NAME = "Inet"
    const val INT2_TYPE_NAME = "Int2"
    const val INT4_TYPE_NAME = "Int4"
    const val INT8_TYPE_NAME = "Int8"
    const val JSON_B_TYPE_NAME = "JsonB"
    const val OID_TYPE_NAME = "Oid"
    const val CATALOG_SINGLE_CHAR_TYPE_NAME = "CatalogSingleChar"
    const val STRING_TYPE_NAME = "String"
    const val TIMESTAMP_TYPE_NAME = "Timestamp"
    const val TIMESTAMP_TZ_TYPE_NAME = "Timestamptz"
    const val TIME_TYPE_NAME = "Time"
    const val TIME_TZ_TYPE_NAME = "Timetz"
    const val UUID_TYPE_NAME = "Uuid"
    const val VAR_BIT_TYPE_NAME = "VarBit"

    val CONSTRUCTORS = listOf(
      withOptionalArgs(BIT_TYPE_NAME, 1, listOf(PrismaStringType)),
      withOptionalArgs(CHAR_TYPE_NAME, 1, listOf(PrismaStringType)),
      withOptionalArgs(DECIMAL_TYPE_NAME, 2, listOf(PrismaDecimalType)),
      withOptionalArgs(STRING_TYPE_NAME, 1, listOf(PrismaStringType)),
      withOptionalArgs(TIMESTAMP_TYPE_NAME, 1, listOf(PrismaDateTimeType)),
      withOptionalArgs(TIMESTAMP_TZ_TYPE_NAME, 1, listOf(PrismaDateTimeType)),
      withOptionalArgs(TIME_TYPE_NAME, 1, listOf(PrismaDateTimeType)),
      withOptionalArgs(TIME_TZ_TYPE_NAME, 1, listOf(PrismaDateTimeType)),
      withOptionalArgs(VAR_BIT_TYPE_NAME, 1, listOf(PrismaStringType)),
      withoutArgs(BOOL_TYPE_NAME, listOf(PrismaBooleanType)),
      withoutArgs(BYTES_TYPE_NAME, listOf(PrismaBytesType)),
      withoutArgs(DATE_TYPE_NAME, listOf(PrismaDateTimeType)),
      withoutArgs(FLOAT4_TYPE_NAME, listOf(PrismaFloatType)),
      withoutArgs(FLOAT8_TYPE_NAME, listOf(PrismaFloatType)),
      withoutArgs(INET_TYPE_NAME, listOf(PrismaStringType)),
      withoutArgs(INT2_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(INT4_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(INT8_TYPE_NAME, listOf(PrismaBigIntType)),
      withoutArgs(JSON_B_TYPE_NAME, listOf(PrismaJsonType)),
      withoutArgs(OID_TYPE_NAME, listOf(PrismaIntType)),
      withoutArgs(CATALOG_SINGLE_CHAR_TYPE_NAME, listOf(PrismaStringType)),
      withoutArgs(UUID_TYPE_NAME, listOf(PrismaStringType)),
    )
  }

  object MongoDB {
    const val STRING = "String"
    const val DOUBLE = "Double"
    const val LONG = "Long"
    const val INT = "Int"
    const val BIN_DATA = "BinData"
    const val OBJECT_ID = "ObjectId"
    const val BOOL = "Bool"
    const val DATE = "Date"
    const val TIMESTAMP = "Timestamp"
    const val JSON = "Json"

    val CONSTRUCTORS = listOf(
      withoutArgs(STRING, listOf(PrismaStringType)),
      withoutArgs(DOUBLE, listOf(PrismaFloatType)),
      withoutArgs(LONG, listOf(PrismaIntType, PrismaBigIntType)),
      withoutArgs(INT, listOf(PrismaIntType)),
      withoutArgs(BIN_DATA, listOf(PrismaBytesType)),
      withoutArgs(OBJECT_ID, listOf(PrismaStringType, PrismaBytesType)),
      withoutArgs(BOOL, listOf(PrismaBooleanType)),
      withoutArgs(DATE, listOf(PrismaDateTimeType)),
      withoutArgs(TIMESTAMP, listOf(PrismaDateTimeType)),
      withoutArgs(JSON, listOf(PrismaJsonType)),
    )
  }
}