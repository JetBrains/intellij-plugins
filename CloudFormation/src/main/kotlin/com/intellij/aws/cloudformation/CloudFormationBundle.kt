package com.intellij.aws.cloudformation

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.CloudFormationBundle"

object CloudFormationBundle {
  private val instance = DynamicBundle(CloudFormationBundle::class.java, BUNDLE)

  @Nls
  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
    return if (instance.containsKey(key)) instance.getMessage(key, *params) else CloudFormationDeprecatedMessagesBundle.message(key, *params)
  }

  @JvmStatic
  fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String,
                     vararg params: Any): java.util.function.Supplier<String> {
    return if (instance.containsKey(key))  instance.getLazyMessage(key, *params) else CloudFormationDeprecatedMessagesBundle.messagePointer(key, *params)
  }
}