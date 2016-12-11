package name.kropp.intellij.makefile.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public class MakefilePsiImplUtil {
    @Nullable
    public static String getTargetName(MakefileTargetLine element) {
        ASTNode targetNode = element.getNode().findChildByType(MakefileTypes.TARGET);
        if (targetNode == null) {
            return null;
        }
        return targetNode.getText();
    }

    @Nullable
    public static String getName(MakefileTarget element) {
        return element != null ? element.getText() : null;
    }

    public static PsiElement setName(MakefileTarget element, String newName) {
        ASTNode identifierNode = element.getNode().getFirstChildNode();
        if (identifierNode != null) {
            MakefileTarget target = MakefileElementFactory.INSTANCE.createTarget(element.getProject(), newName);
            ASTNode newIdentifierNode = target.getFirstChild().getNode();
            element.getNode().replaceChild(identifierNode, newIdentifierNode);
        }
        return element;
    }

    @Nullable
    public static PsiElement getNameIdentifier(MakefileTarget element) {
        ASTNode targetNode = element.getNode();
        return targetNode != null ? targetNode.getPsi() : null;
    }
}