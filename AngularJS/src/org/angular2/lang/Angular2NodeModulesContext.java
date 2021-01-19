// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang;

import com.intellij.web.context.WebFrameworkNodeModulesContext;

import static org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE;

public class Angular2NodeModulesContext extends WebFrameworkNodeModulesContext {

  public Angular2NodeModulesContext() {
    super(ANGULAR_CORE_PACKAGE);
  }
}
