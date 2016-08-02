package training.ui;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;


/**
 * Created by karashevich on 03/09/15.
 */
public class LearnIcons {

    private static Icon load(String path) {
        return IconLoader.getIcon(path, LearnIcons.class);
    }

    public static final Icon CheckmarkGray =  load("/img/checkmark.png");
    public static final Icon ChevronIcon =  load("/img/chevron.png");
    public static final Icon ChevronToolWindowIcon =  load("/img/chevron_toolwin.png");

}
