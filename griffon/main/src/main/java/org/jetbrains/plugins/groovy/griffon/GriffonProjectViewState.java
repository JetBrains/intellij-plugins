// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.groovy.griffon;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import org.jetbrains.plugins.groovy.mvc.projectView.MvcProjectViewState;

/**
 * @author Sergey Evdokimov
 */
@State(
  name="GriffonProjectView",
  storages= {@Storage(StoragePathMacros.WORKSPACE_FILE)})
public class GriffonProjectViewState extends MvcProjectViewState {

}
