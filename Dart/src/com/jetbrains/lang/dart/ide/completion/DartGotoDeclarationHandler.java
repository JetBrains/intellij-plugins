// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandlerBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartGotoDeclarationHandler extends GotoDeclarationHandlerBase {
  @Nullable
  @Override
  public PsiElement getGotoDeclarationTarget(@Nullable final PsiElement element, @NotNull final Editor editor) {
    final Lookup activeLookup = element != null ? LookupManager.getInstance(element.getProject()).getActiveLookup() : null;
    final LookupElement item = activeLookup != null ? activeLookup.getCurrentItem() : null;
    final Object lookupObject = item != null && item.isValid() ? item.getObject() : null;
    return lookupObject instanceof DartLookupObject ? ((DartLookupObject)lookupObject).findPsiElement() : null;
  }
}
