package net.pixeldreamstudios.iconleadingtooltip.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.pixeldreamstudios.iconleadingtooltip.util.IconLeadingUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ItemStack.class, priority = 1500)
public abstract class TooltipIconRelocatorMixin {

    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void iconleadingtooltip$moveIconsToFront(@Nullable Player player, TooltipFlag flag,
                                                     CallbackInfoReturnable<List<Component>> cir) {
        List<Component> tooltip = cir.getReturnValue();
        if (tooltip == null || tooltip.isEmpty()) return;

        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            Component transformed = iconleadingtooltip$transformLine(line);
            if (transformed != line) {
                tooltip.set(i, transformed);
            }
        }
    }

    @Unique
    private static Component iconleadingtooltip$transformLine(Component original) {
        String text = original.getString();
        int[] span = IconLeadingUtil.firstIconSpan(text);

        if (span[0] < 0) {
            return original;
        }

        if (!iconleadingtooltip$isAttributeLine(original)) {
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

    @Unique
    private static boolean iconleadingtooltip$isAttributeLine(Component component) {
        if (iconleadingtooltip$isAttributeKey(component.getContents())) {
            return true;
        }
        for (Component sibling : component.getSiblings()) {
            if (iconleadingtooltip$isAttributeLine(sibling)) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean iconleadingtooltip$isAttributeKey(ComponentContents contents) {
        if (contents instanceof TranslatableContents tr) {
            String key = tr.getKey();
            if (key.startsWith("attribute.modifier.plus.")
                    || key.startsWith("attribute.modifier.take.")
                    || key.startsWith("attribute.modifier.equals.")) {
                return true;
            }
            for (Object arg : tr.getArgs()) {
                if (arg instanceof Component argComponent && iconleadingtooltip$isAttributeLine(argComponent)) {
                    return true;
                }
            }
        }
        return false;
    }
}
