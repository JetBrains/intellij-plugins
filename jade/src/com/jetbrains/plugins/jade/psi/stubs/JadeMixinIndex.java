package com.jetbrains.plugins.jade.psi.stubs;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.jetbrains.plugins.jade.psi.impl.JadeMixinDeclarationImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class JadeMixinIndex extends StringStubIndexExtension<JadeMixinDeclarationImpl> {
  public static final StubIndexKey<String, JadeMixinDeclarationImpl> KEY = StubIndexKey.createIndexKey("jade.mixin");
  public static final int VERSION = 0;

  @Override
  public int getVersion() {
    return super.getVersion() + VERSION;
  }

  @Override
  public @NotNull StubIndexKey<String, JadeMixinDeclarationImpl> getKey() {
    return KEY;
  }

  public static Collection<String> getKeys(Project project) {
    return StubIndex.getInstance().getAllKeys(KEY, project);
  }

  public static Collection<JadeMixinDeclarationImpl> find(@NotNull String key, Project project, GlobalSearchScope scope) {
    return StubIndex.getElements(KEY, key, project, scope, JadeMixinDeclarationImpl.class);
  }
}
