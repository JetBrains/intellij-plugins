package name.kropp.intellij.makefile

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import name.kropp.intellij.makefile.psi.MakefileTypes

class MakefileCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC,
        PlatformPatterns.psiElement(MakefileTypes.DEPENDENCY).withLanguage(MakefileLanguage),
        object : CompletionProvider<CompletionParameters>() {
      override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, resultSet: CompletionResultSet) {
        val file = parameters.originalFile
        if (file is MakefileFile) {
          resultSet.addAllElements(file.targets.map { LookupElementBuilder.create(it) })
        }
      }
    })
  }
}