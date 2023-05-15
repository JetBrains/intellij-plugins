package com.intellij.protobuf.lang.stub

import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.protobuf.lang.psi.PbMessageDefinition
import com.intellij.protobuf.lang.psi.PbNamedElement
import com.intellij.protobuf.lang.psi.PbServiceDefinition
import com.intellij.protobuf.lang.psi.PbServiceMethod
import com.intellij.protobuf.lang.stub.index.QualifiedNameIndex
import com.intellij.protobuf.lang.stub.index.ShortNameIndex
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.CommonProcessors
import com.intellij.util.Processor

@Service(Service.Level.PROJECT)
class ProtoFileAccessor(private val project: Project) {

  fun findMethodByFqn(exactFqn: String): PbServiceMethod? {
    return findPbStubElementsOfType<PbServiceMethod>(exactFqn, PbSearchParameters.EXACT_MATCH).firstOrNull()
  }

  fun findAllMethodsWithFqnPrefix(fqnPrefix: String): Sequence<PbServiceMethod> {
    val effectivePrefix = fqnPrefix.takeIf { it.contains('.') }
    return findPbStubElementsOfType<PbServiceMethod>(effectivePrefix, PbSearchParameters.PREFIX)
  }

  fun findServiceByFqn(exactFqn: String): PbServiceDefinition? {
    return findServicesByFqn(exactFqn, PbSearchParameters.EXACT_MATCH).firstOrNull()
  }

  fun findServicesByFqn(fqnOrPrefix: String, searchParameters: PbSearchParameters): Sequence<PbServiceDefinition> {
    return findPbStubElementsOfType<PbServiceDefinition>(fqnOrPrefix, searchParameters)
  }

  fun findMessageByFqn(exactFqn: String): PbMessageDefinition? {
    return findPbStubElementsOfType<PbMessageDefinition>(exactFqn, PbSearchParameters.EXACT_MATCH).firstOrNull()
  }

  fun findAllServices(): Sequence<PbServiceDefinition> {
    return findPbStubElementsOfType<PbServiceDefinition>()
  }

  private inline fun <reified Type : PbNamedElement> findPbStubElementsOfType(
    namePrefix: String? = null,
    searchParameters: PbSearchParameters = PbSearchParameters.EXACT_MATCH): Sequence<Type> {

    return findAllPbStubElements(namePrefix, searchParameters).filterIsInstance<Type>()
  }

  private fun findAllPbStubElements(fqnPrefix: String?, searchParameters: PbSearchParameters): Sequence<PbNamedElement> {
    val collector = fqnAwareCollector(fqnPrefix, searchParameters)
    collectStringsForKey(QualifiedNameIndex.KEY, project, collector)
    collectStringsForKey(ShortNameIndex.KEY, project, collector)
    val psiManager = PsiManager.getInstance(project)

    return collector.results
      .asSequence()
      .flatMap { elementName ->
        return@flatMap if (elementName.contains('.'))
          collectElementsWithText(elementName, QualifiedNameIndex.KEY)
        else
          collectElementsWithText(elementName, ShortNameIndex.KEY)
            .plus(collectElementsWithText(elementName, QualifiedNameIndex.KEY))
      }
      .groupBy { it.qualifiedName?.toString() ?: it.name }
      .asSequence()
      .map { possiblyDuplicates -> selectUnique(possiblyDuplicates, psiManager) }
      .flatten()
  }

  private fun selectUnique(it: Map.Entry<String?, List<PbNamedElement>>,
                           psiManager: PsiManager): MutableList<PbNamedElement> {
    return it.value.fold(mutableListOf()) { accumulator, candidateElement ->
      val noDuplicates = accumulator.none { uniqueElement -> psiManager.areElementsEquivalent(uniqueElement, candidateElement) }
      if (noDuplicates) accumulator.add(candidateElement)
      accumulator
    }
  }

  private fun fqnAwareCollector(fqnSegment: String?, searchParameters: PbSearchParameters): CommonProcessors.CollectProcessor<String> {
    return object : CommonProcessors.CollectProcessor<String>() {
      private val knownValues = mutableSetOf<String?>()

      override fun accept(value: String?): Boolean {
        val isAccepted = when {
          knownValues.contains(value) -> false
          value.isNullOrBlank() -> false
          fqnSegment.isNullOrBlank() -> true
          searchParameters == PbSearchParameters.PREFIX && value.startsWith(fqnSegment) -> true
          searchParameters == PbSearchParameters.SUFFIX && value.endsWith(fqnSegment) -> true
          searchParameters == PbSearchParameters.EXACT_MATCH && value == fqnSegment -> true
          searchParameters == PbSearchParameters.CONTAINS && value.contains(fqnSegment) -> true
          else -> false
        }

        if (isAccepted) {
          knownValues.add(value)
        }
        return isAccepted
      }
    }
  }

  private fun collectStringsForKey(key: StubIndexKey<String, PbNamedElement>, project: Project, collector: Processor<String>) {
    ProgressManager.checkCanceled()
    StubIndex.getInstance().processAllKeys(key, project, collector)
  }

  private fun collectElementsWithText(text: String, key: StubIndexKey<String, PbNamedElement>): Sequence<PbNamedElement> {
    ProgressManager.checkCanceled()
    return StubIndex.getElements(
      key,
      text,
      project,
      GlobalSearchScope.projectScope(project),
      PbNamedElement::class.java
    ).asSequence()
  }
}

enum class PbSearchParameters {
  EXACT_MATCH,
  PREFIX,
  SUFFIX,
  CONTAINS
}