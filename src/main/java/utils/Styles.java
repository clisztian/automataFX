package utils;

import java.io.InputStream;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Styles {

	

	
	public static final Font bgFont = Font.loadFont(Styles.class.getClassLoader().getResourceAsStream("fonts/static/Exo-Medium.ttf"), 16);	
		
	public final static String IDLE_BUTTON_STYLE = " -fx-effect: dropshadow( gaussian , rgb(245, 64, 57) , 10 , .2 , 0, 0 );";
	public final static String HOVERED_BUTTON_STYLE = " -fx-effect: dropshadow( gaussian , rgb(245, 64, 57) , 35 , .5 , 0, 0 );";
	public final static String DOWN_BUTTON_STYLE = " -fx-effect: dropshadow( gaussian , rgb(83, 210, 200) , 35 , .5 , 0, 0 );";
	
	public final static Color base_color = Color.rgb(64, 200, 227);
}
