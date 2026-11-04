// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.javascript.JSTypeEngineEvaluation.forceTSKotlinTypeEngine
import com.intellij.javascript.typeEngine.JSTypeEngineMode
import com.intellij.javascript.typeEngine.getJSType
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.psi.ES6ImportExportSpecifierAlias
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSConditionalExpression
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSFunctionExpression
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSResolvedTypeId
import com.intellij.lang.javascript.psi.JSReturnStatement
import com.intellij.lang.javascript.psi.JSSpreadExpression
import com.intellij.lang.javascript.psi.JSStatement
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecma6.TypeScriptCastExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTupleType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeofType
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeImpl
import com.intellij.lang.javascript.psi.types.typescript.TypeScriptCompilerType
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.util.AstLoadingFilter
import com.intellij.util.ObjectUtils.tryCast
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.util.containers.ContainerUtil.addIfNotNull
import com.intellij.util.containers.addIfNotNull
import org.angular2.Angular2DecoratorUtil.isForwardRefCall
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

abstract class Angular2SourceEntityListProcessor<T : Angular2Entity>(
  private val myAcceptableEntityClass: Class<T>,
  private val location: PsiElement?,
) {

  private val myAcceptNgModuleWithProviders: Boolean = myAcceptableEntityClass.isAssignableFrom(Angular2Module::class.java)

  protected val resultsVisitor: JSElementVisitor = object : JSElementVisitor() {
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

    override fun visitJSVariable(node: JSVariable) {
      val entity = getAcceptableEntity(node)
      if (entity != null) {
        processAcceptableEntity(entity)
      }
    }

    override fun visitElement(node: PsiElement) {
      val entity = getAcceptableEntity(node)
      if (entity != null) {
        processAcceptableEntity(entity)
      }
      else {
        processAnyElement(node)
      }
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
        }
        else {
          super.visitJSObjectLiteralExpression(node)
        }
      }

      override fun visitJSReferenceExpression(node: JSReferenceExpression) {
        addIfNotNull(result, node.resolve())
      }

      override fun visitJSVariable(node: JSVariable) {
        if (getAcceptableEntity(node) != null) return
        AstLoadingFilter.forceAllowTreeLoading<RuntimeException>(node.containingFile) { addIfNotNull(result, node.initializer) }
      }

      override fun visitJSProperty(node: JSProperty) {
        AstLoadingFilter.forceAllowTreeLoading<RuntimeException>(node.containingFile) { addIfNotNull(result, node.value) }
      }

      override fun visitES6ImportExportSpecifierAlias(alias: ES6ImportExportSpecifierAlias) {
        addIfNotNull(result, alias.findAliasedElement())
      }

      override fun visitES6ImportedBinding(importedBinding: ES6ImportedBinding) {
        result.addAll(importedBinding.findReferencedElements())
      }

      override fun visitES6ExportDefaultAssignment(node: ES6ExportDefaultAssignment) {
        result.addIfNotNull(node.stubSafeElement)
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
        if (isForwardRefCall(node)) {
          addIfNotNull(result, node.stubSafeCallArguments.firstOrNull())
        }
        else {
          addIfNotNull(result, node.stubSafeMethodExpression)
        }
      }

      override fun visitJSFunctionDeclaration(node: JSFunction) {
        collectFunctionReturningArrayItems(node)
      }

      override fun visitJSFunctionExpression(node: JSFunctionExpression) {
        collectFunctionReturningArrayItems(node)
      }

      private fun collectFunctionReturningArrayItems(function: JSFunction) {
        if (getReturnTypes(function).any { JSTypeUtils.isArrayLikeType(it) }) {
          result.addAll(getReturnedExpressions(function))
        }
      }

      override fun visitTypeScriptCastExpression(node: TypeScriptCastExpression) {
        if (node.isConstCast) {
          result.add(node.expression)
        }
      }

      override fun visitTypeScriptTupleType(tupleType: TypeScriptTupleType) {
        tupleType.members.forEach {
          if (it.tupleMemberName == null)
            result.add(it.tupleMemberType)
        }
      }

      override fun visitTypeScriptTypeOfType(typeofType: TypeScriptTypeofType) {
        result.addIfNotNull(typeofType.expression)
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

  protected open fun processAnyElement(node: PsiElement) {

  }

  /**
   * Implementations can store the `element` and pass it later to [CachedValueProvider.Result.create]
   */
  protected open fun processCacheDependency(element: PsiElement) {

  }

  private fun getAcceptableEntity(element: PsiElement): T? {
    return tryCast(Angular2EntitiesProvider.getEntity(element), myAcceptableEntityClass)
  }

  /** The template transpiler uses the resolved entities, so the types come from a program without Kolar. */
  private fun resolveFunctionReturnType(function: JSFunction): Unit = forceTSKotlinTypeEngine(disableKolar = true) {
    var resolvedClazz: JSClass? = null
    var type: JSType? = null
    for (returnType in getReturnTypes(function)) {
      val visitedTypes = HashSet<JSResolvedTypeId>()
      var lookingForModule = myAcceptNgModuleWithProviders
      type = returnType
      while (type != null) {
        // A type from the service, for example from TSKotlin, gets its source element from substitution.
        if (type is TypeScriptCompilerType) type = type.substitute()
        if (type is JSAnyType || !visitedTypes.add(type.resolvedTypeId)) break
        val recordType = NotNullLazyValue.createValue<JSRecordType> { type!!.asRecordType(location) }
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
            type = evaluateModuleWithProvidersType(ngModuleSignature, function)
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
            if (entity != null) {
              processCacheDependency(resolvedClazz)
              processAcceptableEntity(entity)
              return@forceTSKotlinTypeEngine
            }
          }
        }
        else if (sourceElement is TypeScriptClass) {
          resolvedClazz = sourceElement
          val entity = getAcceptableEntity(resolvedClazz)
          if (entity != null) {
            processCacheDependency(resolvedClazz)
            processAcceptableEntity(entity)
            return@forceTSKotlinTypeEngine
          }
        }
        type = recordType.value.callSignatures.find { it.hasNew() }?.returnType
      }
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
            return@forceTSKotlinTypeEngine
          }
        }

        @Suppress("UNCHECKED_CAST")
        processAcceptableEntity(metadataModule as T)
        return@forceTSKotlinTypeEngine
      }
    }
    if (resolvedClazz != null && type !is JSAnyType) {
      processNonAcceptableEntityClass(resolvedClazz)
    }
    else {
      processAnyType()
    }
  }

  /**
   * The type of the module of a `ModuleWithProviders` result of [function].
   * The type `ModuleWithProviders<any>` does not name the module, so then the types of the returned expressions name it.
   */
  private fun evaluateModuleWithProvidersType(
    ngModuleSignature: JSRecordType.PropertySignature,
    function: JSFunction,
  ): JSType? {
    val result = ngModuleSignature.jsType
    if (result !is JSGenericTypeImpl) return result

    val args: List<JSType> = result.arguments
    if (args.size != 1 || args[0] !is JSAnyType)
      return result

    return getReturnedExpressions(function).firstNotNullOfOrNull { expression ->
      expression.getJSType(JSTypeEngineMode.Service)
        ?.asRecordType(location)
        ?.findPropertySignature(NG_MODULE_PROP)
        ?.jsType
    }
  }

  private fun resolveFunctionValue(function: Angular2MetadataFunction): Angular2MetadataModule? {
    return function.value?.asSafely<Angular2MetadataObject>()
      ?.findMember(NG_MODULE_PROP)?.asSafely<Angular2MetadataReference>()
      ?.resolve() as? Angular2MetadataModule
  }
}

/**
 * The return types of the call signatures of [function], from the service-powered type engine.
 * The caller decides on the program, for example with [forceTSKotlinTypeEngine].
 */
private fun getReturnTypes(function: JSFunction): List<JSType> {
  val type = function.getJSType(JSTypeEngineMode.Service) ?: return emptyList()
  val returnTypes = if (type is JSFunctionType)
    listOfNotNull(type.returnType)
  else
    type.asRecordType(function).callSignatures.mapNotNull { it.returnType }
  // A type from the service, for example from TSKotlin, gets its source element and its shape from substitution.
  return returnTypes.map { if (it is TypeScriptCompilerType) it.substitute() else it }
}

/** The expressions of the `return` statements of [function], without the statements of nested functions. */
private fun getReturnedExpressions(function: JSFunction): List<JSExpression> {
  val result = SmartList<JSExpression>()
  // None of the required information is stubbed here
  AstLoadingFilter.forceAllowTreeLoading<RuntimeException>(function.containingFile) {
    function.acceptChildren(object : JSElementVisitor() {
      override fun visitJSReturnStatement(node: JSReturnStatement) {
        addIfNotNull(result, node.expression)
      }

      override fun visitJSStatement(node: JSStatement) {
        node.acceptChildren(this)
      }
    })
  }
  return result
}
