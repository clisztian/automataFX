package utils;

import javafx.scene.text.Font;

public class Fonts {
    private static final String SPACE_BOY;
    private static       String spaceBoyName;

    static {
        try {
            spaceBoyName = Font.loadFont(Fonts.class.getResourceAsStream("font/static/Exo-Medium.ttf"), 10).getName();
        } catch (Exception exception) { }
        SPACE_BOY = spaceBoyName;
    }


    public static final Font spaceBoy(final double size) { return new Font(SPACE_BOY, size); }
}
