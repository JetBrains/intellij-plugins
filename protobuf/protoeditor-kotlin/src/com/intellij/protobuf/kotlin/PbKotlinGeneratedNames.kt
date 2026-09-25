package com.intellij.protobuf.kotlin

import com.intellij.protobuf.jvm.names.NameGeneratorSelector
import com.intellij.protobuf.jvm.names.NameUtils
import com.intellij.protobuf.lang.psi.PbField
import com.intellij.protobuf.lang.psi.PbMessageType
import com.intellij.protobuf.lang.psi.util.PbPsiUtil

internal object PbKotlinGeneratedNames {
    private val forbiddenNames = setOf(
        "as",
        "as?",
        "break",
        "class",
        "continue",
        "do",
        "else",
        "false",
        "for",
        "fun",
        "if",
        "in",
        "!in",
        "interface",
        "is",
        "!is",
        "null",
        "object",
        "package",
        "return",
        "super",
        "this",
        "throw",
        "true",
        "try",
        "typealias",
        "typeof",
        "val",
        "var",
        "when",
        "while",
    )

    fun dslFactoryNameForMessage(messageName: String): String {
        val factoryName = buildString(messageName.length) {
            var capitalizeNext = false
            for (char in messageName) {
                when (char) {
                    in 'a'..'z' -> {
                        append(if (capitalizeNext) char.uppercaseChar() else char)
                        capitalizeNext = false
                    }

                    in 'A'..'Z', in '0'..'9' -> {
                        append(char)
                        capitalizeNext = false
                    }

                    else -> capitalizeNext = true
                }
            }
        }.replaceFirstChar { it.lowercaseChar() }

        return if (factoryName in forbiddenNames) "${factoryName}_" else factoryName
    }

    fun messageClassNameForDslFactory(functionName: String): String {
        return functionName.replaceFirstChar { it.uppercaseChar() }
    }

    fun dslPropertyNameForField(fieldName: String): String {
        val propertyName = NameUtils.underscoreToCamelCase(fieldName)

        return if (propertyName in forbiddenNames) "${propertyName}_" else propertyName
    }

    fun dslPropertyNameForField(field: PbField): String? {
        val fieldName = field.typeName?.shortName
            ?.takeIf { PbPsiUtil.fieldIsGroup(field) }
            ?: field.name
            ?: return null
        val propertyName = dslPropertyNameForField(fieldName)
        if (!hasGeneratedJavaMemberNameCollision(field)) {
            return propertyName
        }

        val fieldNumber = field.fieldNumber?.longValue ?: return propertyName
        return "$propertyName$fieldNumber"
    }

    private fun hasGeneratedJavaMemberNameCollision(field: PbField): Boolean {
        val message = field.symbolOwner as? PbMessageType ?: return false
        val siblingFields = message.getSymbols(PbField::class.java)
            .filter { sibling -> sibling != field }

        return NameGeneratorSelector.selectForFile(field.pbFile).any { generator ->
            val fieldMemberNames = generator.fieldMemberNames(field)
            siblingFields.any { sibling ->
                generator.fieldMemberNames(sibling).any(fieldMemberNames::contains)
            }
        }
    }

    fun builderAccessorNamesForDslField(fieldName: String): List<String> {
        val javaFieldName = fieldName
            .removeSuffix("_")
            .takeIf { it in forbiddenNames }
            ?: fieldName
        val accessorSuffix = javaFieldName.replaceFirstChar { it.uppercaseChar() }

        return listOf(
            "set$accessorSuffix",
            "add$accessorSuffix",
            "get$accessorSuffix",
            "clear$accessorSuffix",
        )
    }

    fun dslPropertyNameForAccessorFunction(functionName: String): String? {
        val accessorSuffix = when {
            functionName.startsWith("has") -> functionName.removePrefix("has")
            functionName.startsWith("clear") -> functionName.removePrefix("clear")
            else -> return null
        }
            .takeIf { it.isNotEmpty() }
            ?: return null

        return accessorSuffix.replaceFirstChar { it.lowercaseChar() }
    }
}
