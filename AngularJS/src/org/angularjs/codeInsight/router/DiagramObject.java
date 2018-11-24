package org.angularjs.codeInsight.router;

import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Irina.Chernushina on 3/23/2016.
 */
public class DiagramObject {
  @NotNull private Type myType;
  @NotNull private final String myName;
  @Nullable private String myTooltip;
  @Nullable private final SmartPsiElementPointer myNavigationTarget;
  private boolean myIsValid = true;  //invalid = created by reference from other place, but not defined
  @NotNull private final List<String> myNotes;
  @NotNull private final List<String> myWarnings;
  @NotNull private final List<String> myErrors;
  private final Map<String, DiagramObject> myChildren;
  private final List<String> myChildOrder;
  private AngularUiRouterNode myContainer;
  private String myParentName;

  public DiagramObject(@NotNull Type type, @NotNull String name, @Nullable SmartPsiElementPointer navigationTarget) {
    myType = type;
    myName = name;
    myNavigationTarget = navigationTarget;
    myWarnings = new SmartList<>();
    myErrors = new SmartList<>();
    myNotes = new SmartList<>();
    myChildren = new HashMap<>();
    myChildOrder = new ArrayList<>();
  }

  public void addChild(@NotNull final DiagramObject child, AngularUiRouterNode parent) {
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

  @NotNull
  public Type getType() {
    return myType;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @Nullable
  public SmartPsiElementPointer getNavigationTarget() {
    return myNavigationTarget;
  }

  public void addError(@NotNull final String error) {
    myErrors.add(error);
    myIsValid = false;
  }

  public void addWarning(@NotNull final String warning) {
    myWarnings.add(warning);
    myIsValid = false;
  }

  public void addNote(@NotNull final String note) {
    myNotes.add(note);
  }

  public boolean isValid() {
    return myIsValid;
  }

  @NotNull
  public List<String> getErrors() {
    return myErrors;
  }

  @NotNull
  public List<String> getWarnings() {
    return myWarnings;
  }

  @NotNull
  public List<String> getNotes() {
    return myNotes;
  }

  @Nullable
  public String getTooltip() {
    return myTooltip == null ? myName : myTooltip;
  }

  public void setTooltip(@Nullable String tooltip) {
    myTooltip = tooltip;
  }

  public void setType(Type type) {
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
    if (myTooltip != null ? !myTooltip.equals(object.myTooltip) : object.myTooltip != null) return false;
    if (myNavigationTarget != null ? !myNavigationTarget.equals(object.myNavigationTarget) : object.myNavigationTarget != null)
      return false;
    if (myContainer != null ? !myContainer.equals(object.myContainer) : object.myContainer != null) return false;

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
