package net.pixeldreamstudios.iconleadingtooltip.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;
@Environment(EnvType.CLIENT)
public final class IconLeadingTooltipClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register(this::onItemTooltip);
    }

    private void onItemTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> texts) {
        if (FabricLoader.getInstance().isModLoaded("dynamictooltips")) return;
        if (!FabricLoader.getInstance().isModLoaded("bettercombat")) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        BcAttackRangeTooltip.append(itemStack, player, texts);
    }

}
