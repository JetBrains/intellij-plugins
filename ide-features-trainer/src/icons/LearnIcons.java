package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;


/**
 * Created by karashevich on 03/09/15.
 */
public class LearnIcons {

    private static Icon load(String path) {
        return IconLoader.getIcon(path, LearnIcons.class);
    }

    public static final Icon CheckmarkGray =  load("/img/checkmarkGray.png");
    public static final Icon CheckmarkBlue =  load("/img/checkmarkBlue.png");
    public static final Icon CheckmarkDarkgray = load("/img/checkmarkDarkGray.png");
    public static final Icon CheckmarkGray12 =  load("/img/checkmark12.png");

    public static class Toolwindows {
        public static final Icon LearnToolWindowIcon = load("/icons/toolwindows/chevron.png");
    }
}
