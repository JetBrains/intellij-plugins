package org.intellij.plugins.markdown.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanelProvider;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

public class MarkdownPreviewSettingsForm implements MarkdownPreviewSettings.Holder {
  private Object myLastItem;
  private ComboBox myPreviewProvider;
  private ComboBox myDefaultSplitLayout;
  private JPanel myMainPanel;
  private JBCheckBox myUseGrayscaleRenderingForJBCheckBox;
  private EnumComboBoxModel<SplitFileEditor.SplitEditorLayout> mySplitLayoutModel;
  private CollectionComboBoxModel<MarkdownHtmlPanelProvider.ProviderInfo> myPreviewPanelModel;

  public JComponent getComponent() {
    return myMainPanel;
  }

  private void createUIComponents() {
    //noinspection unchecked
    final List<MarkdownHtmlPanelProvider.ProviderInfo> providerInfos =
      ContainerUtil.mapNotNull(MarkdownHtmlPanelProvider.getProviders(),
                               provider -> {
                                 if (provider.isAvailable() == MarkdownHtmlPanelProvider.AvailabilityInfo.UNAVAILABLE) {
                                   return null;
                                 }
                                 return provider.getProviderInfo();
                               });
    myPreviewPanelModel = new CollectionComboBoxModel<MarkdownHtmlPanelProvider.ProviderInfo>(providerInfos, providerInfos.get(0));
    myPreviewProvider = new ComboBox(myPreviewPanelModel);

    mySplitLayoutModel = new EnumComboBoxModel<SplitFileEditor.SplitEditorLayout>(SplitFileEditor.SplitEditorLayout.class);
    myDefaultSplitLayout = new ComboBox(mySplitLayoutModel);

    myLastItem = myPreviewProvider.getSelectedItem();
    myPreviewProvider.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        final Object item = e.getItem();
        if (e.getStateChange() != ItemEvent.SELECTED || !(item instanceof MarkdownHtmlPanelProvider.ProviderInfo)) {
          return;
        }

        final MarkdownHtmlPanelProvider provider = MarkdownHtmlPanelProvider.createFromInfo((MarkdownHtmlPanelProvider.ProviderInfo)item);
        final MarkdownHtmlPanelProvider.AvailabilityInfo availability = provider.isAvailable();

        if (!availability.checkAvailability(myMainPanel)) {
          myPreviewProvider.setSelectedItem(myLastItem);
        }
        else {
          myLastItem = item;
          updateUseGrayscaleEnabled();
        }
      }
    });
  }

  private void updateUseGrayscaleEnabled() {
    final MarkdownHtmlPanelProvider.ProviderInfo selected = myPreviewPanelModel.getSelected();
    myUseGrayscaleRenderingForJBCheckBox.setEnabled(selected != null && selected.getClassName().contains("JavaFxHtmlPanelProvider"));
  }

  @Override
  public void setMarkdownPreviewSettings(@NotNull MarkdownPreviewSettings settings) {
    if (myPreviewPanelModel.contains(settings.getHtmlPanelProviderInfo())) {
      myPreviewPanelModel.setSelectedItem(settings.getHtmlPanelProviderInfo());
    }
    mySplitLayoutModel.setSelectedItem(settings.getSplitEditorLayout());
    myUseGrayscaleRenderingForJBCheckBox.setSelected(settings.isUseGrayscaleRendering());

    updateUseGrayscaleEnabled();
  }

  @NotNull
  @Override
  public MarkdownPreviewSettings getMarkdownPreviewSettings() {
    if (myPreviewPanelModel.getSelected() == null) {
      throw new IllegalStateException("Should be selected always");
    }
    return new MarkdownPreviewSettings(mySplitLayoutModel.getSelectedItem(),
                                       myPreviewPanelModel.getSelected(),
                                       myUseGrayscaleRenderingForJBCheckBox.isSelected());
  }
}
