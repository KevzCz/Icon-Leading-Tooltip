package net.pixeldreamstudios.iconleadingtooltip.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.pixeldreamstudios.iconleadingtooltip.IconLeadingTooltip;

@Mod(IconLeadingTooltip.MOD_ID_FORGE)
public final class IconLeadingTooltipForge {

    public IconLeadingTooltipForge() {

        EventBuses.registerModEventBus(IconLeadingTooltip.MOD_ID_FORGE,
                FMLJavaModLoadingContext.get().getModEventBus());

        IconLeadingTooltip.init();
    }
}
