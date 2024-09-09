package com.zorbatron.zbgt.api.recipes.properties;

import java.util.Map;
import java.util.TreeMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import org.apache.commons.lang3.Validate;

import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.util.TextFormattingUtil;

public class CoALProperty extends RecipeProperty<Integer> {

    public static final String KEY = "coal_casing_tier";

    private static final TreeMap<Integer, String> registeredCasingTiers = new TreeMap<>();

    private static CoALProperty INSTANCE;

    protected CoALProperty() {
        super(KEY, Integer.class);
    }

    public static CoALProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CoALProperty();
        }

        return INSTANCE;
    }

    private static String getCasingTier(Integer tier) {
        Map.Entry<Integer, String> mapEntry = registeredCasingTiers.ceilingEntry(tier);

        if (mapEntry == null) {
            throw new IllegalArgumentException("oopz");
        }

        return String.format(" %s", mapEntry.getValue());
    }

    public static void registerCasingTier(int tier, String shortName) {
        Validate.notNull(shortName);
        registeredCasingTiers.put(tier, shortName);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("zbgt.recipe.coal_casing_tier",
                TextFormattingUtil.formatLongToCompactString(castValue(value))) + getCasingTier(castValue(value)), x, y,
                color);
    }
}
