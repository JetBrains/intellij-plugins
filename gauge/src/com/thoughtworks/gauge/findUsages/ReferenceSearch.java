/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.findUsages;

import com.intellij.find.FindBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.thoughtworks.gauge.findUsages.helper.ReferenceSearchHelper;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public final class ReferenceSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
  private final ReferenceSearchHelper helper;

  public ReferenceSearch() {
    this.helper = new ReferenceSearchHelper();
  }

  @Override
  public void processQuery(@NotNull ReferencesSearch.SearchParameters searchParameters,
                           @NotNull Processor<? super PsiReference> processor) {
    ApplicationManager.getApplication().runReadAction(() -> {
      if (!helper.shouldFindReferences(searchParameters, searchParameters.getElementToSearch())) return;
      if (EventQueue.isDispatchThread()) {
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
          () -> processElements(searchParameters, processor),
          FindBundle.message("find.usages.progress.title"), true, searchParameters.getElementToSearch().getProject()
        );
      }
      else {
        processElements(searchParameters, processor);
      }
    });
  }

  private void processElements(final ReferencesSearch.SearchParameters searchParameters, final Processor<? super PsiReference> processor) {
    ApplicationManager.getApplication().runReadAction(() -> {
      StepCollector collector = helper.getStepCollector(searchParameters.getElementToSearch());
      collector.collect();
      List<PsiElement> elements = helper.getPsiElements(collector, searchParameters.getElementToSearch());
      for (PsiElement element : elements) {
        processor.process(element.getReference());
      }
    });
  }
}
