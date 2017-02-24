package name.kropp.intellij.makefile.psi;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MakefilePsiImplUtil {
    private static final Pattern suffixRule = Pattern.compile("^\\.[a-zA-Z]+(\\.[a-zA-Z]+)$");

    public static List<MakefileTarget> getTargets(MakefileRule element) {
        return element.getTargetLine().getTargets().getTargetList();
    }

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
        if (element.isSpecialTarget()) return null;

        ASTNode targetNode = element.getNode();
        return targetNode != null ? targetNode.getPsi() : null;
    }

    public static ItemPresentation getPresentation(MakefileTarget element) {
        return new MakefileTargetPresentation(element);
    }

    public static boolean isSpecialTarget(MakefileTarget element) {
        String name = element.getName();
        return name != null && (name.matches("^\\.[A-Z_]*") || name.equals("FORCE") || suffixRule.matcher(name).matches());
    }

    public static boolean isPatternTarget(MakefileTarget element) {
        String name = element.getName();
        return name != null && name.contains("%");
    }

    public static boolean matches(MakefileTarget element, String prerequisite) {
        String name = element.getName();
        if (name == null) {
            return false;
        }
        if (name.startsWith("%")) {
            return prerequisite.endsWith(name.substring(1));
        }
        if (name.endsWith("%")) {
            return prerequisite.startsWith(name.substring(0, name.length()-1));
        }
        Matcher matcher = suffixRule.matcher(name);
        if (matcher.matches()) {
            return prerequisite.endsWith(matcher.group(1));
        }
        return name.equals(prerequisite);
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

    public static boolean isEmpty(MakefileRecipe element) {
        return element.getCommandList().isEmpty() && element.getConditionalList().isEmpty();
    }
}