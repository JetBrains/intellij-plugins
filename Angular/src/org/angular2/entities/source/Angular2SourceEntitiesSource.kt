package org.angular2.entities.source

import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.ecma6.*
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList.ModifierType
import com.intellij.lang.javascript.psi.stubs.JSFrameworkMarkersIndex
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.stubSafeChildren
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.util.containers.addIfNotNull
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.*
import org.angular2.index.*

class Angular2SourceEntitiesSource : Angular2EntitiesSource {

  override fun getSupportedEntityPsiElements(): List<Class<out PsiElement>> =
    listOf(JSImplicitElement::class.java, ES6Decorator::class.java, TypeScriptClass::class.java, TypeScriptVariable::class.java)

  override fun getEntity(element: PsiElement): Angular2Entity? =
    getSourceEntity(element)

  override fun getAllModules(project: Project): Collection<Angular2Module> {
    val result = ArrayList<Angular2Module>()
    StubIndex.getInstance().processElements(Angular2SourceModuleIndexKey, Angular2IndexingHandler.NG_MODULE_INDEX_NAME,
                                            project, GlobalSearchScope.allScope(project),
                                            JSImplicitElementProvider::class.java) { module ->
      if (module.isValid) {
        result.addIfNotNull(getSourceEntity(module) as? Angular2Module)
      }
      true
    }
    StubIndex.getInstance().processElements(JSFrameworkMarkersIndex.KEY, Angular2IndexingHandler.NG_PSEUDO_MODULE_DECLARATION_MARKER,
                                            project, GlobalSearchScope.allScope(project),
                                            PsiElement::class.java) { variable ->
      if (variable is TypeScriptVariable && variable.isValid) {
        result.addIfNotNull(getSourceEntity(variable) as? Angular2Module)
      }
      true
    }
    return result
  }

  override fun getAllPipeNames(project: Project): Collection<String> =
    Angular2IndexUtil.getAllKeys(Angular2SourcePipeIndexKey, project)

  override fun findDirectiveCandidates(project: Project, indexLookupName: String): List<Angular2Directive> {
    val result = ArrayList<Angular2Directive>()
    StubIndex.getInstance().processElements(
      Angular2SourceDirectiveIndexKey, indexLookupName, project, GlobalSearchScope.allScope(project),
      JSImplicitElementProvider::class.java
    ) { provider ->
      provider.indexingData
        ?.implicitElements
        ?.filter { it.isValid }
        ?.firstNotNullOfOrNull { getSourceEntity(it) as? Angular2Directive }
        ?.let { directive ->
          result.add(directive)
        }
      true
    }
    return result
  }

  override fun findTemplateComponent(templateContext: PsiElement): Angular2Component? =
    Angular2SourceUtil.findComponentClass(templateContext)
      ?.let { Angular2EntitiesProvider.getComponent(it) }

  override fun findPipes(project: Project, name: String): Collection<Angular2Pipe> {
    val result = SmartList<Angular2Pipe>()
    Angular2IndexUtil.multiResolve(project, Angular2SourcePipeIndexKey, name) { pipe ->
      result.addIfNotNull(getSourceEntity(pipe) as? Angular2Pipe)
      true
    }
    return result
  }

  private fun getSourceEntity(element: PsiElement): Angular2Entity? {
    var elementToCheck: PsiElement? = element
    if (elementToCheck is JSImplicitElement) {
      if (!isEntityImplicitElement(elementToCheck)) {
        return null
      }
      elementToCheck = elementToCheck.getContext()
    }
    if (elementToCheck is TypeScriptClass) {
      elementToCheck = Angular2DecoratorUtil.findDecorator(elementToCheck, Angular2DecoratorUtil.PIPE_DEC,
                                                           Angular2DecoratorUtil.COMPONENT_DEC, Angular2DecoratorUtil.MODULE_DEC,
                                                           Angular2DecoratorUtil.DIRECTIVE_DEC)
      if (elementToCheck == null) {
        return null
      }
    }
    else if (elementToCheck is TypeScriptVariable && elementToCheck !is TypeScriptField) {
      return tryGetStandalonePseudoModule(elementToCheck)
    }
    else if (elementToCheck == null
             || elementToCheck !is ES6Decorator
             || !Angular2DecoratorUtil.isAngularEntityDecorator(elementToCheck, Angular2DecoratorUtil.PIPE_DEC,
                                                                Angular2DecoratorUtil.COMPONENT_DEC, Angular2DecoratorUtil.MODULE_DEC,
                                                                Angular2DecoratorUtil.DIRECTIVE_DEC)) {
      return null
    }
    val dec = elementToCheck as ES6Decorator
    return CachedValuesManager.getCachedValue(dec) {
      val entity: Angular2SourceEntity? =
        dec.indexingData
          ?.implicitElements
          ?.find { isEntityImplicitElement(it) }
          ?.let { entityElement ->
            when (dec.decoratorName) {
              Angular2DecoratorUtil.COMPONENT_DEC -> Angular2SourceComponent(dec, entityElement)
              Angular2DecoratorUtil.DIRECTIVE_DEC -> Angular2SourceDirective(dec, entityElement)
              Angular2DecoratorUtil.MODULE_DEC -> Angular2SourceModule(dec, entityElement)
              Angular2DecoratorUtil.PIPE_DEC -> Angular2SourcePipe(dec, entityElement)
              else -> null
            }
          }
      CachedValueProvider.Result.create(entity, dec)
    }
  }

  private fun isEntityImplicitElement(element: JSImplicitElement): Boolean {
    return element.isDirective() || element.isPipe() || element.isModule()
  }

  private fun tryGetStandalonePseudoModule(variable: TypeScriptVariable): Angular2Module? {
    val attributeList = variable.attributeList
    if (attributeList == null
        || !variable.isConst
        || !attributeList.hasModifier(ModifierType.EXPORT))
      return null

    if (
      variable.typeElement
        ?.asSafely<TypeScriptTupleType>()
        ?.members
        ?.all { it.tupleMemberName == null && it.tupleMemberType is TypeScriptTypeofType } == true
      || variable.initializerOrStub
        ?.asSafely<TypeScriptAsExpression>()
        ?.stubSafeChildren
        ?.let { children -> children.any { it is JSArrayLiteralExpression } && children.any { it is TypeScriptConstType } } == true
      || variable.initializerOrStub
        ?.asSafely<JSArrayLiteralExpression>() != null) {
      return Angular2SourceStandalonePseudoModule(variable)
    }
    return null;
  }

}