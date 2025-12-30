package net.pixeldreamstudios.iconleadingtooltip.fabric.mixin.dynamic;

import dev.muon.dynamictooltips.handlers.AttackRangeTooltipHandler;
import dev.muon.dynamictooltips.handlers.AttributeTooltipHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.pixeldreamstudios.iconleadingtooltip.util.IconLeadingUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(AttackRangeTooltipHandler.class)
public abstract class AttackRangeTooltipHandlerMixin {
    private static final ThreadLocal<String> ILT$ICON = new ThreadLocal<>();

    @ModifyArg(
            method = "createTotalRangeComponent(D)Lnet/minecraft/network/chat/MutableComponent;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
                    ordinal = 0
            ),
            index = 0
    )
    private static String ilt$removeLeadingSpace_Total(String content) {
        return " ".equals(content) ? "" : content;
    }

    @ModifyArg(
            method = "createTotalRangeComponent(D)Lnet/minecraft/network/chat/MutableComponent;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"
            ),
            index = 1
    )
    private static Object[] ilt$stripIcon_Total(Object[] args) {
        if (args != null && args.length >= 2 && args[1] instanceof Component attrText) {
            String raw = attrText.getString();
            int[] span = IconLeadingUtil.firstIconSpan(raw);
            if (span[0] >= 0) {
                ILT$ICON.set(raw.substring(span[0], span[1]));
                String restRaw = raw.substring(0, span[0]) + raw.substring(span[1]);
                String rest = IconLeadingUtil.stripSectionCodes(restRaw).replaceFirst("^\\s+", "");
                args[1] = Component.literal(rest);
            } else {
                ILT$ICON.remove();
            }
        }
        return args;
    }

    @Inject(
            method = "createTotalRangeComponent(D)Lnet/minecraft/network/chat/MutableComponent;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void ilt$prefixIcon_Total(double range, CallbackInfoReturnable<MutableComponent> cir) {
        String icon = ILT$ICON.get();
        ILT$ICON.remove();
        if (icon != null && ! icon.isEmpty()) {
            MutableComponent ret = cir.getReturnValue();
            MutableComponent iconPart = Component.literal(icon + " ");
            MutableComponent out = Component.empty().append(iconPart).append(ret);
            cir.setReturnValue(out);
        }
    }

    @ModifyArg(
            method = "createBaseWeaponRangeComponent(DLnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"
            ),
            index = 1
    )
    private static Object[] ilt$stripIcon_BaseRange(Object[] args) {
        if (args != null && args.length >= 2 && args[1] instanceof Component attrText) {
            String raw = attrText.getString();
            int[] span = IconLeadingUtil.firstIconSpan(raw);
            if (span[0] >= 0) {
                ILT$ICON.set(raw.substring(span[0], span[1]));
                String restRaw = raw.substring(0, span[0]) + raw.substring(span[1]);
                String rest = IconLeadingUtil.stripSectionCodes(restRaw).replaceFirst("^\\s+", "");
                args[1] = Component.literal(rest);
            } else {
                ILT$ICON.remove();
            }
        }
        return args;
    }

    @Inject(
            method = "createBaseWeaponRangeComponent(DLnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void ilt$prefixIcon_BaseRange(double value, net.minecraft.ChatFormatting color, CallbackInfoReturnable<MutableComponent> cir) {
        String icon = ILT$ICON.get();
        ILT$ICON.remove();
        if (icon != null && !icon.isEmpty()) {
            MutableComponent ret = cir.getReturnValue();
            MutableComponent listHeader = AttributeTooltipHandler.listHeader();
            MutableComponent iconPart = Component.literal(icon + " ");

            MutableComponent out = Component.empty()
                    .append(listHeader)
                    .append(iconPart)
                    .append(ret.getSiblings().get(0));

            cir.setReturnValue(out);
        }
    }

    @ModifyArg(
            method = "createModifierComponent(Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)Lnet/minecraft/network/chat/MutableComponent;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"
            ),
            index = 1
    )
    private static Object[] ilt$stripIcon_ModRange(Object[] args) {
        if (args != null && args.length >= 2 && args[1] instanceof Component attrText) {
            String raw = attrText.getString();
            int[] span = IconLeadingUtil.firstIconSpan(raw);
            if (span[0] >= 0) {
                ILT$ICON.set(raw.substring(span[0], span[1]));
                String restRaw = raw.substring(0, span[0]) + raw.substring(span[1]);
                String rest = IconLeadingUtil.stripSectionCodes(restRaw).replaceFirst("^\\s+", "");
                args[1] = Component.literal(rest);
            } else {
                ILT$ICON.remove();
            }
        }
        return args;
    }

    @Inject(
            method = "createModifierComponent(Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)Lnet/minecraft/network/chat/MutableComponent;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void ilt$prefixIcon_ModRange(AttributeModifier modifier, CallbackInfoReturnable<MutableComponent> cir) {
        String icon = ILT$ICON.get();
        ILT$ICON.remove();
        if (icon != null && !icon.isEmpty()) {
            MutableComponent ret = cir.getReturnValue();
            MutableComponent listHeader = AttributeTooltipHandler.listHeader();
            MutableComponent iconPart = Component.literal(icon + " ");

            MutableComponent out = Component.empty()
                    .append(listHeader)
                    .append(iconPart)
                    .append(ret.getSiblings().get(0));

            cir.setReturnValue(out);
        }
    }
}