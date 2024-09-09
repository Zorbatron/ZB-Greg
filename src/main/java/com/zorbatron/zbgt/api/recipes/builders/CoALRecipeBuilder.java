package com.zorbatron.zbgt.api.recipes.builders;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import com.zorbatron.zbgt.api.recipes.properties.CoALProperty;
import com.zorbatron.zbgt.api.util.ZBGTLog;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.ComputationProperty;
import gregtech.api.recipes.recipeproperties.TotalComputationProperty;
import gregtech.api.util.EnumValidationResult;

public class CoALRecipeBuilder extends RecipeBuilder<CoALRecipeBuilder> {

    public CoALRecipeBuilder() {}

    public CoALRecipeBuilder(Recipe recipe, RecipeMap<CoALRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public CoALRecipeBuilder(RecipeBuilder<CoALRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public CoALRecipeBuilder copy() {
        return new CoALRecipeBuilder(this);
    }

    public boolean applyProperty(@NotNull String key, Object value) {
        switch (key) {
            case CoALProperty.KEY -> {
                this.CasingTier(((Number) value).intValue());
                return true;
            }
            case ComputationProperty.KEY -> {
                this.CWUt(((Number) value).intValue());
                return true;
            }
            case TotalComputationProperty.KEY -> {
                this.totalCWU(((Number) value).intValue());
                return true;
            }
        }

        return super.applyProperty(key, value);
    }

    public CoALRecipeBuilder CasingTier(int tier) {
        if (tier < 0) {
            ZBGTLog.logger.error("CoAL casing tier cannot be less than 0 (ULV)!", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        } else if (tier > 14) {
            ZBGTLog.logger.error("CoAL casing tier cannot be more than 14 (MAX)!", new IllegalArgumentException());
            this.recipeStatus = EnumValidationResult.INVALID;
        }

        this.applyProperty(CoALProperty.getInstance(), tier);

        return this;
    }

    public int getCasingTier() {
        return this.recipePropertyStorage == null ? 0 :
                this.recipePropertyStorage.getRecipePropertyValue(CoALProperty.getInstance(), 0);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(CoALProperty.getInstance().getKey(), getCasingTier())
                .toString();
    }

    public CoALRecipeBuilder CWUt(int cwut) {
        if (cwut < 0) {
            ZBGTLog.logger.error("CWU/t cannot be less than 0", new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.applyProperty(ComputationProperty.getInstance(), cwut);
        return this;
    }

    /**
     * The total computation for this recipe. If desired, this should be used instead of a call to duration().
     */
    public CoALRecipeBuilder totalCWU(int totalCWU) {
        if (totalCWU < 0) {
            ZBGTLog.logger.error("Total CWU cannot be less than 0", new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.applyProperty(TotalComputationProperty.getInstance(), totalCWU);
        return duration(totalCWU);
    }
}
