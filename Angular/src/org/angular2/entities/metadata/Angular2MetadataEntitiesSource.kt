package org.angular2.entities.metadata

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import org.angular2.entities.*
import org.angular2.entities.ivy.Angular2IvyUtil
import org.angular2.entities.metadata.psi.Angular2MetadataDirectiveBase
import org.angular2.entities.metadata.psi.Angular2MetadataEntity
import org.angular2.entities.metadata.psi.Angular2MetadataModule
import org.angular2.entities.metadata.psi.Angular2MetadataPipe
import org.angular2.index.*
import java.util.function.Consumer

class Angular2MetadataEntitiesSource : Angular2EntitiesSource {

  override fun getSupportedEntityPsiElements(): List<Class<out PsiElement>> =
    emptyList()

  override fun getEntity(element: PsiElement): Angular2Entity? =
  // The metadata entity is acquired by Angular2IvyEntitiesSource
    // in a fallback mode
    null

  override fun getAllModules(project: Project): Collection<Angular2Module> {
    val result = ArrayList<Angular2Module>()
    processMetadataEntities(project, Angular2IndexingHandler.NG_MODULE_INDEX_NAME, Angular2MetadataModule::class.java,
                            Angular2MetadataModuleIndexKey) { result.add(it) }
    return result
  }

  override fun getAllPipeNames(project: Project): Collection<String> =
    Angular2IndexUtil.getAllKeys(Angular2MetadataPipeIndexKey, project)

  override fun findDirectiveCandidates(project: Project, indexLookupName: String): List<Angular2Directive> {
    val result = ArrayList<Angular2Directive>()
    processMetadataEntities(project, indexLookupName, Angular2MetadataDirectiveBase::class.java,
                            Angular2MetadataDirectiveIndexKey) { result.add(it) }
    return result
  }

  override fun findTemplateComponent(templateContext: PsiElement): Angular2Component? =
    null

  override fun findPipes(project: Project, name: String): Collection<Angular2Pipe> {
    val result = mutableListOf<Angular2Pipe>()
    processMetadataEntities(project, name, Angular2MetadataPipe::class.java,
                            Angular2MetadataPipeIndexKey) { result.add(it) }
    return result
  }

  private fun <T : Angular2MetadataEntity<*>> processMetadataEntities(project: Project,
                                                                      name: String,
                                                                      entityClass: Class<T>,
                                                                      key: StubIndexKey<String, T>,
                                                                      consumer: Consumer<in T>) {
    StubIndex.getInstance().processElements(key, name, project, GlobalSearchScope.allScope(project), entityClass) { el ->
      if (el.isValid && !Angular2IvyUtil.hasIvyMetadata(el)) {
        consumer.accept(el)
      }
      true
    }
  }

}