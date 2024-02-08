package org.angular2.entities.ivy

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.ObjectUtils
import org.angular2.entities.*
import org.angular2.entities.metadata.Angular2MetadataUtil
import org.angular2.index.*
import java.util.function.Consumer

class Angular2IvyEntitiesSource : Angular2EntitiesSource {

  override fun getSupportedEntityPsiElements(): List<Class<out PsiElement>> =
    listOf(TypeScriptClass::class.java, TypeScriptField::class.java)

  override fun getEntity(element: PsiElement): Angular2Entity? =
    Angular2IvyUtil.withJsonMetadataFallback(
      element, { Angular2IvyUtil.getIvyEntity(it) },
      { Angular2MetadataUtil.getMetadataEntity(it) }
    )

  override fun getAllModules(project: Project): Collection<Angular2Module> {
    val result = ArrayList<Angular2Module>()
    processIvyEntities(project, Angular2IndexingHandler.NG_MODULE_INDEX_NAME, Angular2IvyModuleIndexKey,
                       Angular2Module::class.java) { result.add(it) }
    return result
  }

  override fun getAllPipeNames(project: Project): Collection<String> =
    Angular2IndexUtil.getAllKeys(Angular2IvyPipeIndexKey, project)

  override fun findDirectiveCandidates(project: Project, indexLookupName: String): List<Angular2Directive> {
    val result = ArrayList<Angular2Directive>()
    processIvyEntities(project, indexLookupName, Angular2IvyDirectiveIndexKey, Angular2Directive::class.java) { result.add(it) }
    return result
  }

  override fun findTemplateComponent(templateContext: PsiElement): Angular2Component? =
    null

  override fun findPipes(project: Project, name: String): Collection<Angular2Pipe> {
    val result = mutableListOf<Angular2Pipe>()
    processIvyEntities(project, name, Angular2IvyPipeIndexKey, Angular2Pipe::class.java) { result.add(it) }
    return result
  }

  private fun <T : Angular2Entity> processIvyEntities(project: Project,
                                                      name: String,
                                                      key: StubIndexKey<String, TypeScriptClass>,
                                                      entityClass: Class<T>,
                                                      consumer: Consumer<in T>) {
    StubIndex.getInstance().processElements(key, name, project, GlobalSearchScope.allScope(project), TypeScriptClass::class.java) { el ->
      if (el.isValid) {
        val entity = ObjectUtils.tryCast(Angular2IvyUtil.getIvyEntity(el), entityClass)
        if (entity != null) {
          consumer.accept(entity)
        }
      }
      true
    }
  }
}