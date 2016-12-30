package Settings;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


//wave form images for trippleCheckButton
public enum WaveForms {
    EMPTY("", false),
    SIN("src/images/sin.png"),
    SQUARE("src//images/square.png"),
    SQUARE_TURNED("src//images/square_turned.png"),
    TRIANGLE("src//images/triangular.png"),
    UpDownArrow("\u21C5", false),
    UpDownArrowDouble("\u2195", false),
    UpArrow("\u2191", false),
    DownArrow("\u2193", false),
    LeftArrow(">", false),
    RightArrow("<", false),
    X("X", false);


    private String fileName, symbol;
    private boolean isImage = true;


    WaveForms(String fileName) {
        this.fileName = fileName;
    }


    WaveForms(String symbol, boolean isImage) {
        this.symbol = symbol;
        this.isImage = isImage;
    }


    public boolean isImage() {
        return isImage;
    }


    public String getSymbol() {
        return this.symbol;
    }

    public BufferedImage getImage() {
        BufferedImage waveForm = null;

        try {
            waveForm = ImageIO.read(new File(this.fileName));
        } catch(IOException e) {
            e.printStackTrace();
        }

        return waveForm;
    }
};