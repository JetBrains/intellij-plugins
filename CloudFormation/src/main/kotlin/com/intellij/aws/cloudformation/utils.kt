package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNode
import org.apache.commons.lang.ArrayUtils
import java.util.Arrays
import java.util.Collections
import java.util.Optional

fun findSubArray(array: ByteArray, subArray: ByteArray): Int {
  return Collections.indexOfSubList(Arrays.asList(*ArrayUtils.toObject(array)), Arrays.asList(*ArrayUtils.toObject(subArray)))
}

inline fun <reified T> lookupSection(sections: Collection<CfnNode>): T? = sections.singleOrNull { it is T } as T?

inline fun <reified T : Any> Collection<*>.ofType(): Collection<T> = this.mapNotNull { it as? T }

fun <T> T.toOptionalValue(): Optional<T> = Optional.of(this)

