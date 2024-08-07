package com.zorbatron.zbgt;

import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.zorbatron.zbgt.common.ZBGTRecipes;
import com.zorbatron.zbgt.common.covers.ZBGTCovers;
import com.zorbatron.zbgt.common.items.ZBGTMetaItems;

import gregtech.api.GregTechAPI;
import gregtech.api.cover.CoverDefinition;

@Mod.EventBusSubscriber(modid = ZBGTCore.MODID)
public class CommonProxy {

    public void preInit() {
        ZBGTMetaItems.init();
    }

    @SubscribeEvent
    public static void registerCovers(GregTechAPI.RegisterEvent<CoverDefinition> event) {
        ZBGTCovers.init();
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        ZBGTRecipes.init();
    }
}
