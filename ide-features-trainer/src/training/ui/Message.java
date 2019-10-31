package training.ui;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.keymap.KeymapUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Message {


    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    @NotNull
    public static Message[] convert(@Nullable Element element) {
        if (element == null) {
            return new Message[0];
        }
        List<Message> list = new ArrayList<>();
        element.getContent().forEach(content -> {
            if (content instanceof Text) {
                list.add(new Message(content.getValue(), MessageType.TEXT_REGULAR));
            }
            else if (content instanceof Element) {
                XMLOutputter outputter = new XMLOutputter();
                MessageType type = MessageType.TEXT_REGULAR;
                Element elementContent = (Element) content;
                String text = outputter.outputString(elementContent.getContent());
                text = StringUtil.unescapeXmlEntities(text);
                String link = null;
                switch (elementContent.getName()) {
                    case "icon":
                        type = MessageType.ICON;
                        break;
                    case "code":
                        type = MessageType.CODE;
                        break;
                    case "shortcut":
                        type = MessageType.SHORTCUT;
                        break;
                    case "strong":
                        type = MessageType.TEXT_BOLD;
                        break;
                    case "a":
                        type = MessageType.LINK;
                        link = elementContent.getAttributeValue("href");
                        break;
                    case "action":
                        type = MessageType.SHORTCUT;
                        final KeyStroke shortcutByActionId = KeymapUtil.INSTANCE.getShortcutByActionId(text);
                        if (shortcutByActionId != null) {
                            text = KeymapUtil.INSTANCE.getKeyStrokeText(shortcutByActionId);
                        }else{
                            text = KeymapUtil.INSTANCE.getKeyStrokeText(KeymapUtil.INSTANCE.getShortcutByActionId("GotoAction")) + " → "+ ActionManager.getInstance().getAction(text).getTemplatePresentation().getText();
                        }
                        break;
                    case "ide":
                        type = MessageType.TEXT_REGULAR;
                        text = ApplicationNamesInfo.getInstance().getFullProductName();
                        break;
                }
                Message message = new Message(text, type);
                message.link = link;
                list.add(message);
            }
        });
        return ContainerUtil.toArray(list, new Message[0]);
    }

    public enum MessageType {TEXT_REGULAR, TEXT_BOLD, SHORTCUT, CODE, LINK, CHECK, ICON}
    @NotNull
    private String messageText;
    private int startOffset;
    private int endOffset;
    private String link;
    @Nullable
    private Runnable runnable = null;

    @NotNull
    private MessageType messageType;
    public Message(@NotNull String messageText, @NotNull MessageType messageType) {
        this.messageText = messageText;
        this.messageType = messageType;
    }

    @Nullable
    public Runnable getRunnable() {
        return runnable;
    }

    public void setRunnable(@Nullable Runnable runnable) {
        this.runnable = runnable;
    }

    public String getText() {
        return messageText;
    }

    public MessageType getType() {
        return messageType;
    }

    public boolean isText() {
        return messageType == MessageType.TEXT_REGULAR || messageType == MessageType.TEXT_BOLD;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageText='" + messageText + '\'' +
                ", messageType=" + messageType +
                '}';
    }
}
