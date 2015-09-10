package training.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.graphics.DetailPanel;

import java.awt.*;
import java.io.IOException;

/**
 * Created by karashevich on 26/02/15.
 */
public class TextUtil {

    public static String ROBOTO = "roboto";


    @Nullable
    public static Font registerFontAsFontFamily(@NotNull String fontFamily, @NotNull final Font[] fonts){
        if (fonts == null) return null;
        if (fonts.length == 0) return null;

        for (Font font1 : fonts) {
//            Map<TextAttribute, Object> textAttributeMap = (Map<TextAttribute, Object>) font1.getAttributes();
//            textAttributeMap.put(TextAttribute.FAMILY, fontFamily);
//            Font font = new Font(textAttributeMap);
//            font1.
//            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font1);
            System.err.println("font-name: " + font1.getName());
            System.err.println("font-family: " + font1.getFamily());
            System.err.println("font isBold: " + font1.isPlain());
            System.err.println("font isRegular: " + font1.isBold());
            System.err.println("font-style: " + font1.getStyle());
        }

        return (new Font(fontFamily, Font.PLAIN, 14));
    }

    public static Font registerRoboto() throws IOException, FontFormatException {
        String robotoRegPath = "roboto.ttf";
        String robotoBoldPath = "robotoBold.ttf";

        Font robotoRegular = Font.createFont(Font.TRUETYPE_FONT, DetailPanel.class.getResourceAsStream(robotoRegPath));
        Font robotoBold = Font.createFont(Font.TRUETYPE_FONT, DetailPanel.class.getResourceAsStream(robotoBoldPath));

        Font[] fonts = {robotoRegular, robotoBold};

        return registerFontAsFontFamily(ROBOTO, fonts);
    }

}
