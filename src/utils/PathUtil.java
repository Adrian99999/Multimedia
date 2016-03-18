package utils;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;


public final class PathUtil {
	private static String programDir = null;
	
	public static String getProgramDir()
	{
		if(programDir == null)
		{
			try {
				String dir = (new File(Preference.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParentFile().getCanonicalPath();
				programDir = URLDecoder.decode(dir, "UTF-8");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return programDir;
	}
	
	public static String getFullPath( String programRelativePath)
	{
		return getProgramDir() + File.separator + programRelativePath;
	}
}
