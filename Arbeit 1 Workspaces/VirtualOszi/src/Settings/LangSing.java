package Settings;

import java.util.Locale;
import java.util.ResourceBundle;

public class LangSing {
	
	private static final class InstanceHolder {
		static final LangSing INSTANCE = new LangSing();
	}


	public static LangSing getInst() {
		return InstanceHolder.INSTANCE;
	}

	private LangSing() {
		Update(Settings.A_HELP_LANGUAGE.get());

	}
	
	public static ResourceBundle Update(String newLanguage){
		if(newLanguage.equals("Deutsch")){
			return ResourceBundle.getBundle("resource.language", Locale.GERMAN);
		}
		else{
			return ResourceBundle.getBundle("resource.language", Locale.ENGLISH);						
		}
	}
	
	public String getTranslation(Settings id){
		try{
			return Update(Settings.A_HELP_LANGUAGE.get()).getString(id.name());			
		}
		catch(Exception e){
			return id.headerText;
		}
	}
	
}
