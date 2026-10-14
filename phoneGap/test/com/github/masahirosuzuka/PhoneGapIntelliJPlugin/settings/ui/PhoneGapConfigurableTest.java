// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.PhoneGapSettings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class PhoneGapConfigurableTest extends BasePlatformTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // the test classpath has no plugin descriptor, so nothing registers the settings service
    ServiceContainerUtil.registerOrReplaceServiceInstance(ApplicationManager.getApplication(), PhoneGapSettings.class,
                                                          new PhoneGapSettings(), getTestRootDisposable());
  }

  /**
   * The searchable options build (traverseUI) disposes a settings page only with {@link PhoneGapConfigurable#disposeUIResources()}.
   */
  public void testDisposeUIResourcesLeavesNoDisposerRoot() {
    assertTrue("The check needs the Disposer debug mode", Disposer.isDebugMode());

    var configurable = new PhoneGapConfigurable(getProject());
    var component = configurable.createComponent();
    assertNotNull(component);
    var disposables = new ArrayList<Disposable>();
    collectDisposables(component, disposables);
    configurable.disposeUIResources();

    var leaks = new LinkedHashMap<Disposable, Throwable>();
    for (var disposable : disposables) {
      var registration = Disposer.getRegistrationTrace(disposable);
      if (registration != null) {
        leaks.put(disposable, registration);
      }
    }
    try {
      if (!leaks.isEmpty()) {
        var error = new AssertionError("These components stay Disposer roots after disposeUIResources(): " +
                                       leaks.keySet().stream().map(it -> it.getClass().getName()).toList());
        leaks.values().forEach(error::addSuppressed);
        throw error;
      }
    }
    finally {
      // stops the pending read actions of the leaked components and keeps the Disposer tree clean for the next tests
      leaks.keySet().forEach(Disposer::dispose);
    }
  }

  private static void collectDisposables(Component component, List<Disposable> result) {
    if (component instanceof Disposable disposable) {
      result.add(disposable);
    }
    if (component instanceof Container container) {
      for (var child : container.getComponents()) {
        collectDisposables(child, result);
      }
    }
  }
}
