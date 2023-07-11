// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.ecmascript6.psi.ES6ImportExportSpecifierAlias
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptFunctionCachingVisitor
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.impl.JSFunctionBaseImpl
import com.intellij.lang.javascript.psi.impl.JSFunctionCachedDataBuilder
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.AstLoadingFilter
import com.intellij.util.ObjectUtils.tryCast
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.util.containers.ContainerUtil.addIfNotNull
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2Entity
import org.angular2.entities.Angular2Module
import org.angular2.entities.Angular2ModuleResolver.Companion.MODULE_WITH_PROVIDERS_CLASS
import org.angular2.entities.Angular2ModuleResolver.Companion.NG_MODULE_PROP
import org.angular2.entities.ivy.Angular2IvyUtil.getIvyEntity
import org.angular2.entities.metadata.Angular2MetadataUtil
import org.angular2.entities.metadata.psi.Angular2MetadataFunction
import org.angular2.entities.metadata.psi.Angular2MetadataModule
import org.angular2.entities.metadata.psi.Angular2MetadataObject
import org.angular2.entities.metadata.psi.Angular2MetadataReference

abstract class Angular2SourceEntityListProcessor<T : Angular2Entity>(private val myAcceptableEntityClass: Class<T>) {

  private val myAcceptNgModuleWithProviders: Boolean = myAcceptableEntityClass.isAssignableFrom(Angular2Module::class.java)

  protected //it's ok, if array does not have any children
  val resultsVisitor: JSElementVisitor = object : JSElementVisitor() {
    override fun visitJSClass(aClass: JSClass) {
      val entity = getAcceptableEntity(aClass)
      if (entity != null) {
        processAcceptableEntity(entity)
      }
      else {
        processNonAcceptableEntityClass(aClass)
      }
    }

    override fun visitJSArrayLiteralExpression(node: JSArrayLiteralExpression) {}

    override fun visitJSFunctionDeclaration(node: JSFunction) {
      resolveFunctionReturnType(node)
    }

    override fun visitJSFunctionExpression(node: JSFunctionExpression) {
      resolveFunctionReturnType(node)
    }

    override fun visitJSElement(node: JSElement) {
      processAnyElement(node)
    }
  }

  protected fun resolve(t: PsiElement): List<PsiElement> {
    val result = SmartList<PsiElement>()
    t.accept(createResolveVisitor(result))
    return result
  }

  private fun createResolveVisitor(result: SmartList<PsiElement>): JSElementVisitor {
    return object : JSElementVisitor() {
      override fun visitJSArrayLiteralExpression(node: JSArrayLiteralExpression) {
        result.addAll(listOf(*node.expressions))
      }

      override fun visitJSObjectLiteralExpression(node: JSObjectLiteralExpression) {
        if (myAcceptNgModuleWithProviders) {
          AstLoadingFilter.forceAllowTreeLoading<RuntimeException>(node.containingFile) {
            addIfNotNull(result, node.findProperty(NG_MODULE_PROP)?.value)
          }
        } else {
          super.visitJSObjectLiteralExpression(node)
        }
      }

      override fun visitJSReferenceExpression(node: JSReferenceExpression) {
        addIfNotNull(result, node.resolve())
      }

      override fun visitJSVariable(node: JSVariable) {
        AstLoadingFilter.forceAllowTreeLoading<RuntimeException>(node.containingFile) { addIfNotNull(result, node.initializer) }
      }

      override fun visitJSProperty(node: JSProperty) {
        AstLoadingFilter.forceAllowTreeLoading<RuntimeException>(node.containingFile) { addIfNotNull(result, node.value) }
      }

      override fun visitES6ImportExportSpecifierAlias(alias: ES6ImportExportSpecifierAlias) {
        addIfNotNull(result, alias.findAliasedElement())
      }

      override fun visitJSSpreadExpression(spreadExpression: JSSpreadExpression) {
        AstLoadingFilter.forceAllowTreeLoading<RuntimeException>(spreadExpression.containingFile) {
          addIfNotNull(result, spreadExpression.expression)
        }
      }

      override fun visitJSConditionalExpression(node: JSConditionalExpression) {
        AstLoadingFilter.forceAllowTreeLoading<RuntimeException>(node.containingFile) {
          addIfNotNull(result, node.thenBranch)
          addIfNotNull(result, node.elseBranch)
        }
      }

      override fun visitJSCallExpression(node: JSCallExpression) {
        addIfNotNull(result, node.stubSafeMethodExpression)
      }

      override fun visitJSFunctionDeclaration(node: JSFunction) {
        collectFunctionReturningArrayItems(node)
      }

      override fun visitJSFunctionExpression(node: JSFunctionExpression) {
        collectFunctionReturningArrayItems(node)
      }

      private fun collectFunctionReturningArrayItems(function: JSFunction) {
        val type = function.returnType
        if (JSTypeUtils.isArrayLikeType(type)) {
          // None of the required information is stubbed here
          AstLoadingFilter.forceAllowTreeLoading<RuntimeException>(function.containingFile
          ) {
            function.acceptChildren(object : JSElementVisitor() {
              override fun visitJSReturnStatement(node: JSReturnStatement) {
                addIfNotNull(result, node.expression)
              }

              override fun visitJSStatement(node: JSStatement) {
                node.acceptChildren(this)
              }
            })
          }
        }
      }
    }
  }

  /**
   * @see .getAcceptableEntity
   */
  protected open fun processNonAcceptableEntityClass(aClass: JSClass) {

  }

  /**
   * @see .getAcceptableEntity
   */
  protected open fun processAcceptableEntity(entity: T) {

  }

  protected open fun processAnyType() {

  }

  protected open fun processAnyElement(node: JSElement) {

  }

  /**
   * Implementations can store the `element` and pass it later to [CachedValueProvider.Result.create]
   */
  protected open fun processCacheDependency(element: PsiElement) {

  }

  private fun getAcceptableEntity(aClass: JSClass): T? {
    return tryCast(Angular2EntitiesProvider.getEntity(aClass), myAcceptableEntityClass)
  }

  private fun resolveFunctionReturnType(function: JSFunction) {
    val visitedTypes = HashSet<JSResolvedTypeId>()
    var lookingForModule = myAcceptNgModuleWithProviders
    var resolvedClazz: JSClass? = null
    var type = function.returnType
    while (type != null
           && type !is JSAnyType
           && visitedTypes.add(type.resolvedTypeId)) {
      val recordType = NotNullLazyValue.createValue<JSRecordType> { type!!.asRecordType() }
      type.sourceElement
        ?.let { processCacheDependency(it) }
      if (lookingForModule) { // see https://angular.io/guide/migration-module-with-providers
        // Ivy syntax
        if (type is JSGenericTypeImpl
            && type.type is JSTypeImpl
            && MODULE_WITH_PROVIDERS_CLASS == type.type.typeText) {
          val argument = type.arguments.firstOrNull()
          if (argument != null && argument !is JSAnyType) {
            type = argument
            lookingForModule = false
            continue
          }
        }
        // pre-Ivy syntax
        val ngModuleSignature = recordType.value.findPropertySignature(NG_MODULE_PROP)
        if (ngModuleSignature != null) {
          type = evaluateModuleWithProvidersType(ngModuleSignature, type.source)
          lookingForModule = false
          continue
        }
      }
      val sourceElement = type.sourceElement
      if (sourceElement is TypeScriptSingleType) {
        val expression = AstLoadingFilter.forceAllowTreeLoading<JSReferenceExpression, RuntimeException>(sourceElement.containingFile) {
          sourceElement.referenceExpression
        }
        if (expression != null) {
          resolvedClazz = expression.resolve() as? JSClass
          val entity = resolvedClazz?.let { getAcceptableEntity(it) }
          if (entity != null && resolvedClazz != null) {
            processCacheDependency(resolvedClazz)
            processAcceptableEntity(entity)
            return
          }
        }
      }
      else if (sourceElement is TypeScriptClass) {
        resolvedClazz = sourceElement
        val entity = getAcceptableEntity(resolvedClazz)
        if (entity != null) {
          processCacheDependency(resolvedClazz)
          processAcceptableEntity(entity)
          return
        }
      }
      type = recordType.value.callSignatures.find { it.hasNew() }?.returnType
    }
    // Fallback to search in metadata
    if (myAcceptNgModuleWithProviders) {
      val metadataFunction = Angular2MetadataUtil.findMetadataFunction(function)
      val metadataModule = metadataFunction?.let { resolveFunctionValue(it) }
      if (metadataModule != null) {
        processCacheDependency(metadataFunction)
        processCacheDependency(metadataModule)
        // Make sure we translate to Ivy module if available
        val tsClass = metadataModule.typeScriptClass
        if (tsClass != null) {
          val ivyModule = getIvyEntity(tsClass) as? Angular2Module
          if (ivyModule != null) {
            processCacheDependency(tsClass)
            @Suppress("UNCHECKED_CAST")
            processAcceptableEntity(ivyModule as T)
            return
          }
        }

        @Suppress("UNCHECKED_CAST")
        processAcceptableEntity(metadataModule as T)
        return
      }
    }
    if (resolvedClazz != null && type !is JSAnyType) {
      processNonAcceptableEntityClass(resolvedClazz)
    }
    else {
      processAnyType()
    }
  }

  private fun evaluateModuleWithProvidersType(ngModuleSignature: JSRecordType.PropertySignature,
                                              functionTypeSource: JSTypeSource): JSType? {
    val result = ngModuleSignature.jsType
    if (result !is JSGenericTypeImpl) return result

    val args: List<JSType> = result.arguments
    if (args.size != 1 || args[0] !is JSAnyType)
      return result
    val function: JSFunctionBaseImpl<*> = functionTypeSource.sourceElement?.context as? JSFunctionBaseImpl<*>
                                          ?: return result

    val evaluatedReturnType = CachedValuesManager.getCachedValue(function) {
      val cachedData = JSFunctionCachedDataBuilder()
      val nestedFuns = SmartList<JSFunction>()
      val cachedDataEvaluator = TypeScriptFunctionCachingVisitor(function, cachedData, nestedFuns)
      AstLoadingFilter.forceAllowTreeLoading<RuntimeException>(function.containingFile) {
        cachedDataEvaluator.visitElement(function.node)
      }
      CachedValueProvider.Result.create(cachedDataEvaluator.returnTypeFromEvaluated, function)
    }
    return evaluatedReturnType?.asRecordType()?.findPropertySignature(NG_MODULE_PROP)?.jsType
  }

  private fun resolveFunctionValue(function: Angular2MetadataFunction): Angular2MetadataModule? {
    return function.value?.asSafely<Angular2MetadataObject>()
      ?.findMember(NG_MODULE_PROP)?.asSafely<Angular2MetadataReference>()
      ?.resolve() as? Angular2MetadataModule
  }
}
