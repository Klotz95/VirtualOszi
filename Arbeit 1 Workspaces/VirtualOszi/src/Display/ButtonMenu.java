package Display;

import Settings.GuiListenerSingleton;
import Settings.Settings;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ButtonMenu {

	private int xPos, yPos, xSize, ySize, maxEntries, ySizeMax, lineHeight, markedEntryNo, entryStart, entryEnd;

	String maxString;

	private boolean visible, visibleChanged;

	private List<String> entryList = new ArrayList<String>();

	private Settings[] buttonArray;
	private Settings bData;

	private static Timer inVisibleTimer;	

	//global?
	private int xSizePanel = 673;


	public ButtonMenu(Settings bData, List<String> entryList, Settings[] buttonArray) {
		inVisibleTimer = new Timer("InvisibleTimer");
		this.xPos = 0;
		this.yPos = 40;
		this.visible = false;
		this.maxEntries = 12;
		this.ySizeMax = 360;
		this.bData = bData;
		this.markedEntryNo = 0;
		this.buttonArray = buttonArray;
		//real line height => lineHeight *= 3;
		this.lineHeight = 10;

		setEntryList(entryList);

		//get max string length in entryList to set menu size
		int length;
		int	maxStringLength = 0;

		for(int i = 0; i < this.entryList.size(); i++) {
			length = this.entryList.get(i).length();

			if(length > maxStringLength) {
				maxStringLength = length;
				maxString = this.entryList.get(i);
			}
		}
	}

	//set active menu entry
	public void setMenuPos() {
		
		for(int i = 0; i < this.entryList.size(); i++) {
			if(Objects.equals(this.entryList.get(i), bData.get())) {
				this.markedEntryNo = i;
				break;
			}
		}

		this.entryStart = 0;

		if(this.markedEntryNo > this.entryList.size()) {
			this.markedEntryNo = 0;
		}

		for(int i = 0; i < this.markedEntryNo; i++) {
			if(entryStart < this.entryList.size() - 12) {
				entryStart++;
				entryEnd++;
			}
		}
	}


	public Settings getMarkedMenuArray() {
		if(buttonArray != null) {

			if (markedEntryNo < buttonArray.length) {
				return buttonArray[markedEntryNo];
			} else {
				return null;
			}
		} else {
			return null;
		}
	}


	public boolean getVisible() {
		return this.visible;
	}

	public void setInvisibleTimer(){
		inVisibleTimer.cancel();
		inVisibleTimer = new Timer("InvisibleTimer");
		inVisibleTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				visibleChanged = true;
				visible = false;
				inVisibleTimer.cancel();
			}
		}, 3000);
	}

	//set max height of menu
	private void setEntryList(List<String> entryList) {
		this.entryList = entryList;

		if(this.entryList.size() > maxEntries) {
			this.ySize = this.lineHeight * maxEntries * 3;
			entryEnd = maxEntries;
		} else {
			this.ySize = this.lineHeight * this.entryList.size() * 3;
			entryEnd = entryList.size();
		}

		this.yPos += ySizeMax - this.ySize;
	}


	//return frist menu entry for default setup
	public String getFirstEntry() {
		if(entryList.size() > 0) {
			return entryList.get(0);
		} else {
			return "";
		}
	}


	public void setxPos(int xPos) {
		this.xPos = xPos;
	}


	public void setVisible(boolean visible) {

		//if visibleChanged true: don't jump to next entry -> function listDown
		if(this.visible != visible) {
			this.visibleChanged = true;
			this.visible = visible;
			setEntryMarked();
		} else {
			this.visibleChanged = false;
		}
	}


	//go up to next entry in list
	public void listUp() {
		if(markedEntryNo == entryStart && entryStart != 0) {
			entryStart--;
			entryEnd--;
		}

		if (markedEntryNo != 0) {
			markedEntryNo--;
			setEntryMarked();
		}
	}


	//go down to next entry in list
	public void listDown(boolean jumpFromEndToStart) {
		//jump from end to start
		if (markedEntryNo == entryList.size() - 1 && jumpFromEndToStart == true) {
			markedEntryNo = 0;
			setEntryMarked();
			entryStart = 0;

			//set new border
			if (this.entryList.size() > maxEntries) {
				entryEnd = maxEntries;
			} else {
				this.entryEnd = this.entryList.size();
			}
			//mark next entry
			// don't jump on fist click if button clicked
		} else if(visibleChanged == false || jumpFromEndToStart == false) {
			if (markedEntryNo < entryList.size() - 1) {
				markedEntryNo++;
				setEntryMarked();
			}

			if (markedEntryNo == entryEnd && entryList.size() != entryEnd) {
				entryStart++;
				entryEnd++;
			}
		}
	}


	//mark next entry in list and set new buttons
	private void setEntryMarked() {
		bData.set(this.entryList.get(markedEntryNo));
		
		//System.err.println("Menu Button set entry marked "+this.entryList.get(markedEntryNo)+" " + this.bData.headerText);
		GuiListenerSingleton.getInstance().handleMenuButtonSetEntryMarkedEvent(this.bData, this.entryList.get(markedEntryNo));
		
		if(buttonArray != null) {
			if(buttonArray[markedEntryNo] != null) {
				OsciDisplayMenu.getInstance().setButtonList(buttonArray[markedEntryNo], bData.buttonNo);
			}
		}
	}

	//paint menu
	public void paint(Graphics2D g2) {
		if(visible) {
			//set font
			g2.setFont(new Font("default", Font.PLAIN, 11));
			//set menu width
			xSize = g2.getFontMetrics().stringWidth("\u2714 " + maxString) + 24;
			//check if menu to big
			if((xSize + xPos) > xSizePanel) {
				xPos -= (xSize + xPos) - xSizePanel + 12;
			}

			//draw menu
			g2.setColor(Color.BLACK);
			g2.fillRoundRect(xPos, yPos, xSize, ySize, 15, 15);

			g2.setColor(Color.WHITE);
			g2.drawRoundRect(xPos, yPos, xSize, ySize, 15, 15);

			int yPos = this.yPos + (2 * this.lineHeight);

			//draw all entries
			for(int i = entryStart; i < entryEnd; i++) {
				if (markedEntryNo == i) {
					g2.setColor(Color.GREEN);
					g2.drawString("\u2714 " + entryList.get(i), xPos + 10, yPos);
					g2.setColor(Color.WHITE);
				} else {
					g2.drawString("  " + entryList.get(i), xPos + 10, yPos);
				}

				yPos += lineHeight;

				if (i != entryEnd - 1) {
					g2.drawLine(xPos, yPos, xSize + xPos, yPos);
				}

				yPos += 2 * lineHeight;
			}
		}
	}
}
