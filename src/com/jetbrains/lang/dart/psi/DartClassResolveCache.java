package com.jetbrains.lang.dart.psi;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.impl.AnyPsiChangeListener;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.util.containers.ConcurrentWeakHashMap;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBus;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public class DartClassResolveCache {
  private final ConcurrentWeakHashMap<DartClass, DartClassResolveResult> myMap = createWeakMap();

  public static DartClassResolveCache getInstance(Project project) {
    ProgressIndicatorProvider.checkCanceled(); // We hope this method is being called often enough to cancel daemon processes smoothly
    return ServiceManager.getService(project, DartClassResolveCache.class);
  }

  public DartClassResolveCache(@NotNull MessageBus messageBus) {
    messageBus.connect().subscribe(PsiManagerImpl.ANY_PSI_CHANGE_TOPIC, new AnyPsiChangeListener() {
      @Override
      public void beforePsiChanged(boolean isPhysical) {
        myMap.clear();
      }

      @Override
      public void afterPsiChanged(boolean isPhysical) {
      }
    });
  }

  private static <K, V> ConcurrentWeakHashMap<K, V> createWeakMap() {
    return new ConcurrentWeakHashMap<K, V>(7, 0.75f, Runtime.getRuntime().availableProcessors(),
                                           ContainerUtil.<ConcurrentWeakHashMap.Key<K, V>>canonicalStrategy());
  }

  public void put(@NotNull DartClass dartClass, @NotNull DartClassResolveResult result) {
    myMap.put(dartClass, result);
  }

  @Nullable
  public DartClassResolveResult get(DartClass dartClass) {
    return myMap.get(dartClass);
  }
}
