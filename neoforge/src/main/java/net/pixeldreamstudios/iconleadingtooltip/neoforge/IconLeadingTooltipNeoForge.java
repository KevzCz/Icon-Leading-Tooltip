package net.pixeldreamstudios.iconleadingtooltip.neoforge;

import net.neoforged. bus.api.IEventBus;
import net. neoforged.fml. common.Mod;
import net.pixeldreamstudios.iconleadingtooltip.IconLeadingTooltip;

@Mod(IconLeadingTooltip.MOD_ID_NEOFORGE)
public final class IconLeadingTooltipNeoForge {
    public IconLeadingTooltipNeoForge(IEventBus modEventBus) {
        IconLeadingTooltip.init();
    }
}