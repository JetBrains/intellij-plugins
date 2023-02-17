package jetbrains.plugins.yeoman.projectGenerator.ui.list;


import com.intellij.CommonBundle;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBUI;
import jetbrains.plugins.yeoman.YeomanBundle;
import jetbrains.plugins.yeoman.generators.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class YeomanInstalledGeneratorsMain extends YeomanGeneratorsMain {

  public static final char J = 'j';
  public static final char C = 'C';

  public void setInstalledGenerators(List<YeomanInstalledGeneratorInfo> installedGenerators) {
    myInstalledGenerators = installedGenerators;
    myModel.setAllViews(myInstalledGenerators);
  }

  private List<YeomanInstalledGeneratorInfo> myInstalledGenerators;
  private final JButton myInstallGenerators;
  private final YeomanGeneratorListProvider myProvider;


  public JButton getInstallGeneratorsButton() {
    return myInstallGenerators;
  }

  public YeomanInstalledGeneratorsMain() {
    super();
    myProvider = new YeomanGeneratorListProvider();
    init(false);
    myInstalledGenerators = new YeomanInstalledGeneratorListProvider().getAllInstalledGenerators();
    myModel.setAllViews(myInstalledGenerators);
    myInstallGenerators = new JButton(YeomanBundle.message("yeoman.generators.dialog.install.generators"));
    myInstallGenerators.setMnemonic(J);
    myInstallGenerators.addActionListener(createInstallGeneratorsActionListener());
    myInstallGenerators.setMargin(JBUI.emptyInsets());
    if (myInstalledGenerators.size() > 0) {
      myGeneratorTable.getSelectionModel().addSelectionInterval(0, 0);
    }

    myToolbarPanel.setVisible(false);


  }

  private ActionListener createInstallGeneratorsActionListener() {
    return new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        DialogWrapper wrapper = new DialogWrapper(YeomanInstalledGeneratorsMain.this.main, true) {
          {
            setOKButtonText(CommonBundle.message("close.action.name"));
            setOKButtonMnemonic(C);
            init();
          }

          @Override
          protected Action @NotNull [] createActions() {
            return new Action[]{getOKAction()};
          }

          @Override
          protected JComponent createCenterPanel() {
            final YeomanAvailableGeneratorsMain availableGeneratorsMain = new YeomanAvailableGeneratorsMain(myInstalledGenerators, myProvider) {
              @Override
              public void handleUpdate() {
                YeomanInstalledGeneratorsMain installedGeneratorMain = YeomanInstalledGeneratorsMain.this;
                final List<YeomanInstalledGeneratorInfo> generators =
                  YeomanInstalledGeneratorListProvider.getProvider().getAllInstalledGenerators();


                YeomanGeneratorInfo selected = installedGeneratorMain.getSelectedObject();
                installedGeneratorMain.setInstalledGenerators(generators);



                final MyPluginsFilter parentFilter = installedGeneratorMain.myFilter;
                if (parentFilter != null) {
                  installedGeneratorMain.myModel.filter(parentFilter.getFilterLowerCase());
                }

                if (selected instanceof YeomanInstalledGeneratorInfo && generators.contains(selected)) {
                  installedGeneratorMain.select(selected);
                }
                else {
                  installedGeneratorMain.select(ContainerUtil.getFirstItem(generators));
                }

                this.setInstalledGenerators(generators);

                this.repaint();
              }
            };


            if (myProvider.isAvailableGeneratorListExists()) {
              availableGeneratorsMain.myModel.setAllViews(myProvider.getAvailableGenerators(false));
            }
            else {
              availableGeneratorsMain.downloadAndShowGeneratorList();
            }

            return availableGeneratorsMain.getMainPanel();
          }
        };

        wrapper.show();
      }
    };
  }

  @Override
  @Nullable
  public YeomanGeneratorFullInfo getFullGeneratorInfo(@Nullable YeomanGeneratorInfo info) {
    if (info instanceof YeomanGeneratorFullInfo || info == null) return (YeomanGeneratorFullInfo)info;

    if (myProvider.isAvailableGeneratorListExists()) {
      for (YeomanGeneratorFullInfo fullInfo : myProvider.getAvailableGenerators(false)) {
        if (StringUtil.equals(fullInfo.getName(), info.getName())) {
          return fullInfo;
        }
      }
    }

    return null;
  }
}