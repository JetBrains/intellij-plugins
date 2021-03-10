// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.types

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.types.JSSimpleRecordTypeImpl
import com.intellij.psi.PsiElement

class VueCompleteRecordType(typeSource: PsiElement?, typeMembers: List<JSRecordType.TypeMember>) :
  JSSimpleRecordTypeImpl(createStrictTypeSource(typeSource), typeMembers), VueCompleteType