package com.ocr;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class CaptchaDecoder {
	
	public static String decode(BufferedImage image) {
		if(image == null) {
			return null;
		}
		
        Tesseract instance = Tesseract.getInstance();
        
        instance.setTessVariable("tessedit_char_whitelist", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        instance.setPageSegMode(TessAPI.TessPageSegMode.PSM_SINGLE_LINE);
        
        final int[][] OFFSET = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}, 
        		{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        
        
        for(int i = 0; i < image.getWidth(); i++) {
        	for(int j = 0; j < image.getHeight(); j++) {
        		int pixel = image.getRGB(i, j);
      
        		if(pixel != 0xffffffff && pixel != 0xff000000) {
        			double chroma = getChroma(pixel);
        			if(chroma < 0.08) {
        				image.setRGB(i, j, 0xff000000);
        			} 
        		}
        	}
        }
        
        for(int i = 1; i < image.getWidth() - 1; i++) {
        	for(int j = 1; j < image.getHeight() - 1; j++) {
        		int pixel = image.getRGB(i, j);
        		
        		if(pixel != 0xffffffff) {
        			int count = 0;
        			for(int k = 0; k < 8; k++) {
        				int spixel = image.getRGB(i + OFFSET[k][0], j + OFFSET[k][1]);
        				if(spixel == 0xff000000) {
        					count++;
        				}
        			}
        			
        			if(pixel == 0xff000000) {
        				if(count == 0) {
        					image.setRGB(i, j, 0xffffffff);
        				}
        			} else {
						if(count > 1) {
							image.setRGB(i, j, 0xff000000);
						} else {
							image.setRGB(i, j, 0xffffffff);
						}
        			}
        		}
        	}
        }

        for(int i = 0; i < image.getWidth(); i++) {
        	for(int j = 0; j < image.getHeight(); j++) {
        		int pixel = image.getRGB(i, j);
      
        		if(pixel != 0xffffffff && pixel != 0xff000000) {
        			image.setRGB(i, j, 0xffffffff);
        		}
        	}
        }
        
        String result = null;
        try {
            result = instance.doOCR(image);
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
        
        //System.out.println(result);
        result = result.replaceAll("[ \n]", "");
        boolean match = result.matches("\\d\\d\\d[A-Z][A-Z][A-Z]");
        
        //ImageIcon icon = new ImageIcon(image);
		//JOptionPane.showMessageDialog(null, null, null, 0, icon);

        return (match) ? result : null;
	}
	
    public static double getChroma(int pixel) {
    	double red = ((pixel & 0x00ff0000) >> 16) / 255.0;
    	double green = ((pixel & 0x0000ff00) >> 8) / 255.0;
    	double blue = (pixel & 0x000000ff) / 255.0;
    	
    	double avg = (red + green + blue) / 3.0;
    	
    	red -= avg;
    	green -= avg;
    	blue -= avg;
    	
    	return Math.sqrt((red*red + green*green + blue*blue) / (avg*2.0));
    }
    
    public static double getLuma(int pixel) {
    	double red = ((pixel & 0x00ff0000) >> 16) / 255.0;
    	double green = ((pixel & 0x0000ff00) >> 8) / 255.0;
    	double blue = (pixel & 0x000000ff) / 255.0;
    	
    	return (red + green + blue) / 3.0;
    }
}
