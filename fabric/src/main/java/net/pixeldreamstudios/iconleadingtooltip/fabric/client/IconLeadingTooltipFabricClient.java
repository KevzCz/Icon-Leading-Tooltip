package net.pixeldreamstudios.iconleadingtooltip.fabric.client;

import dev.architectury.platform.Platform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.pixeldreamstudios.iconleadingtooltip.client.BcAttackRangeTooltip;

import java.util.List;

public final class IconLeadingTooltipFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register(this::onItemTooltip);
    }

    private void onItemTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipFlag tooltipType, List<Component> texts) {
        if (Platform.isModLoaded("dynamictooltips")) return;
        if (! Platform.isModLoaded("bettercombat")) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        BcAttackRangeTooltip.append(itemStack, player, texts);
    }
}