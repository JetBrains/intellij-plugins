import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.testFramework.LightPlatformTestCase;

/**
 * Created by jetbrains on 04/08/16.
 */
public class LearnProjectTest extends LightPlatformTestCase{

    public void testLearnProject(){
        closeAndDeleteProject();

        final AnAction action = ActionManager.getInstance().getAction("learn.open.lesson");

        final DataContext context = DataContext.EMPTY_CONTEXT;
        final AnActionEvent event = AnActionEvent.createFromAnAction(action, null, "", context);

        ActionUtil.performActionDumbAware(action, event);
    }

}
