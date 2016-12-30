package Display;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import Settings.OsciSettingsSingelton;
import Settings.Settings;

public class OsciDisplayMenu {

	private int ySizePanel;
	private int buttonCount = 6;
	private int xSizeButton = 100;
	private int xSpaceButton = 10;
	private int intesityValue;
	private int buttonNo = 0;
	private int refButtonCount = 0;

	private boolean goBack = false;

	private List<Settings> oldButtonArray = new ArrayList<>();

	private Button markedButton;

	private int[] xPos = new int[6];

	private OsciSettingsSingelton buttonData = OsciSettingsSingelton.getInstance();

	private List<Button> buttons = new ArrayList<>();
	private List<Button> buttonsTmp = new ArrayList<>();


	private static final class InstanceHolder {
		static final OsciDisplayMenu INSTANCE = new OsciDisplayMenu();
	}


	public static OsciDisplayMenu getInstance () {
		return InstanceHolder.INSTANCE;
	}
	
	
	private OsciDisplayMenu() {}
	

	public void setPanelHeigth(int y) {
		this.ySizePanel = y;
	}


	public void init() {
		int xPos = xSpaceButton;

		for( int i = 0; i < buttonCount; i++ ) {
			this.xPos[i] = xPos;
			xPos += xSizeButton + xSpaceButton;
		}
	}


	//set one button marked
	public void setMarked(int buttonNumber) {
		for(int i = 0; i < buttons.size(); i++) {
			if(markedButton != null) {
				if (markedButton.getButtonNo() != buttonNumber) {
					buttons.get(i).setUnmarked();
				}
			} else {
				buttons.get(i).setUnmarked();
			}
		}

		for(int i = 0; i < buttons.size(); i++) {
			if(buttons.get(i).getButtonNo() == buttonNumber) {
				markedButton = buttons.get(i);
				buttons.get(i).setMarked();

				break;
			}
		}
	}


	//handle menu ticker event
	public void ticker(int sign) {
		if(Settings.INTENSITY.getBool()) {
			intesityValue = Settings.INTENSITY_VALUE.getInt();

			if(intesityValue >= 0 && intesityValue <= 100) {
				if((intesityValue + sign) != -1 && (intesityValue += sign) != 101) {
					Settings.INTENSITY_VALUE.set(intesityValue += sign);
				}
			}
		} else {
			if(markedButton != null) {
				markedButton.changeValue(sign);
			}
		}
	}


	//handle menu ticker click event
	public void tickerClick() {
		if(markedButton != null) {
			markedButton.tickerClick();
		}
	}


	//set button array for menu button
	public void setButtonList(Settings bId, int menuButtonNo) {
		if(bId == null) { return; }

		switch (bId.type) {
			case BUTTON_ARRAY:
				Button menuButton = null;
				boolean menuButtonSet = false;

				for(int i = 0; i < buttons.size(); i++) {
					if(buttons.get(i).getButtonNo() == menuButtonNo) {
						menuButton = buttons.get(i);
						break;
					}
				}

				buttons = buttonData.getButtonList(bId);

				for(int i = 0; i < buttons.size(); i++) {
					if(buttons.get(i).getButtonNo() == menuButtonNo) {
						buttons.set(i, menuButton);
						menuButtonSet = true;
						break;
					}
				}

				if(menuButtonSet == false) {
					buttons.add(menuButton);
				}

				for( int i = 0; i < buttons.size(); i++ ) {
					buttonNo = buttons.get(i).getButtonNo();
					buttons.get(i).setSize(xPos[buttonNo], this.ySizePanel-45, xSizeButton, 35);
				}
			break;

			case BUTTON_ARRAY_NO_OVERWRITE:
				boolean buttonSet = false;
				buttonsTmp = buttonData.getButtonList(bId);


				for(int j = 0; j < buttonsTmp.size(); j++) {
					buttonSet = false;

					for(int i = 0; i < buttons.size(); i++) {
						if (buttons.get(i).getButtonNo() == buttonsTmp.get(j).getButtonNo()) {
							buttons.set(i, buttonsTmp.get(j));
							buttonSet = true;
						}
					}

					if(!buttonSet) {
						buttons.add(buttonsTmp.get(j));
					}
				}


				for( int i = 0; i < buttons.size(); i++ ) {
					buttonNo = buttons.get(i).getButtonNo();
					buttons.get(i).setSize(xPos[buttonNo], this.ySizePanel-45, xSizeButton, 35);
				}
			break;
			default:
				break;
		}

		/*
		System.out.print("menu count: " + menuButtonCount + "\n\n");

		//button array for marked menu entry
		for(int i = 0; i < buttons.size(); i++) {
			if(buttons.get(i).getButtonNo() >= menuButtonCount) {
				switch (buttons.get(i).getData().type) {
					case MENU_BUTTON:
						menuButtonCount++;
						//System.out.print(buttons.get(i).getData().headerText + "\n\n");
						setButtonList(buttons.get(i).getData().buttonMenu.getMarkedMenuArray(), buttons.get(i).getData().buttonNo);
						break;
				}
			}
		}
		*/
	}


	public void clickBackButton() {
		System.out.print("Size: " + oldButtonArray.size() + "\n");

		if(oldButtonArray.size() > 0 && refButtonCount > 0) {
			int size = oldButtonArray.size();

			goBack = true;
			setButtonList(oldButtonArray.get(size - 2));
			goBack = false;

			oldButtonArray.remove(oldButtonArray.size() - 1);
		}

		if(refButtonCount == 0) {
			System.out.print("\nBACK\n");
		}

		if(refButtonCount > 0) {
			refButtonCount--;
		}
	}


	//set new button array and clear back button cache
	public void  setNewButtonList(Settings bId) {
		oldButtonArray.clear();
		refButtonCount = 0;

		setButtonList(bId);
	}


	//set button list
	public void setButtonList(Settings bId) {
		//count menu levels
		if(markedButton != null) {
			switch (markedButton.getData().type) {
				case REF_BUTTON:
					refButtonCount++;
					break;
				default:
					break;
			}
		}

		//set old button array for back button
		if(!goBack) { oldButtonArray.add(bId); }

		//set old buttons unmarked
		for(int i = 0; i < buttons.size(); i++) {
			buttons.get(i).setUnmarked();
		}
		markedButton = null;

		buttons = buttonData.getButtonList(bId);

		int buttonNo;

		for( int i = 0; i < buttons.size(); i++ ) {
			buttonNo = buttons.get(i).getButtonNo();
			buttons.get(i).setSize(xPos[buttonNo], this.ySizePanel-45, xSizeButton, 35);
		}

		//button array for marked menu entry
		for(int i = 0; i < buttons.size(); i++) {
			switch(buttons.get(i).getData().type) {
				case MENU_BUTTON:
					//System.out.print(buttons.get(i).getData().headerText + "\n\n");
					setButtonList(buttons.get(i).getData().buttonMenu.getMarkedMenuArray(), buttons.get(i).getData().buttonNo);
					break;
				default:
					break;
			}
		}
	}

	//draw buttons and menus
	public void paint(Graphics2D g2) {
		for(int i = 0; i < buttons.size(); i++) {
			buttons.get(i).paint(g2);
		}

		//intesity
		if(Settings.INTENSITY.getBool()) {
			g2.setColor(Color.GRAY);
			g2.fillRoundRect(430, 60, 60, 60, 60, 60);

			g2.setColor(Color.WHITE);
			g2.drawRoundRect(430, 60, 60, 60, 60, 60);

			g2.drawString(Settings.INTENSITY_VALUE.get()+Settings.INTENSITY_VALUE.getUnit(), 450, 95);
		}
	}
}
	