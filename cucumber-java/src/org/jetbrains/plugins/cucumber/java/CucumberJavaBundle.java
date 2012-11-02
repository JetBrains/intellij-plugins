package org.jetbrains.plugins.cucumber.java;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * User: Andrey.Vokin
 * Date: 10/30/12
 */
public class CucumberJavaBundle {
  @NonNls public static final String BUNDLE = "org.jetbrains.plugins.cucumber.java.CucumberJavaBundle";

  private static Reference<ResourceBundle> ourBundle;

  private CucumberJavaBundle() {
  }

  public static String message(@NonNls @PropertyKey(resourceBundle = BUNDLE)String key, Object... params) {
    return CommonBundle.message(getBundle(), key, params);
  }

  private static ResourceBundle getBundle() {
    ResourceBundle bundle = null;
    if (ourBundle != null) bundle = ourBundle.get();
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE);
      ourBundle = new SoftReference<ResourceBundle>(bundle);
    }
    return bundle;
  }
}
