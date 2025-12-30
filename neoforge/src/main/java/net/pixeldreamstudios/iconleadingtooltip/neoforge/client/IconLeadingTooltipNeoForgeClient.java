package net.pixeldreamstudios.iconleadingtooltip.neoforge.client;

import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.pixeldreamstudios.iconleadingtooltip.IconLeadingTooltip;
import net.pixeldreamstudios.iconleadingtooltip.client.BcAttackRangeTooltip;
import net.pixeldreamstudios.iconleadingtooltip.util.IconLeadingUtil;

import java.util.List;

@EventBusSubscriber(modid = IconLeadingTooltip.MOD_ID_NEOFORGE, value = Dist.CLIENT)
public final class IconLeadingTooltipNeoForgeClient {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        List<Component> tooltip = event.getToolTip();
        ItemStack stack = event.getItemStack();

        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            Component transformed = transformLine(line);
            if (transformed != line) {
                tooltip.set(i, transformed);
            }
        }

        if (Platform.isModLoaded("bettercombat")) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                BcAttackRangeTooltip.append(stack, player, tooltip);
            }
        }
    }

    private static Component transformLine(Component original) {
        String text = original.getString();
        int[] span = IconLeadingUtil.firstIconSpan(text);

        if (span[0] < 0) {
            return original;
        }

        if (! isAttributeLine(original)) {
            return original;
        }

        if (span[0] == 0 || (span[0] <= 2 && text.substring(0, span[0]).trim().isEmpty())) {
            return original;
        }

        String icon = text.substring(span[0], span[1]);
        String withoutIcon = text.substring(0, span[0]) + text.substring(span[1]);
        withoutIcon = IconLeadingUtil.stripSectionCodes(withoutIcon);
        withoutIcon = withoutIcon.replaceAll("\\s+", " ").trim();

        Component iconText = Component.literal(icon + " ");
        MutableComponent textPart = Component.literal(withoutIcon).setStyle(original.getStyle());

        return Component.empty().append(iconText).append(textPart);
    }

    private static boolean isAttributeLine(Component component) {
        var content = component.getContents();
        if (content instanceof net.minecraft.network.chat.contents.TranslatableContents tr) {
            String key = tr.getKey();
            if (key.startsWith("attribute.modifier.plus.")
                    || key.startsWith("attribute.modifier.take.")
                    || key.startsWith("attribute.modifier.equals.")
                    || key.equals("neoforge.modifier.plus")
                    || key.equals("neoforge.modifier.take")) {
                return true;
            }
        }

        for (Component sibling :  component.getSiblings()) {
            var siblingContent = sibling.getContents();
            if (siblingContent instanceof net.minecraft.network.chat.contents.TranslatableContents tr) {
                String key = tr.getKey();
                if (key.startsWith("attribute.modifier.plus.")
                        || key.startsWith("attribute.modifier.take.")
                        || key.startsWith("attribute.modifier.equals.")
                        || key.equals("neoforge.modifier.plus")
                        || key.equals("neoforge.modifier.take")) {
                    return true;
                }
            }
        }

        return false;
    }
}