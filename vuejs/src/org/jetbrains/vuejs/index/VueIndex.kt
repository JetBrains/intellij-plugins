// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.Processor
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.index.VueIndexBase.Companion.createJSKey

const val VUE_FILE_EXTENSION = ".vue"
const val VUE_MODULE: String = "vue"
const val VUE_INSTANCE_MODULE: String = "vue/types/vue"
const val VUETIFY_MODULE: String = "vuetify"
const val BOOTSTRAP_VUE_MODULE: String = "bootstrap-vue"
const val SHARDS_VUE_MODULE: String = "shards-vue"
const val COMPOSITION_API_MODULE: String = "@vue/composition-api"

val VUE_DEFAULT_EXTENSIONS_WITH_DOT = arrayOf(VUE_FILE_EXTENSION)

const val GLOBAL: String = "global"
const val LOCAL: String = "local"
const val GLOBAL_BINDING_MARK: String = "*"
private const val INDEXED_ACCESS_HINT = "[]"
private const val DELIMITER = ';'

/**
 * Requires [PsiModificationTracker.MODIFICATION_COUNT] and [DumbService.modificationTracker] as dependencies for caches.
 */
fun getForAllKeys(scope: GlobalSearchScope, key: StubIndexKey<String, JSImplicitElementProvider>): Sequence<JSImplicitElement> {
  if (DumbService.isDumb(scope.project!!)) return emptySequence()
  val keys = StubIndex.getInstance().getAllKeys(key, scope.project!!)
  return keys.asSequence().flatMap { resolve(it, scope, key) }
}

/**
 * Requires [PsiModificationTracker.MODIFICATION_COUNT] and [DumbService.modificationTracker] as dependencies for caches.
 */
fun resolve(name: String, scope: GlobalSearchScope, key: StubIndexKey<String, JSImplicitElementProvider>): Collection<JSImplicitElement> {
  if (DumbService.isDumb(scope.project!!)) return emptyList()
  val normalized = normalizeNameForIndex(name)
  val indexKey = createJSKey(key)

  val result = mutableListOf<JSImplicitElement>()
  StubIndex.getInstance().processElements(
    key, normalized, scope.project!!, scope, JSImplicitElementProvider::class.java,
    Processor { provider: JSImplicitElementProvider? ->
      provider?.indexingData?.implicitElements
        // the check for name is needed for groups of elements, for instance:
        // directives: {a:..., b:...} -> a and b are recorded in 'directives' data.
        // You can find it with 'a' or 'b' key, but you should filter the result
        ?.filter { it.userString == indexKey && normalized == it.name }
        ?.forEach { result.add(it) }
      return@Processor true
    })
  return result
}

internal fun normalizeNameForIndex(name: String) = fromAsset(name.substringBeforeLast(GLOBAL_BINDING_MARK))

data class VueIndexData(val originalName: String,
                        val nameQualifiedReference: String,
                        val descriptorQualifiedReference: String,
                        val indexedAccessUsed: Boolean,
                        val isGlobal: Boolean)

private fun splitAndUnescape(s: String): List<String> {
  val result = ArrayList<String>(4)
  val builder = StringBuilder()
  var prevIsEscape = false
  for (i in s.indices) {
    val c = s[i]
    if (prevIsEscape) {
      builder.append(c)
      prevIsEscape = false
    }
    else if (c == '\\') {
      prevIsEscape = true
    }
    else if (c == DELIMITER) {
      result.add(builder.toString())
      builder.clear()
    }
    else {
      builder.append(c)
    }
  }
  result.add(builder.toString())
  return result
}

fun getVueIndexData(element: JSImplicitElement): VueIndexData? {
  val userStringData = element.userStringData ?: return null
  val parts = splitAndUnescape(userStringData)

  assert(parts.size == 4) {
    "Error with $element [name = ${element.name}, userString = ${element.userString}, userStringData = $userStringData, parts=$parts]"
  }

  val originalName = parts[0]
  val nameQualifiedReference = parts[1]
  val descriptorQualifiedReference = parts[2].substringBefore(INDEXED_ACCESS_HINT)
  val indexedAccessUsed = parts[2].endsWith(INDEXED_ACCESS_HINT)
  val isGlobal = parts[3] == "1"

  return VueIndexData(originalName, nameQualifiedReference, descriptorQualifiedReference, indexedAccessUsed, isGlobal)
}

fun serializeUserStringData(originalName: String,
                            nameQualifiedReference: String,
                            descriptorQualifiedReference: String,
                            indexedAccessUsed: Boolean,
                            isGlobal: Boolean): String {
  return buildString {
    append(escapePart(originalName))
    append(DELIMITER)
    append(escapePart(nameQualifiedReference))
    append(DELIMITER)
    append(escapePart(descriptorQualifiedReference))
    if (indexedAccessUsed) append(INDEXED_ACCESS_HINT)
    append(DELIMITER)
    append(if (isGlobal) "1" else "0")
  }
}

private fun escapePart(part: String): String {
  return StringUtil.escapeChars(part, '\\', DELIMITER)
}

