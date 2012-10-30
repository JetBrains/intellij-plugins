package org.jetbrains.plugins.cucumber.groovy;

import com.intellij.CommonBundle;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * @author Max Medvedev
 */

public class GrCucumberBundle extends GroovyObjectSupport implements GroovyObject {
  @NonNls public static final String BUNDLE = "org.jetbrains.plugins.cucumber.groovy.GrCucumberBundle";

  private static Reference<ResourceBundle> ourBundle;

  private GrCucumberBundle() {
  }

  public static String message(@NonNls @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
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
