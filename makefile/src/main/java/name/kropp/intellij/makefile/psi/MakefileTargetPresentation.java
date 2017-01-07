package name.kropp.intellij.makefile.psi;

import com.intellij.navigation.ItemPresentation;
import name.kropp.intellij.makefile.MakefileFileTypeKt;

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
        return "in " + target.getContainingFile().getVirtualFile().getPresentableName();
    }

    @Override
    public Icon getIcon(boolean b) {
        return MakefileFileTypeKt.getMakefileTargetIcon();
    }
}
