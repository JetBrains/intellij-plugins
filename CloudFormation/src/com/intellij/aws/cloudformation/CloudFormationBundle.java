package com.intellij.aws.cloudformation;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

public class CloudFormationBundle {
  private static final ResourceBundle ourBundle = ResourceBundle.getBundle(CloudFormationBundle.class.getName());

  private CloudFormationBundle() {
  }

  public static String getString(@PropertyKey(resourceBundle = "com.intellij.aws.cloudformation.CloudFormationBundle") String key,
                                 Object... params) {
    return CommonBundle.message(ourBundle, key, params);
  }
}
