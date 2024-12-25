package jetbrains.plugins.yeoman.projectGenerator.ui.list;

import com.intellij.ide.ui.search.SearchUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.*;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.ui.speedSearch.SpeedSearchSupply;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorFullInfo;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorInfo;
import jetbrains.plugins.yeoman.generators.YeomanInstalledGeneratorInfo;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.BorderUIResource;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;

public class YeomanGeneratorsMain implements Disposable {
  
  private static final @NlsSafe String TEXT_SUFFIX = "</body></html>";

  protected JPanel main;
  protected JPanel myToolbarPanel;
  protected JEditorPane myDescriptionTextArea;
  protected JPanel myInfoPanel;
  protected JPanel myTablePanel;
  protected JBScrollPane myDescriptionScrollPane;
  protected JPanel myHeader;
  private JSplitPane mySplitPane;
  protected YeomanGeneratorTable myGeneratorTable;

  protected @Nullable MyPluginsFilter myFilter;

  private YeomanGeneratorInfoPanelHeader myGeneratorInfoPanelHeader;
  private ActionToolbar myActionToolbar;

  public YeomanGeneratorTableModel getModel() {
    return myModel;
  }

  protected YeomanGeneratorTableModel myModel;


  public YeomanGeneratorsMain() {
  }

  protected void init(boolean isFullInfo) {
    GuiUtils.replaceJSplitPaneWithIDEASplitter(main);
    myDescriptionTextArea.setEditorKit(new HTMLEditorKit());
    myDescriptionTextArea.setEditable(false);
    myDescriptionTextArea.addHyperlinkListener(new BrowserHyperlinkListener());
    myDescriptionTextArea.setBorder(JBUI.Borders.emptyLeft(5));
    myModel = new YeomanGeneratorTableModel();

    myGeneratorTable = new YeomanGeneratorTable(myModel);

    JScrollPane scrollpanel = ScrollPaneFactory
      .createScrollPane(myGeneratorTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    myTablePanel.add(scrollpanel, BorderLayout.CENTER);


    myDescriptionScrollPane.setBackground(UIUtil.getTextFieldBackground());
    Border border = new BorderUIResource.LineBorderUIResource(new JBColor(Gray._220, Gray._55), 1);
    myInfoPanel.setBorder(border);

    myGeneratorInfoPanelHeader = new YeomanGeneratorInfoPanelHeader(this, isFullInfo);


    myHeader.setBackground(UIUtil.getTextFieldBackground());
    myHeader.add(myGeneratorInfoPanelHeader.getComponent(), BorderLayout.CENTER);

    installTableActions();

    if (myFilter != null) {
      myToolbarPanel.add(myFilter, BorderLayout.WEST);
    }

    final ActionGroup group = getActionGroup(true);
    if (group != null) {
      myActionToolbar = ActionManager.getInstance().createActionToolbar("YeomanGenerators", group, true);
      final JComponent component = myActionToolbar.getComponent();
      component.setBorder(JBUI.Borders.emptyBottom(6));
      myToolbarPanel.add(component, BorderLayout.CENTER);
    }

    final Container parent = myInfoPanel.getParent();
    if (parent instanceof Splitter) {
      ((Splitter)parent).setDividerWidth(2);
    }
  }

  protected ActionGroup getActionGroup(boolean b) {
    return null;
  }


  @Override
  public void dispose() {

  }

  public void repaint() {
    myModel.fireTableDataChanged();
  }

  protected void installTableActions() {
    YeomanGeneratorSpeedSearch.installOn(myGeneratorTable);

    myGeneratorTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        refresh();
      }
    });
  }

  private void refresh() {
    YeomanGeneratorInfo selectedValue = getSelectedValue();
    final YeomanGeneratorFullInfo generator = getFullGeneratorInfo(selectedValue);
    setTextValue(generator == null ? null : new StringBuilder(generator.getDescription()), null, myDescriptionTextArea);
    myGeneratorInfoPanelHeader.updateInfo(selectedValue);
    final JComponent parent = (JComponent)myHeader.getParent();
    parent.revalidate();
    parent.repaint();
  }



  private @Nullable YeomanGeneratorInfo getSelectedValue() {
    YeomanGeneratorInfo[] descriptors = myGeneratorTable.getSelectedObjects();
    return descriptors != null && descriptors.length == 1 ? descriptors[0] : null;
  }

  private static @NlsSafe String getTextPrefix() {
    final int fontSize = JBUIScale.scale(13);
    final int m1 = JBUIScale.scale(1);
    final int m2 = JBUIScale.scale(5);
    
    return String.format(
      "<html><head>" +
      "    <style type=\"text/css\">" +
      "        p {" +
      "            font-family: Arial,serif; font-size: %dpt; margin: %dpx %dpx" +
      "        }" +
      "    </style>" +
      "</head><body style=\"font-family: Arial,serif; font-size: %dpt; margin: %dpx %dpx;\">",
      fontSize, m1, m1, fontSize, m2, m2);
  }

  public static void setTextValue(@Nls @NlsSafe StringBuilder text, @Nullable String filter, JEditorPane pane) {
    if (text != null) {
      text.insert(0, getTextPrefix());
      text.append(TEXT_SUFFIX);
      @NlsSafe String trim = SearchUtil.markup(text.toString(), filter).trim();
      pane.setText(trim);
      pane.setCaretPosition(0);
    }
    else {
      pane.setText(getTextPrefix() + TEXT_SUFFIX);
    }
  }


  public @NotNull JPanel getMainPanel() {
    return main;
  }

  private void createUIComponents() {
    myHeader = new JPanel(new BorderLayout()) {
      @Override
      public Color getBackground() {
        return UIUtil.getTextFieldBackground();
      }
    };
  }

  @SuppressWarnings("unused")
  public @Nullable YeomanGeneratorInfo getSelectedObject() {
    return ArrayUtil.getFirstElement(myGeneratorTable.getSelectedObjects());
  }

  public void handleUpdate() {
  }


  public class MyPluginsFilter extends FilterComponent {
    public MyPluginsFilter() {
      super("PLUGIN_FILTER", 5);
    }

    @Override
    public void filter() {
      myGeneratorTable.putClientProperty(SpeedSearchSupply.SEARCH_QUERY_KEY, getFilter());
      myModel.filter(getFilterLowerCase());
      TableUtil.ensureSelectionExists(myGeneratorTable);
    }

    public String getFilterLowerCase() {
      return StringUtil.toLowerCase(getFilter());
    }
  }

  public void select(@Nullable YeomanGeneratorInfo info) {
    myGeneratorTable.select(info);
  }

  public @Nullable YeomanInstalledGeneratorInfo getInstalledGeneratorInfo(YeomanGeneratorInfo info) {
    return info instanceof YeomanInstalledGeneratorInfo ? (YeomanInstalledGeneratorInfo)info : null;
  }

  public @Nullable YeomanGeneratorFullInfo getFullGeneratorInfo(@Nullable YeomanGeneratorInfo info) {
    return YeomanGeneratorFullInfo.getFullInfo(info);
  }
}
