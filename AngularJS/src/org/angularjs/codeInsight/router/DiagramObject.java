package org.angularjs.codeInsight.router;

import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Irina.Chernushina on 3/23/2016.
 */
public class DiagramObject {
  @NotNull private final Type myType;
  @NotNull private final String myName;
  @Nullable private final SmartPsiElementPointer myNavigationTarget;
  private boolean myIsValid = true;  //invalid = created by reference from other place, but not defined
  @NotNull private final List<String> myWarnings;
  @NotNull private final List<String> myErrors;
  private final Map<String, DiagramObject> myChildren;
  private AngularUiRouterNode myParent;

  public DiagramObject(@NotNull Type type, @NotNull String name, @Nullable SmartPsiElementPointer navigationTarget) {
    myType = type;
    myName = name;
    myNavigationTarget = navigationTarget;
    myWarnings = new SmartList<>();
    myErrors = new SmartList<>();
    myChildren = new HashMap<>();
  }

  public void addChild(@NotNull final DiagramObject child, AngularUiRouterNode parent) {
    myChildren.put(child.getName(), child);
    child.myParent = parent;
  }

  public Map<String, DiagramObject> getChildren() {
    return myChildren;
  }

  public AngularUiRouterNode getParent() {
    return myParent;
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
}
