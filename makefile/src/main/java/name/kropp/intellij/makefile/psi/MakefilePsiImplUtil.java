package name.kropp.intellij.makefile.psi;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import kotlin.text.Regex;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

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

    public static ItemPresentation getPresentation(MakefileTarget element) {
        return new MakefileTargetPresentation(element);
    }

    public static boolean isSpecialTarget(MakefileTarget element) {
        String name = element.getName();
        return name != null && name.matches("^.[A-Z]*");
    }

    private static TokenSet ASSIGNMENT = TokenSet.create(MakefileTypes.ASSIGN);
    private static TokenSet LINE = TokenSet.create(MakefileTypes.LINE);
    private static TokenSet VARIABLE_VALUE_LINE = TokenSet.create(MakefileTypes.VARIABLE_VALUE_LINE);

    @Nullable
    public static PsiElement getAssignment(MakefileVariableAssignment element) {
        ASTNode node = element.getNode().findChildByType(ASSIGNMENT);
        if (node == null)
            return null;
        return node.getPsi();
    }

    @Nullable
    public static String getValue(MakefileVariableAssignment element) {
        MakefileVariableValue value = element.getVariableValue();
        if (value == null) {
            return "";
        }
        ASTNode[] nodes = value.getNode().getChildren(LINE);
        return Arrays.stream(nodes).map(ASTNode::getText).collect(Collectors.joining("\n"));
    }

    @Nullable
    public static PsiElement getAssignment(MakefileDefine element) {
        ASTNode node = element.getNode().findChildByType(ASSIGNMENT);
        if (node == null)
            return null;
        return node.getPsi();
    }

    @Nullable
    public static String getValue(MakefileDefine element) {
        ASTNode[] nodes = element.getNode().getChildren(VARIABLE_VALUE_LINE);
        return Arrays.stream(nodes).map(ASTNode::getText).collect(Collectors.joining("\n"));
    }
}