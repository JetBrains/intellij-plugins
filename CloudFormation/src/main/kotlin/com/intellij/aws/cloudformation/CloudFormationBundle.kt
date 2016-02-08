package com.intellij.aws.cloudformation

import com.intellij.CommonBundle
import org.jetbrains.annotations.PropertyKey
import java.util.ResourceBundle

object CloudFormationBundle {
  private val bundle = ResourceBundle.getBundle(CloudFormationBundle::class.java.name)

  fun getString(@PropertyKey(resourceBundle = "com.intellij.aws.cloudformation.CloudFormationBundle") key: String,
                vararg params: Any): String = CommonBundle.message(bundle, key, *params)
}
