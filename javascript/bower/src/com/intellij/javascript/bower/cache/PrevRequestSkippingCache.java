package com.intellij.javascript.bower.cache;

import com.intellij.util.ObjectUtils;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.containers.FixedHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;

public class PrevRequestSkippingCache<K, V> {

  private final Fetcher<? super K, ? extends V> myFetcher;

  private final Map<K, Future<V>> myRecentFutureMap = Collections.synchronizedMap(new FixedHashMap<>(10));
  private final Executor myCheckExecutor;
  private final Executor myFetchExecutor;
  private final BlockingQueue<FetchCallback<K, V>> myFetchQueue = new LinkedBlockingQueue<>();
  private volatile FetchCallback<K, V> myLatestCallback;

  public PrevRequestSkippingCache(@NotNull Fetcher<? super K, ? extends V> fetcher) {
    myFetcher = fetcher;
    myCheckExecutor = AppExecutorUtil.createBoundedApplicationPoolExecutor("PrevRequestSkippingCache Check Pool", 2);
    myFetchExecutor = AppExecutorUtil.createBoundedApplicationPoolExecutor("PrevRequestSkippingCache Pool", 2);
  }

  public void fetch(final @NotNull FetchCallback<K, V> callback) {
    myLatestCallback = callback;
    if (!checkRecentCache(callback)) {
      fetchAsync(callback);
    }
  }

  private boolean canBeSkipped(@NotNull FetchCallback callback) {
    if (!callback.canBeSkipped()) {
      return false;
    }
    FetchCallback latestFetch = myLatestCallback;
    if (callback == latestFetch || latestFetch == null) {
      return false;
    }
    return !callback.getKey().equals(latestFetch.getKey());
  }

  private boolean checkRecentCache(@NotNull FetchCallback<K, V> callback) {
    Future<V> future = myRecentFutureMap.get(callback.getKey());
    if (future == null) {
      return false;
    }
    if (future.isDone()) {
      processFuture(callback, future);
    }
    else {
      myCheckExecutor.execute(() -> {
        if (!canBeSkipped(callback)) {
          processFuture(callback, future);
        }
      });
    }
    return true;
  }

  private void fetchAsync(@NotNull FetchCallback<K, V> callback) {
    myFetchQueue.offer(callback);
    myFetchExecutor.execute(() -> {
      FetchCallback<K, V> next;
      while ((next = myFetchQueue.poll()) != null) {
        if (!canBeSkipped(next)) {
          if (!checkRecentCache(next)) {
            fetchSync(next);
          }
        }
      }
    });
  }

  private void fetchSync(final @NotNull FetchCallback<K, V> callback) {
    FutureTask<V> future = new FutureTask<>(() -> {
      try {
        V result = myFetcher.fetch(callback.getKey());
        callback.onSuccess(result);
        return result;
      }
      catch (Exception e) {
        callback.onException(e);
        throw e;
      }
    });
    myRecentFutureMap.put(callback.getKey(), future);
    future.run();
  }

  private void processFuture(@NotNull FetchCallback<K, V> consumer, @NotNull Future<V> future) {
    try {
      V result = future.get();
      consumer.onSuccess(result);
    }
    catch (InterruptedException e) {
      consumer.onSuccess(null);
    }
    catch (ExecutionException e) {
      Exception cause = ObjectUtils.tryCast(e.getCause(), Exception.class);
      if (cause != null) {
        consumer.onException(cause);
      }
      else {
        consumer.onException(e);
      }
    }
  }

  public interface Fetcher<K, V> {
    @Nullable
    V fetch(@NotNull K key) throws Exception;
  }

  public abstract static class FetchCallback<K, V> {
    private final K myKey;
    private final boolean myCanBeSkipped;

    public FetchCallback(@NotNull K key, boolean canBeSkipped) {
      myKey = key;
      myCanBeSkipped = canBeSkipped;
    }

    public @NotNull K getKey() {
      return myKey;
    }

    public boolean canBeSkipped() {
      return myCanBeSkipped;
    }

    public abstract void onSuccess(@Nullable V value);
    public abstract void onException(@NotNull Exception e);
  }

}
