// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring

import com.intellij.refactoring.rename.api.PsiModifiableRenameUsage
import com.intellij.refactoring.rename.api.RenameUsage
import com.intellij.refactoring.rename.api.RenameUsageSearchParameters
import com.intellij.refactoring.rename.api.RenameUsageSearcher
import com.intellij.util.Query
import org.angular2.entities.Angular2DirectiveSelectorSymbol
import org.angular2.findUsages.Angular2SelectorUsageSearcher

class Angular2SelectorRenameUsageSearcher: RenameUsageSearcher {

  override fun collectSearchRequest(parameters: RenameUsageSearchParameters): Query<out RenameUsage>? =
    (parameters.target as? Angular2DirectiveSelectorSymbol)
      ?.let { target ->
        Angular2SelectorUsageSearcher.buildSelectorUsagesQuery(target, parameters.searchScope)
          .mapping { PsiModifiableRenameUsage.defaultPsiModifiableRenameUsage(it) }
      }


}