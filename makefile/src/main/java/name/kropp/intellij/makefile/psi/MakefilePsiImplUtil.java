package name.kropp.intellij.makefile.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.Nullable;

public class MakefilePsiImplUtil {
    @Nullable
    public static String getTarget(MakefileTargetLine element) {
        ASTNode targetNode = element.getNode().findChildByType(MakefileTypes.TARGET);
        if (targetNode == null) {
            return null;
        }
        return targetNode.getText();
    }
}