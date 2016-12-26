package name.kropp.intellij.makefile.psi;

import com.intellij.icons.AllIcons;
import com.intellij.navigation.ItemPresentation;

import javax.swing.*;

class MakefileTargetPresentation implements ItemPresentation {
    private MakefileTarget target;

    MakefileTargetPresentation(MakefileTarget target) {
        this.target = target;
    }

    @Override
    public String getPresentableText() {
        return target.getText();
    }

    @Override
    public String getLocationString() {
        return "";
    }

    @Override
    public Icon getIcon(boolean b) {
        return AllIcons.Toolwindows.ToolWindowRun;
    }
}
