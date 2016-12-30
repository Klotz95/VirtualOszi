package Settings;

import Display.Button;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;


public class OsciSettingsSingelton {
	private List<Button> buttonList =  new ArrayList<>();
	private List<Button> buttonTmpList = new ArrayList<>();

	private String settingsGroup = "";

	private Properties prop = new Properties();
	private String settingsFile = "config.properties";


	private static final class InstanceHolder {
		static final OsciSettingsSingelton INSTANCE = new OsciSettingsSingelton();
	}


	public static OsciSettingsSingelton getInstance () {
		return InstanceHolder.INSTANCE;
	}


	private OsciSettingsSingelton () {
		//save settings on exit
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				saveProp();
			}
		},"ShutdownHook"));
	}


	//add button data to button array
	private void initButtonArray() {
		for( Settings bData : Settings.values() ) {

			//set first menu entry as default value
			if(bData.type == ButtonType.MENU_BUTTON) {
				if(Objects.equals(bData.get(), "")) {
					bData.set(bData.buttonMenu.getFirstEntry());
				}
			}

			if(bData.type == ButtonType.BUTTON_ARRAY || bData.type == ButtonType.BUTTON_ARRAY_NO_OVERWRITE) {
				settingsGroup = bData.settingGroup;
			}

			buttonList.add(new Button(bData));
			bData.settingGroup = settingsGroup;
		}

		for( Settings bData : Settings.values() ) {
			if(bData.type == ButtonType.MENU_BUTTON) {
				bData.buttonMenu.setMenuPos();
			}
		}
	}


	//get button array (6 buttons)
	public List<Button> getButtonList(Settings buttonArrayId) {
		buttonTmpList.clear();

		for(int i = buttonArrayId.ordinal() + 1; i < buttonArrayId.ordinal() + 7; i++) {
			if(i == buttonList.size()) { break; }

			if(Objects.equals(buttonList.get(i).getData().settingGroup, buttonArrayId.settingGroup) && buttonList.get(i).getData().type != ButtonType.BUTTON_ARRAY && buttonList.get(i).getData().type != ButtonType.BUTTON_ARRAY_NO_OVERWRITE) {
				buttonTmpList.add(buttonList.get(i));
			} else {
				break;
			}
		}

		return new ArrayList<Button>(buttonTmpList);
	}


	//load all settings from disk
	public void loadProp() {
		InputStream input = null;

		try {
			input = new FileInputStream(settingsFile);

			if(input != null) {
				prop.load(input);
			}

			//load all settings
			for(Settings s : Settings.values()) {
				if(s.isSetting) {
					String propStr = prop.getProperty(s.name());

					if(propStr != null) {
						s.set(propStr);
					}
				}
			}

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	//load properties and set button array
	public void init() {
		loadProp();
		initButtonArray();
	}


	//save all settings to disk
	public void saveProp() {
		OutputStream output = null;

		try {
			output = new FileOutputStream(settingsFile);

			//save all settings
			for(Settings s : Settings.values()) {
				if(s.isSetting) {
					prop.setProperty(s.name(), s.get());
				}
			}

			// save properties
			prop.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

