// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml.model;

import com.intellij.openapi.util.NlsSafe;

import javax.swing.Icon;

public interface JdlNodeData {
  @NlsSafe String getName();

  Icon getIcon();
}
