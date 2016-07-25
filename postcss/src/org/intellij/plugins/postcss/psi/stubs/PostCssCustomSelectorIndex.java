package org.intellij.plugins.postcss.psi.stubs;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.util.Processor;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomSelectorIndex extends StringStubIndexExtension<PostCssCustomSelector> {
  public static final StubIndexKey<String, PostCssCustomSelector> KEY = StubIndexKey.createIndexKey("postcss.custom.selector");

  @NotNull
  @Override
  public StubIndexKey<String, PostCssCustomSelector> getKey() {
    return KEY;
  }

  public static void process(@NotNull String name,
                             @NotNull Project project,
                             @NotNull GlobalSearchScope scope, @NotNull Processor<? super PostCssCustomSelector> processor) {
    StubIndex.getInstance().processElements(KEY, name, project, scope, PostCssCustomSelector.class, processor);
  }
}