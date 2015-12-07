package com.techjar.ledcm;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.Value;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.font.effects.Effect;

/**
 *
 * @author Techjar
 */
public class FontManager {
    protected final File fontPath;
    protected BiMap<FontInfo, UnicodeFont> fonts;
    
    public FontManager() {
        fontPath = new File("resources/fonts/");
        fonts = HashBiMap.create();
    }
    
    public FontInstance getFont(String font, int size, boolean bold, boolean italic, List<Effect> effects) {
        FontInfo info = new FontInfo(font, size, bold, italic, effects);
        return new FontInstance(getFont(info), info);
    }
    
    public FontInstance getFont(String font, int size, boolean bold, boolean italic) {
        return getFont(font, size, bold, italic, null);
    }

    @SneakyThrows(SlickException.class)
    public UnicodeFont getFont(FontInfo info) {
        if (fonts.containsKey(info)) return fonts.get(info);
        UnicodeFont unicodeFont = new UnicodeFont(new File(fontPath, info.getFont() + ".ttf").getPath(), info.getSize(), info.isBold(), info.isItalic());
        unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
        if (info.getEffects() != null && !info.getEffects().isEmpty())
            unicodeFont.getEffects().addAll(info.getEffects());
        unicodeFont.addAsciiGlyphs();
        unicodeFont.loadGlyphs();
        fonts.put(info, unicodeFont);
        return unicodeFont;
    }

    public FontInfo getFontInfo(UnicodeFont font) {
        return fonts.inverse().get(font);
    }
    
    public void unloadFont(FontInfo info) {
        if (fonts.containsKey(info)) fonts.remove(info).destroy();
    }
    
    public void cleanup() {
        for (UnicodeFont font : fonts.values())
            font.destroy();
        fonts.clear();
    }

    @Value public static class FontInfo {
        private String font;
        private int size;
        private boolean bold;
        private boolean italic;
        private List<Effect> effects;
    }

    @Value public static class FontInstance {
        private UnicodeFont unicodeFont;
        private FontInfo info;
    }
}
