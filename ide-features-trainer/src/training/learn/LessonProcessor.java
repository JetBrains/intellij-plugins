package training.learn;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import training.commands.Command;
import training.commands.CommandFactory;
import training.commands.ExecutionList;
import training.util.PerformActionUtil;
import training.editor.MouseListenerHolder;
import training.editor.actions.HideProjectTreeAction;
import training.ui.Message;
import training.util.XmlUtil;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by karashevich on 30/01/15.
 */
public class LessonProcessor {

    @TestOnly
    public static ExecutionList getCurrentExecutionList() {
        return currentExecutionList;
    }

    private static ExecutionList currentExecutionList;

    public static void process(final Project project, final Lesson lesson, final Editor editor, @Nullable String target) throws Exception {

        HashMap<String, String> editorParameters = new HashMap<>();

        Queue<Element> elements = new LinkedBlockingQueue<>();
        if (lesson.getScn() == null) {
            System.err.println("Scenario is empty or cannot be read!");
            return;
        }

        final Element root = lesson.getScn().getRoot();

        if (root == null) {
            System.err.println("Scenario is empty or cannot be read!");
            return;
        }

        //getEditor parameters
        getEditorParameters(root, editorParameters);

        //Create queue of Actions
        for (final Element el : root.getChildren()) {
            //if element is MouseBlocked (blocks all mouse events) than add all children inside it.
            if (isMouseBlock(el)) {
                if (el.getChildren() != null) {
                    elements.add(el); //add block element
                    for (Element el1 : el.getChildren()) {
                        if (isCaretBlock(el1)) {
                            if (el1.getChildren() != null) {
                                elements.add(el1); //add block element
                                for (Element el2 : el1.getChildren()) {
                                    elements.add(el2); //add inner elements
                                }
                                elements.add(new Element(Command.CommandType.CARETUNBLOCK.toString())); //add unblock element
                            }
                        } else {
                            elements.add(el1); //add inner elements
                        }
                    }
                    elements.add(new Element(Command.CommandType.MOUSEUNBLOCK.toString())); //add unblock element
                }
            } else if (isCaretBlock(el)) {
                if (el.getChildren() != null) {
                    elements.add(el); //add block element
                    for (Element el1 : el.getChildren()) {
                        elements.add(el1); //add inner elements
                    }
                    elements.add(new Element(Command.CommandType.CARETUNBLOCK.toString())); //add unblock element
                }
            } else {
                elements.add(el);
            }
        }

        MouseListenerHolder mouseListenerHolder = new MouseListenerHolder();

        //Initialize lesson in the editor

        LessonManager lessonManager = LessonManager.getInstance(lesson);
        if (lessonManager != null) {
            lessonManager.initLesson(editor);
        } else {
            throw new Exception("Unable to get LessonManager");
        }

        //Prepare environment before execution
        prepareEnvironment(editor, project, editorParameters);

        //Perform first action, all next perform like a chain reaction
        Command cmd = CommandFactory.buildCommand(elements.peek());
        ExecutionList executionList = new ExecutionList(elements, lesson, project, editor, mouseListenerHolder, target);
        currentExecutionList = executionList;

        cmd.execute(executionList);

    }


    private static void getEditorParameters(Element root, HashMap<String, String> editorParameters) {
        if (root.getAttribute(Lesson.EditorParameters.PROJECT_TREE) != null) {
            editorParameters.put(Lesson.EditorParameters.PROJECT_TREE, root.getAttributeValue(Lesson.EditorParameters.PROJECT_TREE));
        }
    }

    private static void prepareEnvironment(Editor editor, Project project, HashMap<String, String> editorParameters) throws ExecutionException, InterruptedException {
        if (editorParameters.containsKey(Lesson.EditorParameters.PROJECT_TREE)) {
            if (ActionManager.getInstance().getAction(HideProjectTreeAction.actionId) == null) {
                HideProjectTreeAction hideAction = new HideProjectTreeAction();
                ActionManager.getInstance().registerAction(hideAction.getActionId(), hideAction);
            }
            PerformActionUtil.performAction(HideProjectTreeAction.actionId, editor, project);
        }
    }

    private static boolean isMouseBlock(Element el) {
        return el.getName().toUpperCase().equals(Command.CommandType.MOUSEBLOCK.toString());
    }

    private static boolean isCaretBlock(Element el) {
        return el.getName().toUpperCase().equals(Command.CommandType.CARETBLOCK.toString());
    }

    public static String takeDescriptionsOnly(Lesson lesson) {
        StringBuilder sb = new StringBuilder();
        Queue<Element> elements = new LinkedBlockingQueue<>();
        if (lesson.getScn() == null) {
            System.err.println("Scenario is empty or cannot be read!");
            return null;
        }

        final Element root = lesson.getScn().getRoot();

        if (root == null) {
            System.err.println("Scenario is empty or cannot be read!");
            return null;
        }


        //Create queue of Actions
        for (final Element el : root.getChildren()) {
            //if element is MouseBlocked (blocks all mouse events) than add all children inside it.
            if (isMouseBlock(el)) {
                if (el.getChildren() != null) {
                    elements.add(el); //add block element
                    for (Element el1 : el.getChildren()) {
                        if (isCaretBlock(el1)) {
                            if (el1.getChildren() != null) {
                                elements.add(el1); //add block element
                                for (Element el2 : el1.getChildren()) {
                                    elements.add(el2); //add inner elements
                                }
                                elements.add(new Element(Command.CommandType.CARETUNBLOCK.toString())); //add unblock element
                            }
                        } else {
                            elements.add(el1); //add inner elements
                        }
                    }
                    elements.add(new Element(Command.CommandType.MOUSEUNBLOCK.toString())); //add unblock element
                }
            } else if (isCaretBlock(el)) {
                if (el.getChildren() != null) {
                    elements.add(el); //add block element
                    for (Element el1 : el.getChildren()) {
                        elements.add(el1); //add inner elements
                    }
                    elements.add(new Element(Command.CommandType.CARETUNBLOCK.toString())); //add unblock element
                }
            } else {
                elements.add(el);
            }
        }

        while (elements.size() > 0) {
            final Element polledElement = elements.poll();
            if (polledElement.getAttribute("description") != null) {
                String htmlText = polledElement.getAttribute("description").getValue();
                final Message[] messages = XmlUtil.extractAll(new Message[]{new Message(htmlText, Message.MessageType.TEXT_REGULAR)});
                StringBuilder messageString = new StringBuilder();
                for (Message message : messages) {
                    messageString.append(message.getText());
                }
                sb.append(messageString.toString()).append("\n");

            }
        }

        return sb.toString();
    }

}
