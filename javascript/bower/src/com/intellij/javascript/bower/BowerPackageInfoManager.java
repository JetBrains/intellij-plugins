package com.intellij.javascript.bower;

import com.intellij.javascript.bower.cache.PrevRequestSkippingCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BowerPackageInfoManager {

  private final BowerSettings mySettings;
  private final PrevRequestSkippingCache<String, BowerPackageInfo> myCache;

  public BowerPackageInfoManager(@NotNull BowerSettings settings) {
    mySettings = settings;
    myCache = new PrevRequestSkippingCache<>(
      new PrevRequestSkippingCache.Fetcher<>() {
        @Override
        public BowerPackageInfo fetch(@NotNull String packageName) throws Exception {
          return BowerPackageUtil.loadPackageInfo(null, mySettings, packageName);
        }
      }
    );
  }

  public void fetchPackageInfo(final @NotNull PackageInfoConsumer packageInfoConsumer) {
    String packageName = packageInfoConsumer.myPackageName;
    boolean canBeSkipped = packageInfoConsumer.myCanBeSkipped;
    PrevRequestSkippingCache.FetchCallback<String, BowerPackageInfo> callback =
      new PrevRequestSkippingCache.FetchCallback<>(packageName, canBeSkipped) {
        @Override
        public void onSuccess(@Nullable BowerPackageInfo packageInfo) {
          packageInfoConsumer.onPackageInfo(packageInfo);
        }

        @Override
        public void onException(@NotNull Exception e) {
          packageInfoConsumer.onException(e);
        }
      };
    myCache.fetch(callback);
  }

  public abstract static class PackageInfoConsumer {
    private final String myPackageName;
    private final boolean myCanBeSkipped;

    public PackageInfoConsumer(@NotNull String packageName, boolean canBeSkipped) {
      myPackageName = packageName;
      myCanBeSkipped = canBeSkipped;
    }

    public abstract void onPackageInfo(@Nullable BowerPackageInfo packageInfo);
    public abstract void onException(@NotNull Exception e);
  }

}
