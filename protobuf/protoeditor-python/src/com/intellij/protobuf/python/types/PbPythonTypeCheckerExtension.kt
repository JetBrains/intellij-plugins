package com.intellij.protobuf.python.types

import com.jetbrains.python.psi.types.PyClassLikeType
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.PyTypeChecker
import com.jetbrains.python.psi.types.PyTypeCheckerExtension
import com.jetbrains.python.psi.types.TypeEvalContext
import java.util.Optional

internal class PbPythonTypeCheckerExtension : PyTypeCheckerExtension {
  override fun match(
    expected: PyType?,
    actual: PyType?,
    context: TypeEvalContext,
    substitutions: PyTypeChecker.GenericSubstitutions,
  ): Optional<Boolean> {
    if (actual is PbPythonAbstractType<*>) {
      if (expected is PyClassLikeType && expected !is PbPythonAbstractType<*>) {
        if (expected.isDefinition != actual.isDefinition) return Optional.empty()
        val expectedClass = expected.toClass()

        for (ancestor in actual.getAncestorTypes(context)) {
          if (PyTypeChecker.match(expectedClass, ancestor.toClass(), context, substitutions)) {
            return Optional.of(true)
          }
        }
      }
    }

    return Optional.empty()
  }
}
