// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.router;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class DiagramObject {
  private @NotNull Type myType;
  private final @NotNull @NlsSafe String myName;
  private @Nullable @Nls String myTooltip;
  private final @Nullable SmartPsiElementPointer<?> myNavigationTarget;
  private boolean myIsValid = true;  //invalid = created by reference from other place, but not defined
  private final @NotNull List<String> myNotes;
  private final @NotNull List<String> myWarnings;
  private final @NotNull List<String> myErrors;
  private final Map<String, DiagramObject> myChildren;
  private final List<String> myChildOrder;
  private AngularUiRouterNode myContainer;
  private String myParentName;

  public DiagramObject(@NotNull Type type, @NotNull String name, @Nullable SmartPsiElementPointer<?> navigationTarget) {
    myType = type;
    myName = name;
    myNavigationTarget = navigationTarget;
    myWarnings = new SmartList<>();
    myErrors = new SmartList<>();
    myNotes = new SmartList<>();
    myChildren = new HashMap<>();
    myChildOrder = new ArrayList<>();
  }

  public void addChild(final @NotNull DiagramObject child, AngularUiRouterNode parent) {
    myChildren.put(child.getName(), child);
    child.myContainer = parent;
    myChildOrder.add(child.getName());
  }

  public List<DiagramObject> getChildrenList() {
    final List<DiagramObject> list = new ArrayList<>();
    for (String s : myChildOrder) {
      list.add(myChildren.get(s));
    }
    return list;
  }

  public AngularUiRouterNode getContainer() {
    return myContainer;
  }

  public @NotNull Type getType() {
    return myType;
  }

  public @NotNull @NlsSafe String getName() {
    return myName;
  }

  public @Nullable SmartPsiElementPointer<?> getNavigationTarget() {
    return myNavigationTarget;
  }

  public void addError(final @NotNull String error) {
    myErrors.add(error);
    myIsValid = false;
  }

  public void addWarning(final @NotNull String warning) {
    myWarnings.add(warning);
    myIsValid = false;
  }

  public void addNote(final @NotNull String note) {
    myNotes.add(note);
  }

  public boolean isValid() {
    return myIsValid;
  }

  public @NotNull List<String> getErrors() {
    return myErrors;
  }

  public @NotNull List<String> getWarnings() {
    return myWarnings;
  }

  public @NotNull List<String> getNotes() {
    return myNotes;
  }

  public @NotNull @Nls String getTooltip() {
    return myTooltip == null ? myName : myTooltip;
  }

  public void setTooltip(@Nls @Nullable String tooltip) {
    myTooltip = tooltip;
  }

  public void setType(@NotNull Type type) {
    myType = type;
  }

  public String getParent() {
    return myParentName;
  }

  public void setParent(String parentName) {
    myParentName = parentName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DiagramObject object = (DiagramObject)o;

    if (myType != object.myType) return false;
    if (!myName.equals(object.myName)) return false;
    if (!Objects.equals(myTooltip, object.myTooltip)) return false;
    if (!Objects.equals(myNavigationTarget, object.myNavigationTarget)) {
      return false;
    }
    if (!Objects.equals(myContainer, object.myContainer)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myType.hashCode();
    result = 31 * result + myName.hashCode();
    result = 31 * result + (myTooltip != null ? myTooltip.hashCode() : 0);
    result = 31 * result + (myContainer != null ? myContainer.hashCode() : 0);
    return result;
  }
}
