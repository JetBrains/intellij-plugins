// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.groovy.griffon;

import icons.GriffonIcons;
import org.jetbrains.plugins.groovy.mvc.MvcModuleBuilder;

import javax.swing.*;

/**
 * @author peter
 */
public class GriffonModuleBuilder extends MvcModuleBuilder {
  public GriffonModuleBuilder() {
    super(GriffonFramework.getInstance());
  }

  @Override
  public Icon getNodeIcon() {
    return GriffonIcons.Griffon;
  }
}
