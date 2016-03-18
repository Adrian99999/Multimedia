package utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javafx.scene.media.Media;

public final class MediaUtil {
	public static String getMediaFileName(Media media)
	{
		try {
			return URLDecoder.decode((new File(media.getSource())).getName(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "inconu";
		}
	}
}
