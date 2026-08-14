package com.intellij.aws.cloudformation

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.CloudFormationDeprecatedMessagesBundle"

object CloudFormationDeprecatedMessagesBundle {
  private val instance = DynamicBundle(CloudFormationDeprecatedMessagesBundle::class.java, BUNDLE)

  @Nls
  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String = instance.getMessage(key, *params)

  @JvmStatic
  fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String,
                     vararg params: Any): java.util.function.Supplier<String> = instance.getLazyMessage(key, *params)
}