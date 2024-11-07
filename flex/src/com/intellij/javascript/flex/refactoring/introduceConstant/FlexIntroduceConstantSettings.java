// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.refactoring.introduceConstant;

import com.intellij.lang.javascript.refactoring.introduce.ClassLevelIntroduceSettings;

public interface FlexIntroduceConstantSettings extends ClassLevelIntroduceSettings {
  String getClassName();
}