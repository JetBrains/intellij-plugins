package com.intellij.protobuf.python.types

import com.intellij.openapi.util.Ref
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.protobuf.lang.psi.PbEnumDefinition
import com.intellij.protobuf.lang.psi.PbEnumValue
import com.intellij.protobuf.lang.psi.PbExtendBody
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbMapField
import com.intellij.protobuf.lang.psi.PbMessageType
import com.intellij.protobuf.lang.psi.PbSimpleField
import com.intellij.protobuf.python.PbPythonNames
import com.intellij.protobuf.python.PbPythonProtoUtils.resolveInProto
import com.intellij.protobuf.python.PbPythonPsiUtils.locateInProto
import com.intellij.protobuf.python.PbPythonPsiUtils.locateInProtoUsingQualifier
import com.intellij.protobuf.python.PbPythonSourceContext
import com.intellij.protobuf.python.toPyClass
import com.intellij.protobuf.python.types.PbPythonTypeUtils.convertMapFieldToPyType
import com.intellij.protobuf.python.types.PbPythonTypeUtils.convertProtoTypeToPyType
import com.intellij.protobuf.python.types.PbPythonTypeUtils.convertTypeToAssignable
import com.intellij.protobuf.python.types.PbPythonTypeUtils.createCollectionType
import com.intellij.protobuf.python.types.PbPythonTypeUtils.findWellKnownTypeSuperclass
import com.intellij.protobuf.python.types.PbPythonTypeUtils.getMapContainerClassQName
import com.intellij.protobuf.python.types.PbPythonTypeUtils.getRepeatedContainerClassQName
import com.intellij.psi.PsiElement
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyElement
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyTargetExpression
import com.jetbrains.python.psi.impl.PyBuiltinCache
import com.jetbrains.python.psi.types.PyCallableType
import com.jetbrains.python.psi.types.PyCallableTypeImpl
import com.jetbrains.python.psi.types.PyClassTypeImpl
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.PyTypeProviderBase
import com.jetbrains.python.psi.types.TypeEvalContext

internal class PbPythonTypeProvider : PyTypeProviderBase() {

  override fun getReferenceExpressionType(referenceExpression: PyReferenceExpression, context: TypeEvalContext): PyType? {
    for ((source, localQn) in locateInProto(referenceExpression, context)) {
      for (pbElement in resolveInProto(source, localQn)) {
        val type = getType(source, localQn, pbElement, referenceExpression)
        if (type != null) return type
      }
    }
    return null
  }

  override fun getReferenceType(referenceTarget: PsiElement, context: TypeEvalContext, anchor: PsiElement?): Ref<PyType?>? {
    if (referenceTarget is PyTargetExpression && referenceTarget.isQualified) {
      val (source, localQn) = locateInProtoUsingQualifier(referenceTarget, context) ?: return null

      // If protobuf was located, this target belongs to protobuf.
      // Prevent other type providers from handling it by returning Ref.create(null).
      for (pbElement in resolveInProto(source, localQn)) {
        val type = getType(source, localQn, pbElement, referenceTarget)
        if (type != null) return Ref.create(type)
      }
      return Ref.create(null)
    }
    return null
  }

  override fun prepareCalleeTypeForCall(
    type: PyType?,
    call: PyCallExpression,
    context: TypeEvalContext,
  ): Ref<PyCallableType?>? {
    val pbPythonType = type as? PbPythonAbstractType<*> ?: return null
    if (!pbPythonType.isCallable) return Ref.create(null)

    val params = pbPythonType.getParameters(context).orEmpty()
    val returnType = pbPythonType.toInstance()
    return Ref.create(PyCallableTypeImpl(params, returnType))
  }

  private fun getType(
    source: PbPythonSourceContext,
    localQn: QualifiedName,
    pbElement: PbElement,
    anchor: PyElement,
  ): PyType? {
    val builtins = PyBuiltinCache.getInstance(anchor)

    return when (pbElement) {
      is PbFile -> PbPythonNamespaceType(source, anchor)
      is PbMessageType -> {
        val superclass = findWellKnownTypeSuperclass(pbElement, source, anchor)
        PbPythonMessageType(pbElement, source, localQn, true, anchor, superclass)
      }
      is PbEnumDefinition -> PbPythonEnumType(pbElement, source, localQn, anchor)
      is PbEnumValue -> builtins.intType
      is PbSimpleField -> {
        if (pbElement.parent is PbExtendBody) {
          return PbPythonNames.FIELD_DESCRIPTOR.toPyClass(anchor)?.let { PyClassTypeImpl(it, false) }
        }

        val fieldType = convertProtoTypeToPyType(pbElement.typeName, source, anchor)
        val type = convertTypeToAssignable(fieldType, anchor)

        if (pbElement.isRepeated) {
          val containerQName = getRepeatedContainerClassQName(type)
          createCollectionType(containerQName.toPyClass(anchor), listOf(type), builtins.listType)
        }
        else {
          type
        }
      }
      is PbMapField -> {
        val (fieldKeyType, fieldValueType) = convertMapFieldToPyType(pbElement, source, anchor)
        val keyType = convertTypeToAssignable(fieldKeyType, anchor)
        val valueType = convertTypeToAssignable(fieldValueType, anchor)

        val containerQName = getMapContainerClassQName(valueType)
        createCollectionType(containerQName.toPyClass(anchor), listOf(keyType, valueType), builtins.dictType)
      }
      else -> null
    }
  }
}
