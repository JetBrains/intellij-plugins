package org.intellij.plugins.markdown.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanelProvider;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class MarkdownPreviewSettingsForm implements MarkdownPreviewSettings.Holder {
  private ComboBox myPreviewProvider;
  private ComboBox myDefaultSplitLayout;
  private JPanel myMainPanel;
  private EnumComboBoxModel<SplitFileEditor.SplitEditorLayout> mySplitLayoutModel;
  private CollectionComboBoxModel<MarkdownHtmlPanelProvider.ProviderInfo> myPreviewPanelModel;

  public JComponent getComponent() {
    return myMainPanel;
  }

  private void createUIComponents() {
    //noinspection unchecked
    final List<MarkdownHtmlPanelProvider.ProviderInfo> providerInfos = ContainerUtil.map(MarkdownHtmlPanelProvider.getProviders(),
                                                                                         new Function<MarkdownHtmlPanelProvider, MarkdownHtmlPanelProvider.ProviderInfo>() {
                                                                                           @Override
                                                                                           public MarkdownHtmlPanelProvider.ProviderInfo fun(
                                                                                             MarkdownHtmlPanelProvider provider) {
                                                                                             return provider.getProviderInfo();
                                                                                           }
                                                                                         });
    myPreviewPanelModel = new CollectionComboBoxModel<MarkdownHtmlPanelProvider.ProviderInfo>(providerInfos, providerInfos.get(0));
    myPreviewProvider = new ComboBox(myPreviewPanelModel);

    mySplitLayoutModel = new EnumComboBoxModel<SplitFileEditor.SplitEditorLayout>(SplitFileEditor.SplitEditorLayout.class);
    myDefaultSplitLayout = new ComboBox(mySplitLayoutModel);
  }

  @Override
  public void setMarkdownPreviewSettings(@NotNull MarkdownPreviewSettings settings) {
    if (myPreviewPanelModel.contains(settings.getHtmlPanelProviderInfo())) {
      myPreviewPanelModel.setSelectedItem(settings.getHtmlPanelProviderInfo());
    }
    mySplitLayoutModel.setSelectedItem(settings.getSplitEditorLayout());
  }

  @NotNull
  @Override
  public MarkdownPreviewSettings getMarkdownPreviewSettings() {
    if (myPreviewPanelModel.getSelected() == null) {
      throw new IllegalStateException("Should be selected always");
    }
    return new MarkdownPreviewSettings(mySplitLayoutModel.getSelectedItem(), myPreviewPanelModel.getSelected());
  }
}
