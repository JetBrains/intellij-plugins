package com.jetbrains.lang.makefile

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.patterns.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.*
import com.jetbrains.lang.makefile.psi.MakefileTypes.*

class MakefileCompletionContributor : CompletionContributor() {
  private object KeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
    private val keywords = listOf("include", "define", "undefine", "override", "export", "private", "vpath", "ifeq", "ifneq", "ifdef", "ifndef")
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet) {
      resultSet.addAllElements(keywords.map { LookupElementBuilder.create(it) })
    }
  }

  private object FunctionCompletionProvider : CompletionProvider<CompletionParameters>() {
    private val functions = listOf("error", "warning", "info", "shell", "subst", "patsubst", "strip", "findstring",
      "filter", "filter-out", "sort", "word", "wordlist", "words", "firstword", "lastword", "dir", "notdir", "suffix",
      "basename", "addsuffix", "addprefix", "join", "wildcard", "realpath", "abspath", "if", "or", "and",
      "foreach", "file", "call", "value", "eval", "origin", "flavor", "guile")
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet) {
      resultSet.addAllElements(functions.map { LookupElementBuilder.create(it) })
    }
  }

  init {
    extend(CompletionType.BASIC, StandardPatterns.or(psiElement().afterLeaf(psiElement(EOL)), psiElement().isNull), KeywordCompletionProvider)
    extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement(OPEN_PAREN).afterLeafSkipping(psiElement(OPEN_PAREN), psiElement(DOLLAR))), FunctionCompletionProvider)
  }
}