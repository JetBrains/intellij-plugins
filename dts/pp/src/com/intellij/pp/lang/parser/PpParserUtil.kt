package com.intellij.pp.lang.parser

import com.intellij.lang.LighterASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.impl.PsiBuilderImpl
import com.intellij.psi.tree.IElementType

object PpParserUtil {
    /**
     * Rolls back all trailing preprocessor statements. Therefore, this function
     * considers all trailing children of the last production and all consecutive
     * preprocessor statements. But does not consider productions after the exit
     * marker. This is useful in case when a rule ends with an error. Like a
     * missing semicolon:
     *
     * property = "value"
     * /include/ "file"
     *
     * Without calling this function the following psi tree would be generated:
     *
     * DTS File
     *   PsiElement(PROPERTY)
     *     ...
     *     PsiElement(INCLUDE)
     *       ...
     *     PsiErrorElement
     *
     * This function rolls back the preprocessor statement at the end and would
     * yield the following psi tree:
     *
     * DTS File
     *   PsiElement(PROPERTY)
     *     ...
     *     PsiErrorElement
     *   PsiElement(INCLUDE)
     *     ...
     *
     * To move the psi error, all preprocessor statements also need to be rolled
     * back when an error is inserted.
     */
    fun rollbackPreprocessorStatements(builder: PpBuildAdapter, exitMarker: PsiBuilder.Marker) {
        val trailingProductions = collectTrailingProductions(builder, exitMarker)

        // collect all markers that need to be adjusted, all markers that are
        // after the preprocessor statement
        val adjustProductions = trailingProductions.dropLastWhile { it.tokenType !in builder.ppStatementsSet }
        if (adjustProductions.isEmpty()) return

        // check if all productions that need to be adjusted are actually marker
        if (adjustProductions.any { it !is PsiBuilder.Marker }) return

        val backups = mutableListOf<Pair<PsiBuilder.Marker, IElementType>?>()
        for (marker in adjustProductions) {
            if (marker.tokenType in builder.ppStatementsSet) {
                (marker as PsiBuilder.Marker).rollbackTo()
            } else {
                backups.add(backupMarker(marker))
            }
        }

        backups.reversed().filterNotNull().forEach { (marker, type) -> marker.done(type) }
    }

    /**
     * Collects all trailing productions that potentially need to be rolled
     * back. Considers all trailing children of the last production and all
     * consecutive preprocessor statements. This includes nested children but
     * does not consider productions after the exit marker.
     *
     * Returns an empty list if a trailing productions is of type node content.
     */
    private fun collectTrailingProductions(builder: PpBuildAdapter, exitMarker: PsiBuilder.Marker): List<PsiBuilderImpl.ProductionMarker> {
        val productions = builder.productions.reversed()

        var last: PsiBuilderImpl.ProductionMarker? = null
        var maxEndIndex = Int.MAX_VALUE

        val result = mutableListOf<PsiBuilderImpl.ProductionMarker>()
        for (current in productions) {
            when {
                // stop collecting if the exit marker was reached
                current == exitMarker -> break

                // scopes should not be adjusted, abort
                current.tokenType in builder.ppScopeSet -> return emptyList()

                // maxEndIndex set when a preprocessor statement was found and if
                // the endIndex is greater this is a child of the preprocessor
                // statement. This child is going to be rolled if the preprocessor
                // statement is rolled back. Therefore, it needs to be skipped.
                // If the last statement was not preprocessor statement (last != null)
                // stop collecting because this is cannot be a child
                current.endIndex > maxEndIndex -> if (last != null) break

                // if the current production is a preprocessor statement, skip all
                // children and consider productions after the statement
                current.tokenType in builder.ppStatementsSet -> {
                    last = null
                    maxEndIndex = current.startIndex

                    result.add(current)
                }

                // check if last is null or if current is a trailing child of last
                last == null || current.startIndex >= last.startIndex && current.endIndex == last.endIndex -> {
                    last = current
                    result.add(current)
                }

                else -> break
            }
        }

        return result
    }

    /**
     * Creates a backup of the given marker and drops the marker. The backup
     * is a preceding marker. Return null if it is not possible to create a
     * backup.
     */
    fun backupMarker(marker: LighterASTNode?): Pair<PsiBuilder.Marker, IElementType>? {
        if (marker !is PsiBuilder.Marker) return null

        val result = Pair(marker.precede(), marker.tokenType)
        marker.drop()

        return result
    }
}