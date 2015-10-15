package org.intellij.plugins.markdown.preview.split;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.ui.JBSplitter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

public abstract class SplitFileEditor extends UserDataHolderBase implements FileEditor {
  private static final String MY_PROPORTION_KEY = "SplitFileEditor.Proportion";

  @NotNull
  protected final FileEditor myMainEditor;
  @NotNull
  protected final FileEditor mySecondEditor;
  @NotNull
  private final JBSplitter myComponent;
  @NotNull
  private final MyListenersMultimap myListenersGenerator = new MyListenersMultimap();

  public SplitFileEditor(@NotNull FileEditor mainEditor, @NotNull FileEditor secondEditor) {
    myMainEditor = mainEditor;
    mySecondEditor = secondEditor;

    myComponent = new JBSplitter(false, 0.5f, 0.15f, 0.85f);
    myComponent.setSplitterProportionKey(MY_PROPORTION_KEY);
    myComponent.setFirstComponent(myMainEditor.getComponent());
    myComponent.setSecondComponent(mySecondEditor.getComponent());
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myComponent;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return myComponent.getFirstComponent();
  }

  @NotNull
  @Override
  public FileEditorState getState(@NotNull FileEditorStateLevel level) {
    return new MyFileEditorState(myMainEditor.getState(level), mySecondEditor.getState(level));
  }

  @Override
  public void setState(@NotNull FileEditorState state) {
    if (state instanceof MyFileEditorState) {
      final MyFileEditorState compositeState = (MyFileEditorState)state;
      if (compositeState.getFirstState() != null) {
        myMainEditor.setState(compositeState.getFirstState());
      }
      if (compositeState.getSecondState() != null) {
        mySecondEditor.setState(compositeState.getSecondState());
      }
    }
  }

  @Override
  public boolean isModified() {
    return myMainEditor.isModified() || mySecondEditor.isModified();
  }

  @Override
  public boolean isValid() {
    return myMainEditor.isValid() && mySecondEditor.isValid();
  }

  @Override
  public void selectNotify() {
    myMainEditor.selectNotify();
    mySecondEditor.selectNotify();
  }

  @Override
  public void deselectNotify() {
    myMainEditor.deselectNotify();
    mySecondEditor.deselectNotify();
  }

  @Override
  public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    myMainEditor.addPropertyChangeListener(listener);
    mySecondEditor.addPropertyChangeListener(listener);

    final DoublingEventListenerDelegate delegate = myListenersGenerator.addListenerAndGetDelegate(listener);
    myMainEditor.addPropertyChangeListener(delegate);
    mySecondEditor.addPropertyChangeListener(delegate);
  }

  @Override
  public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    myMainEditor.removePropertyChangeListener(listener);
    mySecondEditor.removePropertyChangeListener(listener);

    final DoublingEventListenerDelegate delegate = myListenersGenerator.removeListenerAndGetDelegate(listener);
    if (delegate != null) {
      myMainEditor.removePropertyChangeListener(delegate);
      mySecondEditor.removePropertyChangeListener(delegate);
    }
  }

  @Nullable
  @Override
  public BackgroundEditorHighlighter getBackgroundHighlighter() {
    return myMainEditor.getBackgroundHighlighter();
  }

  @Nullable
  @Override
  public FileEditorLocation getCurrentLocation() {
    return myMainEditor.getCurrentLocation();
  }

  @Nullable
  @Override
  public StructureViewBuilder getStructureViewBuilder() {
    return myMainEditor.getStructureViewBuilder();
  }

  @Override
  public void dispose() {
    Disposer.dispose(myMainEditor);
    Disposer.dispose(mySecondEditor);
  }

  static class MyFileEditorState implements FileEditorState {
    @Nullable
    private final FileEditorState myFirstState;
    @Nullable
    private final FileEditorState mySecondState;

    public MyFileEditorState(@Nullable FileEditorState firstState, @Nullable FileEditorState secondState) {
      myFirstState = firstState;
      mySecondState = secondState;
    }

    @Nullable
    public FileEditorState getFirstState() {
      return myFirstState;
    }

    @Nullable
    public FileEditorState getSecondState() {
      return mySecondState;
    }

    @Override
    public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
      return otherState instanceof MyFileEditorState
             && (myFirstState == null || myFirstState.canBeMergedWith(((MyFileEditorState)otherState).myFirstState, level))
             && (mySecondState == null || mySecondState.canBeMergedWith(((MyFileEditorState)otherState).mySecondState, level));
    }
  }

  private class DoublingEventListenerDelegate implements PropertyChangeListener {
    @NotNull
    private final PropertyChangeListener myDelegate;

    private DoublingEventListenerDelegate(@NotNull PropertyChangeListener delegate) {
      myDelegate = delegate;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      myDelegate.propertyChange(new PropertyChangeEvent(SplitFileEditor.this, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()));
    }
  }

  private class MyListenersMultimap {
    private final Map<PropertyChangeListener, Pair<Integer, DoublingEventListenerDelegate>> myMap =
      new HashMap<PropertyChangeListener, Pair<Integer, DoublingEventListenerDelegate>>();

    @NotNull
    public DoublingEventListenerDelegate addListenerAndGetDelegate(@NotNull PropertyChangeListener listener) {
      if (!myMap.containsKey(listener)) {
        myMap.put(listener, Pair.create(1, new DoublingEventListenerDelegate(listener)));
      }
      else {
        final Pair<Integer, DoublingEventListenerDelegate> oldPair = myMap.get(listener);
        myMap.put(listener, Pair.create(oldPair.getFirst() + 1, oldPair.getSecond()));
      }

      return myMap.get(listener).getSecond();
    }

    @Nullable
    public DoublingEventListenerDelegate removeListenerAndGetDelegate(@NotNull PropertyChangeListener listener) {
      final Pair<Integer, DoublingEventListenerDelegate> oldPair = myMap.get(listener);
      if (oldPair == null) {
        return null;
      }

      if (oldPair.getFirst() == 1) {
        myMap.remove(listener);
      }
      else {
        myMap.put(listener, Pair.create(oldPair.getFirst() - 1, oldPair.getSecond()));
      }
      return oldPair.getSecond();
    }
  }
}
