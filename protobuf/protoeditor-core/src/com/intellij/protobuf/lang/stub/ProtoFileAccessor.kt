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
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.CommonProcessors
import com.intellij.util.Processor

@Service
class ProtoFileAccessor(private val project: Project) {

  fun findMethodByFqn(exactFqn: String): PbServiceMethod? {
    return findPbStubElementsOfType<PbServiceMethod>(exactFqn, true).firstOrNull()
  }

  fun findAllMethodsWithFqnPrefix(fqnPrefix: String): Sequence<PbServiceMethod> {
    val effectivePrefix = fqnPrefix.takeIf { it.contains('.') }
    return findPbStubElementsOfType<PbServiceMethod>(effectivePrefix, false)
  }

  fun findServiceByFqn(exactFqn: String): PbServiceDefinition? {
    return findServicesByFqn(exactFqn, true).firstOrNull()
  }

  fun findServicesByFqn(fqnOrPrefix: String, exactMatch: Boolean): Sequence<PbServiceDefinition> {
    return findPbStubElementsOfType<PbServiceDefinition>(fqnOrPrefix, exactMatch)
  }

  fun findMessageByFqn(exactFqn: String): PbMessageDefinition? {
    return findPbStubElementsOfType<PbMessageDefinition>(exactFqn, true).firstOrNull()
  }

  fun findAllServices(): Sequence<PbServiceDefinition> {
    return findPbStubElementsOfType<PbServiceDefinition>()
  }

  private inline fun <reified Type : PbNamedElement> findPbStubElementsOfType(
    namePrefix: String? = null,
    exactMatch: Boolean = namePrefix != null): Sequence<Type> {

    return findAllPbStubElements(namePrefix, exactMatch).filterIsInstance<Type>()
  }

  private fun findAllPbStubElements(fqnPrefix: String?, exactMatch: Boolean): Sequence<PbNamedElement> {
    val collector = fqnAwareCollector(fqnPrefix, exactMatch)
    collectStringsForKey(QualifiedNameIndex.KEY, project, collector)
    collectStringsForKey(ShortNameIndex.KEY, project, collector)

    return collector.results
      .asSequence()
      .flatMap { elementName ->
        return@flatMap if (elementName.contains('.'))
          collectElementsWithText(elementName, QualifiedNameIndex.KEY)
        else
          collectElementsWithText(elementName, QualifiedNameIndex.KEY)
            .plus(collectElementsWithText(elementName, ShortNameIndex.KEY))
      }
  }

  private fun fqnAwareCollector(fqnPrefix: String?, exactMatch: Boolean): CommonProcessors.CollectProcessor<String> {
    return object : CommonProcessors.CollectProcessor<String>() {
      override fun accept(value: String?): Boolean {
        return when {
          value.isNullOrBlank() -> false
          fqnPrefix.isNullOrBlank() -> true
          !exactMatch && value.startsWith(fqnPrefix) -> true
          exactMatch && value == fqnPrefix -> true
          else -> false
        }
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