package com.intellij.aws.cloudformation

import com.intellij.CommonBundle
import org.jetbrains.annotations.PropertyKey

import java.util.ResourceBundle

object CloudFormationBundle {
  private val ourBundle = ResourceBundle.getBundle(CloudFormationBundle::class.java.name)

  fun getString(@PropertyKey(resourceBundle = "com.intellij.aws.cloudformation.CloudFormationBundle") key: String,
                vararg params: Any): String {
    return CommonBundle.message(ourBundle, key, *params)
  }
}
