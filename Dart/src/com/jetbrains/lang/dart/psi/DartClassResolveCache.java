package com.jetbrains.lang.dart.psi;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.impl.AnyPsiChangeListener;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBus;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentMap;

/**
 * @author Fedor.Korotkov
 */
public class DartClassResolveCache {
  private final ConcurrentMap<DartClass, DartClassResolveResult> myMap = createWeakMap();

  public static DartClassResolveCache getInstance(Project project) {
    ProgressIndicatorProvider.checkCanceled(); // We hope this method is being called often enough to cancel daemon processes smoothly
    return ServiceManager.getService(project, DartClassResolveCache.class);
  }

  public DartClassResolveCache(@NotNull MessageBus messageBus) {
    messageBus.connect().subscribe(PsiManagerImpl.ANY_PSI_CHANGE_TOPIC, new AnyPsiChangeListener.Adapter() {
      @Override
      public void beforePsiChanged(boolean isPhysical) {
        myMap.clear();
      }
    });
  }

  private static <K, V> ConcurrentMap<K, V> createWeakMap() {
    return ContainerUtil.createConcurrentWeakMap(7, 0.75f, Runtime.getRuntime().availableProcessors(),
                                           ContainerUtil.<K>canonicalStrategy());
  }

  public void put(@NotNull DartClass dartClass, @NotNull DartClassResolveResult result) {
    myMap.put(dartClass, result);
  }

  @Nullable
  public DartClassResolveResult get(DartClass dartClass) {
    return myMap.get(dartClass);
  }
}
