package org.jetbrains.training.editor.eduUI;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by karashevich on 30/06/15.
 */
public class LessonMessage {
    @NotNull
    private JLabel message;
    @Nullable
    private ShortcutLabel shortcut;


    public LessonMessage(@NotNull JLabel message, @Nullable ShortcutLabel shortcut) {
        this.message = message;
        this.shortcut = shortcut;
    }

    public JLabel getLabel(){
        return message;
    }

    public JPanel getPanel(){
        JPanel c = new JPanel();

        c.setLayout(new BoxLayout(c, BoxLayout.LINE_AXIS));
        c.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        c.add(message);
        message.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (shortcut != null) {
            c.add(shortcut);
        }
        return c;
    }

}
