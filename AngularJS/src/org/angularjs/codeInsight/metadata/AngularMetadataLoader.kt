package org.angularjs.codeInsight.metadata

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.util.CachedValueImpl

object AngularMetadataLoader {
  private val GSON = Gson()
  private val LOG = Logger.getInstance(AngularMetadataLoader::class.java)
  private val KEY = Key.create<CachedValueImpl<AngularMetadata>>("angular.metadata.value")

  fun load(file: VirtualFile): AngularMetadata {
    var cachedValue = file.getUserData(KEY)
    if (cachedValue == null) {
      cachedValue = file.putUserDataIfAbsent(KEY, CachedValueImpl({ val visited = mutableSetOf(file)
                                                                    CachedValueProvider.Result.create(doLoad(file, visited), visited)
                                                                  }))
    }
    return cachedValue.value
  }

  private fun doLoad(file: VirtualFile, visited: MutableSet<VirtualFile>): AngularMetadata {
    val text = VfsUtil.loadText(file)
    val classes = mutableListOf<AngularClass>()
    try {
      val json = GSON.fromJson<Any>(text, Any::class.java)
      if (json is Map<*, *>) {
        classes.addAll(loadMetadata(file, visited, json))
      }
      if (json is ArrayList<*> && json.size > 0) {
        val metadata = json[0]
        if (metadata is Map<*, *>) {
          classes.addAll(loadMetadata(file, visited, metadata))
        }
      }
    }
    catch (e: Exception) {
      LOG.error("Error loading " + file.path, e)
    }
    return AngularMetadata(classes.toTypedArray())
  }

  private fun loadMetadata(file: VirtualFile, visited: MutableSet<VirtualFile>, json: Map<*, *>): List<AngularClass> {
    val classes = mutableListOf<AngularClass>()

    val metadata = json["metadata"]
    if (metadata is Map<*, *>) {
      for (clazz in metadata) {
        if (clazz.key is String && clazz.value is Map<*, *>) {
          val parsed = parseClass(file, visited, classes, clazz.key as String, clazz.value as Map<*, *>)
          if (parsed != null) {
            classes.add(parsed)
          }
        }
      }
    }

    return classes
  }

  private fun parseClass(file: VirtualFile, visited: MutableSet<VirtualFile>, classes: List<AngularClass>, key: String, value: Map<*, *>): AngularClass? {
    if (value["__symbolic"] != "class") return null
    val members = value["members"]
    val inputs = mutableListOf<AngularField>()
    val outputs = mutableListOf<AngularField>()
    if (members is Map<*, *>) {
      for (member in members) {
        if (member.key is String && member.value is ArrayList<*>) {
          val declarations = (member.value as ArrayList<*>)
          if (declarations.size < 1) continue

          val name = member.key as String
          if (findDecorator(declarations[0], "Input") != null) {
            inputs.add(AngularField(name))
          }
          if (findDecorator(declarations[0], "Output") != null) {
            outputs.add(AngularField(name))
          }
        }
      }
    }
    val extends = value["extends"]
    if (extends is Map<*, *>) {
      val name = extends["name"]
      val module = extends["module"]
      if (name is String) {
        var superClass: AngularClass? = null
        if (module is String) {
          val superFile = file.parent.findFileByRelativePath(module + ".metadata.json")
          if (superFile != null) {
            val superMetadata = load(superFile)
            visited.add(superFile)
            superClass = superMetadata.findClass(name)
          }
        } else {
          superClass = classes.find { it.name == name}
        }
        if (superClass != null) {
          inputs.addAll(superClass.inputs)
          outputs.addAll(superClass.outputs)
        }
      }
    }
    val decorator = findDecorator(value, "Component") ?: findDecorator(value, "Directive")
    if (decorator is Map<*, *>) {
      val arguments = decorator["arguments"]
      if (arguments is ArrayList<*> && arguments.size > 0 && arguments[0] is Map<*, *>) {
        val argumentsMap = arguments[0] as Map<*, *>
        val selector = argumentsMap["selector"]

        if (selector is String) {
          parseFieldsFromDecorator(argumentsMap, inputs, "inputs")
          parseFieldsFromDecorator(argumentsMap, outputs, "outputs")
          return AngularDirective(key, inputs.toTypedArray(), outputs.toTypedArray(), selector)
        }
      }
    }
    return AngularClass(key, inputs.toTypedArray(), outputs.toTypedArray())
  }

  private fun parseFieldsFromDecorator(decorator: Map<*, *>,
                                       fields: MutableList<AngularField>,
                                       name: String) {
    val fieldList = decorator[name]
    if (fieldList is ArrayList<*>) {
      fieldList.filterIsInstance<String>().mapTo(fields) { AngularField(it) }
    }
  }

  private fun findDecorator(obj: Any, decoratorName: String): Map<*, *>? {
    if (obj is Map<*, *>) {
      val decorators = obj["decorators"]
      if (decorators is ArrayList<*>) {
        for (decorator in decorators) {
          if (decorator is Map<*, *>) {
            val expression = decorator["expression"]
            if (expression is Map<*, *> && expression["name"] == decoratorName) {
              return decorator
            }
          }
        }
      }
    }
    return null
  }
}
