package com.intellij.flex.uiDesigner.mxml;

import org.jetbrains.annotations.Nullable;


class State {
  private final int index;

  String name;

  final LinkedList<OverrideBase> overrides = new LinkedList<OverrideBase>();
  private final StateWriter stateWriter;

  State(StateWriter stateWriter, int index) {
    this.stateWriter = stateWriter;
    this.index = index;
  }

  @Nullable
  AddItems getValidActiveAddItems(Context parentContext, boolean autoItemDestruction) {
    AddItems item = getActiveAddItems(parentContext);
    return (item != null && item.isAutoDestruction() == autoItemDestruction) ? item : null;
  }

  private AddItems getActiveAddItems(Context parentContext) {
    return parentContext.activeAddItems != null ? parentContext.activeAddItems[index] : null;
  }

  private void setActiveAddItems(AddItems override, Context parentContext) {
    if (parentContext.activeAddItems == null) {
      parentContext.activeAddItems = new AddItems[stateWriter.statesSize()];
    }

    parentContext.activeAddItems[index] = override;
  }

  public void addAddItems(AddItems override, Context parentContext, @Nullable SetPropertyOrStyle pendingFirstSetProperty) {
    // if all children included in some state, then position is first, and in overrides are exists in reverse order, 
    // — we sort for correct layering
    AddItems activeAddItems = getActiveAddItems(parentContext);
    if (activeAddItems != null) {
      assert overrides.size() > 0;
      overrides.addBefore(activeAddItems, override);
    }
    else if (pendingFirstSetProperty != null &&
             overrides.contains(pendingFirstSetProperty) /* todo refactor, depends on — support setProperty in several states — 
             text.groupName="A" where groupName is A and B states */) {
      overrides.addBefore(pendingFirstSetProperty, override);
    }
    else {
      overrides.add(override);
    }

    setActiveAddItems(override, parentContext);
  }

  public void applyItemAutoDestruction(Context context, Context parentContext, BaseWriter writer) {
    AddItems override = getActiveAddItems(parentContext);
    if (override == null) {
      return;
    }

    int size = override.getItemDeferredInstances().size();
    int lastIndex = size - 1;
    if (!override.isAutoDestruction() && override.getItemDeferredInstances().get(lastIndex) == context) {
      if (size == 1) {
        // test case: ItemDestructionPolicyMergeItems2, override == text="3", override.next == text="2"
        AddItems next = (AddItems)overrides.getNext(override);
        if (next != null && next.isAutoDestruction()) {
          next.addItemDeferredInstance(override.getItemDeferredInstances().get(0));
          writer.getBlockOut().removeLastMarkerAndAssert(override.dataRange);
          overrides.remove(override);
          setActiveAddItems(next, parentContext);
        }
        else {
          override.setAutoDestruction(true);
        }
      }
      else {
        override.getItemDeferredInstances().remove(lastIndex);
        AddItems separatedAddItems = stateWriter.createAddItems((DynamicObjectContext)context, parentContext, true);
        // about why add before active override see State.addAddItems
        overrides.addBefore(override, separatedAddItems);
        setActiveAddItems(separatedAddItems, parentContext);
      }
    }
  }
}
