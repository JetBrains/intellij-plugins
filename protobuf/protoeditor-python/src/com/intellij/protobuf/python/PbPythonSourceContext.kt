package com.intellij.protobuf.python

import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.protobuf.ide.PbCompositeModificationTracker
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.shared.gencode.ProtoFromSourceComments
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.pyi.PyiFile
import com.jetbrains.python.pyi.PyiUtil

/**
 * Context for generated Python (`_pb.py`/`_pb2.py` files).
 *
 * Stores information about how the file was generated and from what source.
 */
internal data class PbPythonSourceContext(
  val pbFile: PbFile,
  val apiVersion: ApiVersion,
) {
  enum class ApiVersion(val suffix: String) {
    // Order matters: _pb2 should have priority over _pb (for example, when searching files)
    V2("_pb2"),
    V1("_pb");

    companion object {
      fun fromFileName(fileBaseName: String): ApiVersion? =
        entries.find { fileBaseName.endsWith(it.suffix) }
    }
  }

  companion object {
    /**
     * Resolves [PbPythonSourceContext] from a generated Python file.
     *
     * @param file `.py` or `.pyi` file generated from a `.proto` file
     */
    fun resolve(file: PyFile): PbPythonSourceContext? =
      CachedValuesManager.getCachedValue(file) {
        val generatedPyFile = when (file) {
          is PyiFile ->
            // Prefer PbPython* types over regular Python classes from .pyi
            // Try finding generated .py using .pyi
            PyiUtil.getOriginalElement(file) as? PyFile
          else -> file
        }

        val result = generatedPyFile?.let { resolveImpl(it) }

        CachedValueProvider.Result.create(
          result,
          listOfNotNull(
            generatedPyFile,
            VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
            generatedPyFile?.let { PbCompositeModificationTracker.getInstance(it.project) }
          )
        )
      }

    private fun resolveImpl(generatedPyFile: PyFile): PbPythonSourceContext? {
      val fileBaseName = generatedPyFile.viewProvider.virtualFile.nameWithoutExtension
      val apiVersion = ApiVersion.fromFileName(fileBaseName) ?: return null

      val protoSource = ProtoFromSourceComments.findProtoOfGeneratedCode("#", generatedPyFile) ?: return null

      return PbPythonSourceContext(protoSource, apiVersion)
    }
  }
}
