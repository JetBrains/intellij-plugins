package com.dmarcotte.handlebars.config;

import com.intellij.ide.util.PropertiesComponent;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PropertiesComponentStub extends PropertiesComponent {
  private final Map<String, String> fakeStorage = new THashMap<String, String>();

  @Override
  public void unsetValue(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isValueSet(String name) {
    return fakeStorage.containsKey(name);
  }

  @Override
  public String getValue(@NonNls String name) {
    return fakeStorage.get(name);
  }

  @Override
  public void setValue(@NonNls String name, String value) {
    fakeStorage.put(name, value);
  }

  @Override
  public void setValue(@NotNull String name, @NotNull String value, @NotNull String defaultValue) {
    if (value.equals(defaultValue)) {
      fakeStorage.remove(name);
    }
    else {
      fakeStorage.put(name, value);
    }
  }

  @Override
  public void setValue(@NotNull String name, float value, float defaultValue) {
    if (value == defaultValue) {
      fakeStorage.remove(name);
    }
    else {
      fakeStorage.put(name, String.valueOf(value));
    }
  }

  @Override
  public void setValue(@NotNull String name, int value, int defaultValue) {
    if (value == defaultValue) {
      fakeStorage.remove(name);
    }
    else {
      fakeStorage.put(name, String.valueOf(value));
    }
  }

  @NotNull
  @Override
  public String getValue(@NonNls String name, @NotNull String defaultValue) {
    throw new UnsupportedOperationException("Implement me if needed");
  }

  @Override
  @SuppressWarnings("UnusedDeclaration") // required by IDEA 12, but unused when building against IDEA 11
  public String[] getValues(@NonNls String name) {
    throw new UnsupportedOperationException("Implement me if needed");
  }

  @Override
  @SuppressWarnings("UnusedDeclaration") // required by IDEA 12, but unused when building against IDEA 11
  public void setValues(@NonNls String name, String[] values) {
    throw new UnsupportedOperationException("Implement me if needed");
  }

  @SuppressWarnings("EmptyMethod") // see comment in method for why this is cool
  @Override
  public String getOrInit(@NonNls String name, String defaultValue) {
    // parent is implemented using isValueSet and getValue, so use that to keep things
    // true to form.  There is a tiny chance that will change and this test will start behaving odd...
    // hopefully if that happens, this comment helps resolve the issue
    return super.getOrInit(name, defaultValue);
  }
}
