package com.intellij.flex.uiDesigner;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import gnu.trove.TIntArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

abstract class EmbedAssetManager<I extends EmbedAssetInfo> implements Disposable {
  private final TIntArrayList freeIndices = new TIntArrayList();
  protected final ArrayList<I> assets = new ArrayList<I>();

  protected EmbedAssetManager() {
    final Application application = ApplicationManager.getApplication();
    final MessageBus messageBus = application.getMessageBus();
    messageBus.connect(this).subscribe(DesignerApplicationManager.MESSAGE_TOPIC, new DesignerApplicationListener() {
      @Override
      public void initialDocumentOpened() {
      }

      @Override
      public void applicationClosed() {
        reset();
      }
    });
  }

  protected int allocateId() {
    return freeIndices.isEmpty() ? assets.size() : freeIndices.remove(freeIndices.size() - 1);
  }

  protected void add(@NotNull I info) {
    if (info.id == assets.size()) {
      assets.add(info);
    }
    else {
      assets.set(info.id, info);
    }
  }

  protected void remove(int id) {
    assets.set(id, null);
    freeIndices.add(id);
  }

  public I getInfo(int id) {
    return assets.get(id);
  }

  public void reset() {
    assets.clear();
    freeIndices.resetQuick();
  }

  @Override
  public void dispose() {
    // implements Disposable for messageBus.connect
  }
}

abstract class EmbedAssetInfo {
  public final VirtualFile file;
  public final int id;

  public EmbedAssetInfo(@NotNull VirtualFile file, int id) {
    this.file = file;
    this.id = id;
  }
}
