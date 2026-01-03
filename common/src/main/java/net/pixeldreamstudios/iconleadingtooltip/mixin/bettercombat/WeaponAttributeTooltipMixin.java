package net.pixeldreamstudios.iconleadingtooltip.mixin.bettercombat;

import dev.architectury.platform.Platform;
import net.bettercombat.client.WeaponAttributeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.iconleadingtooltip.util.IconLeadingUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(value = WeaponAttributeTooltip.class, remap = false)
public class WeaponAttributeTooltipMixin {

    @Inject(
            method = "modifyTooltip",
            at = @At("RETURN"),
            remap = false,
            require = 0
    )
    private static void transformBetterCombatTooltip(ItemStack itemStack, List lines, CallbackInfo ci) {
        if (Platform.isModLoaded("dynamictooltips")) {
            return;
        }
        if (!Platform.isModLoaded("bettercombat")) {
            return;
        }

        for (int i = 0; i < lines.size(); i++) {
            Object lineObj = lines.get(i);
            if (!(lineObj instanceof Component line)) {
                continue;
            }
            Component transformed = transformAttackRangeLine(line);
            if (transformed != line) {
                lines.set(i, transformed);
            }
        }
    }

    private static Component transformAttackRangeLine(Component component) {
        if (component.getSiblings().isEmpty()) {
            return component;
        }

        for (Component sibling : component.getSiblings()) {


            var content = sibling.getContents();
            if (!(content instanceof TranslatableContents tr)) {
                continue;
            }

            String key = tr.getKey();
            if (!key.startsWith("attribute.modifier.equals.")) {
                continue;
            }

            Object[] args = tr.getArgs();
            if (args.length < 2 || !(args[1] instanceof Component attrText)) {
                continue;
            }

            var attrContent = attrText.getContents();
            if (!(attrContent instanceof TranslatableContents attrTr)) {
                continue;
            }

            String attrKey = attrTr.getKey();
            if (!attrKey.equals("attribute.name.generic.attack_range")) {
                continue;
            }

            String attrRaw = attrText.getString();
            int[] span = IconLeadingUtil.firstIconSpan(attrRaw);

            if (span[0] < 0) {
                continue;
            }

            String icon = attrRaw.substring(span[0], span[1]);
            String cleanedAttr = IconLeadingUtil.stripSectionCodes(
                    attrRaw.substring(0, span[0]) + attrRaw.substring(span[1])
            ).trim();

            Object[] newArgs = args.clone();
            newArgs[1] = Component.literal(cleanedAttr);

            MutableComponent rebuilt = Component.translatable(key, newArgs);

            Component iconText = Component.literal(icon + " ");

            MutableComponent result = iconText.copy().append(rebuilt).withStyle(ChatFormatting.DARK_GREEN);

            return result;
        }

        return component;
    }
}