package training.lesson.dialogs;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.ui.StateRestoringCheckBox;
import org.jetbrains.annotations.NotNull;
import training.lesson.EducationBundle;

import javax.swing.*;
import java.awt.*;

/**
 * Created by karashevich on 09/09/15.
 */
public class EduProjectWarningDialog extends DialogWrapper {
    private final Project myProject;

    private StateRestoringCheckBox myCbDoNotAskAgain;


    public EduProjectWarningDialog(Project project) {
        super(project, true);
        myProject = project;
        setTitle(EducationBundle.message("dialog.eduProjectWarning.title"));
        init();
    }


    @Override
    @NotNull
    protected Action[] createActions() {
        return new Action[]{getOKAction(), getCancelAction()};
    }

    @Override
    protected JComponent createNorthPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();

        final String warningMessage = EducationBundle.message("dialog.eduProjectWarning.message");

        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(warningMessage), gbc);

//        gbc.gridy++;
//        gbc.gridx = 0;
//        gbc.weightx = 0.0;
//        gbc.gridwidth = 1;
//        myCbDoNotAskAgain = new StateRestoringCheckBox();
//        myCbDoNotAskAgain.setText(EducationBundle.message("dialog.invalidSdk.checkbox"));
//        panel.add(myCbDoNotAskAgain, gbc);

//        myCbDoNotAskAgain.setSelected(true);

        return panel;
    }



    @Override
    protected JComponent createCenterPanel() {
        return null;
    }


    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

}
