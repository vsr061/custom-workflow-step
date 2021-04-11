package com.hashout.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public final class PDFHelper {

    private static final Map<String, PDFont> FONT_MAP = new HashMap<String, PDFont>() {{
        put("Times Roman", PDType1Font.TIMES_ROMAN);
        put("Times Roman Bold", PDType1Font.TIMES_BOLD);
        put("Times Roman Italic", PDType1Font.TIMES_ITALIC);
        put("Times Roman Bold Italic", PDType1Font.TIMES_BOLD_ITALIC);
        put("Helvetica", PDType1Font.HELVETICA);
        put("Helvetica Bold", PDType1Font.HELVETICA_BOLD);
        put("Helvetica Oblique", PDType1Font.HELVETICA_OBLIQUE);
        put("Helvetica Bold Oblique", PDType1Font.HELVETICA_BOLD_OBLIQUE);
        put("Courier", PDType1Font.COURIER);
        put("Courier Bold", PDType1Font.COURIER_BOLD);
        put("Courier Oblique", PDType1Font.COURIER_OBLIQUE);
        put("Courier Bold Oblique", PDType1Font.COURIER_BOLD_OBLIQUE);
    }};

    public static PDFont getFontFromName(String name) {
        return FONT_MAP.getOrDefault(name, PDType1Font.TIMES_ROMAN);
    }

    public static float getFontSize(String fontSize) {
        return StringUtils.isNumeric(fontSize) ? Float.parseFloat(fontSize) : 20;
    }

    public static Color getColor(String color) {
        return StringUtils.startsWith(color, "#") && StringUtils.length(color) == 7
                ? Color.decode(color)
                : Color.BLACK;
    }

    public static float getOpacity(String opacity) {
        return StringUtils.isNumeric(opacity) ? Float.parseFloat(opacity) / 100 : 0.2f;
    }

    public static float getXOffsetFromPosition(String position, float textWidth, PDDocument doc) {
        if (position != null) {
            switch (position) {
                case "TOP_LEFT":
                case "BOTTOM_LEFT":
                    return 0;
                case "TOP_RIGHT":
                case "BOTTOM_RIGHT":
                    return doc.getPage(0).getMediaBox().getWidth() - textWidth;
                case "CENTER":
                default:
                    return (doc.getPage(0).getMediaBox().getWidth() - textWidth) / 2;
            }
        }
        return (doc.getPage(0).getMediaBox().getWidth() - textWidth) / 2;
    }

    public static float getYOffsetFromPosition(String position, float textHeight, PDDocument doc) {
        if (position != null) {
            switch (position) {
                case "TOP_LEFT":
                case "TOP_RIGHT":
                    return doc.getPage(0).getMediaBox().getHeight() - textHeight;
                case "BOTTOM_LEFT":
                case "BOTTOM_RIGHT":
                    return 0;
                case "CENTER":
                default:
                    return (doc.getPage(0).getMediaBox().getHeight() - textHeight) / 2;
            }
        }
        return (doc.getPage(0).getMediaBox().getHeight() - textHeight) / 2;
    }

    public static double getOrientation(String angle) {
        return StringUtils.isNumeric(angle) ? Math.toRadians(Double.parseDouble(angle)) : Math.toRadians(45);
    }
}
