package utils;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

import utils.PathUtil;

@XmlRootElement
public class Preference {
	public String songFile = null;
	public double statgeWidth = -1;
	public double stageHeight = -1;
	public double volume = 50;
	public boolean checkRejouerSelect = false;
	
	private static final String FILE = ".preference";
	private static File file = new File(PathUtil.getFullPath(FILE));
	
//	static{
//		file = new File(PathUtil.getFullPath(FILE));
//	}
	
	public void stoker()
	{
		if(file == null) return;
		{
			try {
				Marshaller m = JAXBContext.newInstance(Preference.class).createMarshaller();
				
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				m.marshal(this, file);
				
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static Preference charger()
	{
		if(file == null || !file.exists()) return null;
		try {
			return (Preference) JAXBContext.newInstance(Preference.class).createUnmarshaller().unmarshal(file);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
			
	}
}
