package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNode
import java.util.*

inline fun <reified T> lookupSection(sections: Collection<CfnNode>): T? = sections.singleOrNull { it is T } as T?

inline fun <reified T : Any> Collection<*>.ofType(): Collection<T> = this.mapNotNull { it as? T }

fun <T> T.toOptionalValue(): Optional<T> = Optional.of(this)

