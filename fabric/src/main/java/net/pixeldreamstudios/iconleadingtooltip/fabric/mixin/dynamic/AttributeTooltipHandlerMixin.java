package net.pixeldreamstudios.iconleadingtooltip.fabric.mixin.dynamic;

import dev.muon.dynamictooltips.handlers.AttributeTooltipHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.pixeldreamstudios.iconleadingtooltip.util.IconLeadingUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(AttributeTooltipHandler.class)
public abstract class AttributeTooltipHandlerMixin {
    private static final ThreadLocal<String> ILT$ICON = new ThreadLocal<>();

    @ModifyArg(
            method = "processBaseModifiers",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"
            ),
            index = 0
    )
    private static String ilt$removeLeadingBaseSpace(String content) {
        return " ".equals(content) ? "" : content;
    }

    @ModifyArg(
            method = "createModifierComponent(Lnet/minecraft/world/entity/ai/attributes/Attribute;Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)Lnet/minecraft/network/chat/MutableComponent;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"
            ),
            index = 1
    )
    private static Object[] ilt$stripIconFromAttrArg_Mod(Object[] args) {
        if (args != null && args.length >= 2 && args[1] instanceof Component attrText) {
            String raw = attrText.getString();
            int[] span = IconLeadingUtil.firstIconSpan(raw);
            if (span[0] >= 0) {
                String icon = raw.substring(span[0], span[1]);
                ILT$ICON.set(icon);
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
            method = "createModifierComponent(Lnet/minecraft/world/entity/ai/attributes/Attribute;Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)Lnet/minecraft/network/chat/MutableComponent;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void ilt$prefixIcon_Mod(Attribute attribute, AttributeModifier modifier,
                                           CallbackInfoReturnable<MutableComponent> cir) {
        String icon = ILT$ICON.get();
        ILT$ICON.remove();
        if (icon != null && !icon.isEmpty()) {
            MutableComponent ret = cir.getReturnValue();
            boolean needsSpace = ret.getString().isEmpty() || ! ret.getString().startsWith(" ");
            MutableComponent container = Component.empty();
            MutableComponent iconPart = Component.literal(icon).withStyle(s -> s.withColor(0xFFFFFF));
            if (needsSpace) {
                container.append(iconPart).append(Component.literal(" ")).append(ret);
            } else {
                container.append(iconPart).append(ret);
            }
            cir.setReturnValue(container);
        }
    }

    @ModifyArg(
            method = "createBaseComponent(Lnet/minecraft/world/entity/ai/attributes/Attribute;DDZ)Lnet/minecraft/network/chat/MutableComponent;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"
            ),
            index = 1
    )
    private static Object[] ilt$stripIconFromAttrArg_Base(Object[] args) {
        if (args != null && args.length >= 2 && args[1] instanceof Component attrText) {
            String raw = attrText.getString();
            int[] span = IconLeadingUtil.firstIconSpan(raw);
            if (span[0] >= 0) {
                String icon = raw.substring(span[0], span[1]);
                ILT$ICON.set(icon);
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
            method = "createBaseComponent(Lnet/minecraft/world/entity/ai/attributes/Attribute;DDZ)Lnet/minecraft/network/chat/MutableComponent;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void ilt$prefixIcon_Base(Attribute attribute, double value, double entityBase, boolean merged,
                                            CallbackInfoReturnable<MutableComponent> cir) {
        String icon = ILT$ICON.get();
        ILT$ICON.remove();
        if (icon != null && !icon.isEmpty()) {
            MutableComponent ret = cir.getReturnValue();
            boolean needsSpace = ret.getString().isEmpty() || !ret.getString().startsWith(" ");
            MutableComponent container = Component.empty();
            MutableComponent iconPart = Component.literal(icon).withStyle(s -> s.withColor(0xFFFFFF));
            if (needsSpace) {
                container.append(iconPart).append(Component.literal(" ")).append(ret);
            } else {
                container.append(iconPart).append(ret);
            }
            cir.setReturnValue(container);
        }
    }
}