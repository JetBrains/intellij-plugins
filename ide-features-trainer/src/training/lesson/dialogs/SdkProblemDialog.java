package training.lesson.dialogs;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.projectRoots.JdkUtil;
import com.intellij.openapi.projectRoots.impl.SdkVersionUtil;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.StateRestoringCheckBox;
import org.jetbrains.annotations.NotNull;
import training.lesson.EducationBundle;

import javax.swing.*;
import java.awt.*;

/**
 * Created by karashevich on 09/09/15.
 */
public class SdkProblemDialog extends DialogWrapper {
    private final Project myProject;
    private final Callback myCallback;

    private StateRestoringCheckBox myCbOpenProjectSdkPreferences;


    public interface Callback {
        void run(SdkProblemDialog dialog);
    }

    public SdkProblemDialog(Project project, Callback callback) {
        super(project, true);
        myProject = project;
        myCallback = callback;
        setTitle(EducationBundle.message("dialog.invalidSdk.title"));
        init();
    }


    @Override
    @NotNull
    protected Action[] createActions() {
        return new Action[]{getOKAction(), getCancelAction()};
    }

//    @Override
//    protected void doHelpAction() {
//        HelpManager.getInstance().invokeHelp("refactoring.safeDelete");
//    }

    @Override
    protected JComponent createNorthPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();

        final String warningMessage = EducationBundle.message("dialog.invalidSdk.message", "at least JDK 1.6 or IDEA SDK with corresponding JDK");

        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(warningMessage), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        myCbOpenProjectSdkPreferences = new StateRestoringCheckBox();
        myCbOpenProjectSdkPreferences.setText(EducationBundle.message("dialog.invalidSdk.checkbox"));
        panel.add(myCbOpenProjectSdkPreferences, gbc);


        myCbOpenProjectSdkPreferences.setSelected(true);

        return panel;
    }



    @Override
    protected JComponent createCenterPanel() {
        return null;
    }


    @Override
    protected void doOKAction() {
        if (DumbService.isDumb(myProject)) {
            Messages.showMessageDialog(myProject, "Changing Project SDK is not available while indexing is in progress", "Indexing", null);
            return;
        }

        if (myCbOpenProjectSdkPreferences != null && myCbOpenProjectSdkPreferences.isSelected()) {
            ProjectSettingsService.getInstance(myProject).chooseAndSetSdk();
            super.doOKAction();
        }

//        if (myCallback != null && isSafeDelete()) {
//            myCallback.run(this);
//        } else {
//            super.doOKAction();
//        }
//
//        final RefactoringSettings refactoringSettings = RefactoringSettings.getInstance();
//        if (myCbSafeDelete != null) {
//            refactoringSettings.SAFE_DELETE_WHEN_DELETE = myCbSafeDelete.isSelected();
//        }
//        if (isSafeDelete()) {
//            if (myDelegate == null) {
//                refactoringSettings.SAFE_DELETE_SEARCH_IN_COMMENTS = isSearchInComments();
//                if (myCbSearchTextOccurrences != null) {
//                    refactoringSettings.SAFE_DELETE_SEARCH_IN_NON_JAVA = isSearchForTextOccurences();
//                }
//            } else {
//                myDelegate.setToSearchInComments(myElements[0], isSearchInComments());
//
//                if (myCbSearchTextOccurrences != null) {
//                    myDelegate.setToSearchForTextOccurrences(myElements[0], isSearchForTextOccurences());
//                }
//            }
//        }
    }

}
