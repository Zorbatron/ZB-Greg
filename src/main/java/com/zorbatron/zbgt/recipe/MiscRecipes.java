package com.zorbatron.zbgt.recipe;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;

import com.zorbatron.zbgt.ZBGTConfig;
import com.zorbatron.zbgt.recipe.helpers.RecipeAssists;

import gregtech.api.unification.ore.OrePrefix;

public class MiscRecipes {

    protected static void init() {
        magneticFluids();
        genericCircuits();

        if (ZBGTConfig.recipeSettings.enableSillyRecipes) {
            sillyRecipes();
        }
    }

    private static void magneticFluids() {
        POLARIZER_RECIPES.recipeBuilder()
                .fluidInputs(Iron.getFluid(144))
                .fluidOutputs(IronMagnetic.getFluid(144))
                .EUt(VHA[LV]).duration(144)
                .buildAndRegister();

        POLARIZER_RECIPES.recipeBuilder()
                .fluidInputs(Steel.getFluid(144))
                .fluidOutputs(SteelMagnetic.getFluid(144))
                .EUt(VHA[MV]).duration(144)
                .buildAndRegister();

        POLARIZER_RECIPES.recipeBuilder()
                .fluidInputs(Neodymium.getFluid(144))
                .fluidOutputs(NeodymiumMagnetic.getFluid(144))
                .EUt(VHA[HV]).duration(144)
                .buildAndRegister();

        POLARIZER_RECIPES.recipeBuilder()
                .fluidInputs(Samarium.getFluid(144))
                .fluidOutputs(SamariumMagnetic.getFluid(144))
                .EUt(VHA[EV]).duration(150)
                .buildAndRegister();
    }

    private static void genericCircuits() {
        for (int tier = ULV; tier <= MAX; tier++) {
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.circuit, RecipeAssists.getMarkerMaterialByTier(tier), 16)
                    .circuitMeta(29)
                    .output(RecipeAssists.getGenericCircuitByTier(tier), 16)
                    .EUt(8).duration(20)
                    .buildAndRegister();
        }
    }

    private static void sillyRecipes() {
        COMPRESSOR_RECIPES.recipeBuilder()
                .fluidInputs(Water.getFluid(Integer.MAX_VALUE))
                .fluidOutputs(Neutronium.getFluid(1))
                .circuitMeta(1)
                .EUt(Integer.MAX_VALUE).duration(Integer.MAX_VALUE)
                .buildAndRegister();
    }
}
