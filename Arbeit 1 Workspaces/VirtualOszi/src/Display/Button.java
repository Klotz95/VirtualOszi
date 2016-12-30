package Display;

import Settings.ButtonType;
import Settings.Settings;
import Settings.GuiListenerSingleton;
import Settings.LangSing;

import java.awt.*;
import java.util.Objects;


public class Button {
	private int x, y, xSize,ySize;
	private Settings bData;



	public Button(Settings bData) {
		this.bData = bData;

	}


	public Settings getData() {
		return this.bData;
	}


	public int getButtonNo() {
		return bData.buttonNo;
	}


	//set button size and position
	void setSize(int x, int y, int xSize, int ySize) {
		this.x = x;
		this.y = y;

		switch (bData.type) {
		case MENU_BUTTON:
			bData.buttonMenu.setxPos(this.x);
			break;
		default:
			break;
		}

		this.xSize = xSize;
		this.ySize = ySize;
	}


	//handle menu ticker
	void changeValue(int sign) {
		switch (bData.type) {
		case VALUE_BUTTON:
			GuiListenerSingleton.getInstance().handleTickButtonEvent(bData,sign);
			break;

		case TRIPLE_CHECK_BUTTON:
			this.changeTrippleCheckButton();
			break;

		case MENU_BUTTON:
			if(bData.buttonMenu.getVisible() == false) {
				bData.buttonMenu.setVisible(true);				
			}
			switch (sign) {
			case -1:
				bData.buttonMenu.listUp();
				break;
			case 1:
				bData.buttonMenu.listDown(false);
				break;
			}
			bData.buttonMenu.setInvisibleTimer();
			break;
		default:
			break;
		}
	}


	//handle events if button marked
	public void setMarked() {

		bData.marked = true;

		switch(bData.type) {
		case CHECKBOX_BUTTON:
			//System.err.println("Checkbox Button ");
			this.bData.set(!this.bData.getBool());
			GuiListenerSingleton.getInstance().handleToggleButtonEvent(bData);
			break;

		case TRIPLE_CHECK_BUTTON:
			this.changeTrippleCheckButton();
			break;

		case MENU_BUTTON:
			bData.buttonMenu.setInvisibleTimer();
			bData.buttonMenu.setVisible(true);
			bData.buttonMenu.listDown(true);
			//System.err.println("Menu Button ");
			//GuiListenerSingleton.getInstance().handleMenuButtonEvent(bData);
			break;

		case REF_BUTTON:
			OsciDisplayMenu.getInstance().setButtonList(bData.buttonArrayId);
			break;

		case EVENT_BUTTON:
			GuiListenerSingleton.getInstance().handleClickButtonEvent(bData);
			break;
		default:
			break;
		}
	}


	//handle menu ticker click
	public void tickerClick() {
		switch(bData.type) {
		case MENU_BUTTON:
			bData.buttonMenu.setVisible(! bData.buttonMenu.getVisible());
			break;
		default:
			break;
		}
	}


	//handle trippleCheckButton event
	private void changeTrippleCheckButton() {
		for(int i = 0; i < bData.waves.length; i++) {
			if(Objects.equals(this.bData.get(), bData.waves[i].name())) {
				if(i+1 == bData.waves.length) {
					this.bData.set(bData.waves[0].name());
				} else {
					this.bData.set(bData.waves[i + 1].name());
				}

				if(Objects.equals(this.bData.get(), "EMPTY")) {
					changeTrippleCheckButton();
				}

				break;
			}
		}
	}


	public void setUnmarked() {
		bData.marked = false;

		if(bData.type == ButtonType.MENU_BUTTON) {
			bData.buttonMenu.setVisible(false); //??
		}
	}


	private void drawMenuTickerSign(Graphics2D g2) {
		if(bData.marked) {
			g2.setPaint(Color.GREEN);
			g2.setFont(new Font("default", Font.PLAIN, 14));
			g2.drawString("\u21BB", x + 4, y + 12);
			g2.setFont(new Font("default", Font.PLAIN, 11));
		} else {
			g2.setPaint(Color.LIGHT_GRAY);
			g2.setFont(new Font("default", Font.PLAIN, 14));
			g2.drawString("\u21BB", x + 4, y + 12);
			g2.setFont(new Font("default", Font.PLAIN, 11));
		}
	}


	public void paint(Graphics2D g2) {
		if(bData.visible) {
			//background
			g2.setStroke(new BasicStroke(1));
			g2.setPaint(Color.GRAY);
			g2.fillRoundRect(x, y, xSize, ySize, 10, 10);

			//set font
			g2.setFont(new Font("default", Font.PLAIN, 11));
			
			String text=LangSing.getInst().getTranslation(bData);
			
			switch(bData.type) {
			case VALUE_BUTTON:
				//menu ticker sign
				drawMenuTickerSign(g2);

				//header text
				g2.setPaint(Color.WHITE);
				g2.drawString(text, x+22, y+12);

				//settings text
				g2.drawString(bData.get() + " " + bData.getUnit(), x+(xSize/3), y+30);
				break;

			case CHECKBOX_BUTTON:
				//header text
				g2.setPaint(Color.WHITE);
				g2.drawString(text, x+4, y+12);

				//check box
				g2.drawRect(x+(xSize/2)-5, y+(ySize/2), 10, 10);

				if(bData.getBool()) {
					g2.fillRect(x+(xSize/2)-5, y+(ySize/2), 10, 10);
				}

				break;

			case TRIPLE_CHECK_BUTTON:
				g2.setPaint(Color.WHITE);

				int xPos = this.x+10;


				for(int i = 0; i < bData.waves.length; i++) {
					if(bData.waves[i].isImage()) {
						g2.drawImage(bData.waveFormsImages[i], xPos, y + 5, 15, 15, null);
					} else {
						g2.drawString(bData.waves[i].getSymbol(), xPos, y + 15);
					}

					if(Objects.equals(bData.get(), bData.waves[i].name())) {
						g2.drawString("\u2714", xPos+3, y+30);
					}

					xPos += 30;
				}

				break;

			case MENU_BUTTON:
				//menu ticker sign
				drawMenuTickerSign(g2);

				//header text
				g2.setPaint(Color.WHITE);
				g2.drawString(text, x+22, y+12);
				//menu setting
				if(this.getData().get().length() > 8) {
					g2.drawString(this.getData().get().substring(0, 8) + "...", x+22, y+30);
				} else {
					g2.drawString(this.getData().get(), x+22, y+30);
				}

				bData.buttonMenu.paint(g2);

				break;

			case REF_BUTTON:
				//header text
				g2.setPaint(Color.WHITE);
				g2.drawString(text, x+4, y+12);
				g2.setFont(new Font("default", Font.BOLD, 16));
				g2.drawString("\u21e9", x+40, y+30);
				g2.setFont(new Font("default", Font.PLAIN, 11));
				break;

			case EVENT_BUTTON:
				//header text
				g2.setPaint(Color.WHITE);

				String lines[] = text.split("\n");

				g2.drawString(lines[0], x+16, y+14);

				if(lines.length > 1) {
					g2.drawString(lines[1], x+16, y+28);
				}

				break;
			default:
				break;
			}

			if(bData.marked) {
				g2.setPaint(Color.WHITE);
				g2.drawRoundRect(x, y, xSize, ySize, 10, 10);
			}
		}
	}
}
