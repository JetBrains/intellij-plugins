package com.intellij.protobuf.python.types

import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.protobuf.lang.psi.PbSymbol
import com.intellij.protobuf.lang.resolve.PbSymbolLookupElement
import com.intellij.protobuf.python.PbPythonProtoUtils.resolveInProto
import com.intellij.protobuf.python.PbPythonSourceContext
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.QualifiedName
import com.intellij.util.ProcessingContext
import com.intellij.util.Processor
import com.jetbrains.python.PyNames
import com.jetbrains.python.psi.AccessDirection
import com.jetbrains.python.psi.PyCallSiteOwner
import com.jetbrains.python.psi.PyElement
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.impl.PyBuiltinCache
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.resolve.RatedResolveResult
import com.jetbrains.python.psi.types.PyClassLikeType
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.TypeEvalContext
import java.util.Objects

internal abstract class PbPythonAbstractType<E : PbElement>(
  pbElement: E?,
  val source: PbPythonSourceContext,
  val localQn: QualifiedName,
  private val isDefinitionFlag: Boolean,
  pyAnchor: PyElement?,
) : PyClassLikeType {

  protected abstract val typeName: String

  // --- Pointer to PbElement ---
  private var pbElementPointer = pbElement?.createSmartPointer()

  protected fun getPbElement(typeFilter: (PbElement) -> E?): E? {
    val current = pbElementPointer?.element
    if (current?.isValid == true) return current
    val restored = resolveInProto(source, localQn).firstNotNullOfOrNull(typeFilter) ?: return null
    pbElementPointer = restored.createSmartPointer()
    return restored
  }

  internal abstract val pbElement: E?

  // --- Python Anchor ---
  private var pyAnchorPointer = pyAnchor?.createSmartPointer()

  protected fun getPyAnchor(context: TypeEvalContext? = null): PyElement? {
    val current = pyAnchorPointer?.element
    if (current?.isValid == true) return current
    val restored = context?.origin as? PyFile ?: return null
    pyAnchorPointer = restored.createSmartPointer()
    return restored
  }

  // --- PyType implementation ---
  override val name: String
    get() = "$classQName ($typeName)"  // Visible to the user

  override val isBuiltin: Boolean = false
  override fun assertValid(message: String?) {
    check(isValid()) {
      "${message ?: "Type is no longer valid"}. Type: $name"
    }
  }

  abstract fun getChildren(): Collection<PbSymbol>

  override fun resolveMember(
    name: String,
    location: PyExpression?,
    direction: AccessDirection,
    resolveContext: PyResolveContext,
  ): List<RatedResolveResult> = resolveMember(name, location, direction, resolveContext, true)

  override fun getCompletionVariants(
    completionPrefix: String?,
    location: PsiElement,
    context: ProcessingContext,
  ): Array<out Any> {
    val typeEvalContext = TypeEvalContext.codeCompletion(location.project, location.containingFile)
    val results = mutableListOf<Any>()

    results.addAll(getChildren().map(::PbSymbolLookupElement))

    getSuperClassTypes(typeEvalContext).forEach { superType ->
      results.addAll(superType.getCompletionVariants(completionPrefix, location, context))
    }

    if (isDefinition) {
      getMetaClassType(typeEvalContext, true)?.let { metaType ->
        results.addAll(metaType.getCompletionVariants(completionPrefix, location, context))
      }
    }

    return results.toTypedArray()
  }

  // --- PyCallableType implementation ---
  override fun isCallable(): Boolean = isDefinition
  override fun getReturnType(context: TypeEvalContext): PyType? = if (isDefinition) toInstance() else null
  override fun getCallType(context: TypeEvalContext, callSite: PyCallSiteOwner): PyType? = getReturnType(context)

  // --- PyInstantiableType implementation  ---
  override fun isDefinition(): Boolean = isDefinitionFlag

  protected abstract fun asDefinition(isDefinitionFlag: Boolean): PbPythonAbstractType<E>
  override fun toInstance(): PbPythonAbstractType<E> = if (isDefinition) asDefinition(false) else this
  override fun toClass(): PbPythonAbstractType<E> = if (isDefinition) this else asDefinition(true)

  // --- PyClassLikeType implementation  ---
  override fun isValid(): Boolean = pbElement?.isValid == true

  override fun resolveMember(
    name: String,
    location: PyExpression?,
    direction: AccessDirection,
    resolveContext: PyResolveContext,
    inherited: Boolean,
  ): List<RatedResolveResult> {
    val results = mutableListOf<RatedResolveResult>()

    resolveInProto(source, localQn.append(name)).forEach {
      results.add(RatedResolveResult(RatedResolveResult.RATE_NORMAL, it))
    }

    if (inherited) {
      for (ancestorType in getAncestorTypes(resolveContext.typeEvalContext)) {
        results.addAll(ancestorType.resolveMember(name, location, direction, resolveContext, true).orEmpty())
      }

      if (isDefinition && PyNames.INIT != name && PyNames.NEW != name) {
        val metaType = getMetaClassType(resolveContext.typeEvalContext, true)
        results.addAll(metaType?.resolveMember(name, location, direction, resolveContext, true).orEmpty())
      }
    }

    return results
  }

  override fun visitMembers(
    processor: Processor<in PsiElement>,
    inherited: Boolean,
    context: TypeEvalContext,
  ) {
    getChildren().forEach { if (!processor.process(it)) return }

    if (inherited) {
      getAncestorTypes(context).forEach {
        it.visitMembers(processor, true, context)
      }

      if (isDefinition) {
        getMetaClassType(context, true)?.visitMembers(processor, true, context)
      }
    }
  }

  override fun getMemberNames(inherited: Boolean, context: TypeEvalContext): Set<String> {
    val result = getChildren().mapNotNull { it.name }.toMutableSet()

    if (inherited) {
      getAncestorTypes(context).forEach { ancestor ->
        result.addAll(ancestor.getMemberNames(true, context))
      }

      if (isDefinition) {
        getMetaClassType(context, true)?.let {
          result.addAll(it.getMemberNames(true, context))
        }
      }
    }

    return result
  }

  override fun getSuperClassTypes(context: TypeEvalContext): List<PyClassLikeType> =
    listOfNotNull(PyBuiltinCache.getInstance(getPyAnchor(context)).objectType)

  override fun getMetaClassType(context: TypeEvalContext, inherited: Boolean): PyClassLikeType? = null

  override fun getClassQName(): String = source.pbFile.packageQualifiedName.append(localQn).toString()

  // --- PyWithAncestors implementation ---
  override fun getAncestorTypes(context: TypeEvalContext): List<PyClassLikeType> {
    val supers = getSuperClassTypes(context)
    return (supers + supers.flatMap { it.getAncestorTypes(context).filterNotNull() }).distinct()
  }

  // --- Other methods ---
  override fun toString(): String = name

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as PbPythonAbstractType<*>
    return source == other.source &&
           localQn == other.localQn &&
           isDefinition == other.isDefinition
  }

  override fun hashCode(): Int = Objects.hash(source, localQn, isDefinition)
}
