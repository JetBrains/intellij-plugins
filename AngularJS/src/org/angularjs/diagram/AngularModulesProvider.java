// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.diagram;

import com.intellij.lang.javascript.modules.diagram.JSModuleConnectionProvider;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.CommonProcessors;
import icons.AngularJSIcons;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularModuleIndex;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class AngularModulesProvider implements JSModuleConnectionProvider {
  public static final Color ANGULAR_COLOR = new Color(221, 27, 22);

  @Override
  public Color getEdgeColor() {
    return ANGULAR_COLOR;
  }

  @Override
  public String getName() {
    return "AngularJS";
  }

  @Override
  public List<Link> getDependencies(@NotNull PsiFile file) {
    final Project project = file.getProject();
    if (!(file instanceof JSFile)) return null;
    if (!AngularIndexUtil.hasAngularJS(project)) return null;

    final SmartPointerManager spm = SmartPointerManager.getInstance(project);
    final List<Link> result = new ArrayList<>();
    final CommonProcessors.CollectProcessor<String> processor = new CommonProcessors.CollectProcessor<>();
    final GlobalSearchScope fileScope = GlobalSearchScope.fileScope(file);
    StubIndex.getInstance().processAllKeys(AngularModuleIndex.KEY, processor, fileScope);
    for (String key : processor.getResults()) {
      AngularIndexUtil.multiResolve(project, AngularModuleIndex.KEY, key, element -> {
        if (!file.equals(element.getContainingFile())) {
          return true;
        }
        // todo whether it would blink
        final JSCallExpression expression = PsiTreeUtil.getParentOfType(element, JSCallExpression.class);
        if (expression != null) {
          final List<String> dependencies =
            AngularModuleIndex.findDependenciesInModuleDeclaration(expression);
          if (dependencies != null) {
            for (String dependency : dependencies) {
              final JSImplicitElement resolve =
                AngularIndexUtil.resolve(project, AngularModuleIndex.KEY, dependency);
              if (resolve != null) {
                result.add(new Link(spm.createSmartPsiElementPointer(element.getNavigationElement()),
                                    spm.createSmartPsiElementPointer(resolve.getNavigationElement()), key,
                                    resolve.getName(),
                                    AngularJSIcons.AngularJS));
              }
            }
          }
        }
        return true;
      });
    }
    return result;
  }
}
