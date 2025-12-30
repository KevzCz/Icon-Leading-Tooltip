package net.pixeldreamstudios.iconleadingtooltip.fabric;

import net.fabricmc.api.ModInitializer;

import net.pixeldreamstudios.iconleadingtooltip.IconLeadingTooltip;

public final class IconLeadingTooltipFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        IconLeadingTooltip.init();
    }
}
