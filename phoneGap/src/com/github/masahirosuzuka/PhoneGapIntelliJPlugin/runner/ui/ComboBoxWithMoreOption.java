package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui;


import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

@SuppressWarnings("unchecked")
public class ComboBoxWithMoreOption extends ComboBox {

  @NotNull
  private final Collection<String> myStartItems;
  @NotNull
  private final Collection<String> myExtendedItems;
  @NotNull
  private final String myMoreItem;

  public ComboBoxWithMoreOption(@NotNull Collection<String> startItems, @NotNull Collection<String> extendedItems) {
    super();
    this.myStartItems = startItems;
    myExtendedItems = extendedItems;
    myMoreItem = PhoneGapBundle.message("phonegap.run.more.options");
    initItems();
  }

  public void initItems() {
    addItems(this, myStartItems);

    this.addItem(myMoreItem);
    this.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.DESELECTED && myMoreItem.equals(getSelectedItem())) {

          final Object prevItem = e.getItem();

          ApplicationManager.getApplication().invokeLater(() -> {
            if (getItemCount() != myStartItems.size() + myExtendedItems.size()) {
              ComboBoxWithMoreOption.this.extend();
              setSelectedItem(prevItem);
            }

            if (!ComboBoxWithMoreOption.this.isPopupVisible()) {
              ComboBoxWithMoreOption.this.showPopup();
            }
          });
        }
      }
    });
  }

  public void setSelectedWithExtend(String value) {
    if (!myStartItems.contains(value)) {
      extend();
    }
    setSelectedItem(value);
  }

  private static void addItems(ComboBox box, Collection<String> items) {
    for (String item : items) {
      box.addItem(item);
    }
  }


  public void extend() {
    removeItem(myMoreItem);
    addItems(this, myExtendedItems);
  }
}
