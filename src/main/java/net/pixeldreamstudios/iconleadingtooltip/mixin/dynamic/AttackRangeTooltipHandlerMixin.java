package net.pixeldreamstudios.iconleadingtooltip.mixin.dynamic;

import dev.muon.dynamictooltips.handlers.AttackRangeTooltipHandler;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
            method = "createTotalRangeComponent(D)Lnet/minecraft/text/MutableText;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"
            ),
            index = 1
    )
    private static Object[] ilt$stripIcon_Total(Object[] args) {
        if (args != null && args.length >= 2 && args[1] instanceof Text attrText) {
            String raw = attrText.getString();
            int[] span = IconLeadingUtil.firstIconSpan(raw);
            if (span[0] >= 0) {
                ILT$ICON.set(raw.substring(span[0], span[1]));
                String restRaw = raw.substring(0, span[0]) + raw.substring(span[1]);
                String rest = IconLeadingUtil.stripSectionCodes(restRaw).replaceFirst("^\\s+", "");
                args[1] = Text.literal(rest);
            } else {
                ILT$ICON.remove();
            }
        }
        return args;
    }

    @Inject(
            method = "createTotalRangeComponent(D)Lnet/minecraft/text/MutableText;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void ilt$prefixIcon_Total(double range, CallbackInfoReturnable<MutableText> cir) {
        String icon = ILT$ICON.get();
        ILT$ICON.remove();
        if (icon != null && !icon.isEmpty()) {
            MutableText ret = cir.getReturnValue();
            boolean needsSpace = ret.getString().isEmpty() || !ret.getString().startsWith(" ");

            MutableText container = Text.empty();
            MutableText iconPart = Text.literal(icon).styled(s -> s.withColor(0xFFFFFF));

            if (needsSpace) {
                container.append(iconPart).append(Text.literal(" ")).append(ret);
            } else {
                container.append(iconPart).append(ret);
            }
            cir.setReturnValue(container);
        }
    }


    @ModifyArg(
            method = "createBaseWeaponRangeComponent(DLnet/minecraft/util/Formatting;)Lnet/minecraft/text/MutableText;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"
            ),
            index = 1
    )
    private static Object[] ilt$stripIcon_BaseRange(Object[] args) {
        if (args != null && args.length >= 2 && args[1] instanceof Text attrText) {
            String raw = attrText.getString();
            int[] span = IconLeadingUtil.firstIconSpan(raw);
            if (span[0] >= 0) {
                ILT$ICON.set(raw.substring(span[0], span[1]));
                String restRaw = raw.substring(0, span[0]) + raw.substring(span[1]);
                String rest = IconLeadingUtil.stripSectionCodes(restRaw).replaceFirst("^\\s+", "");
                args[1] = Text.literal(rest);
            } else {
                ILT$ICON.remove();
            }
        }
        return args;
    }

    @Inject(
            method = "createBaseWeaponRangeComponent(DLnet/minecraft/util/Formatting;)Lnet/minecraft/text/MutableText;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void ilt$prefixIcon_BaseRange(double value, Formatting color, CallbackInfoReturnable<MutableText> cir) {
        String icon = ILT$ICON.get();
        ILT$ICON.remove();
        if (icon != null && !icon.isEmpty()) {
            MutableText ret = cir.getReturnValue();
            boolean needsSpace = ret.getString().isEmpty() || !ret.getString().startsWith(" ");

            MutableText container = Text.empty();
            MutableText iconPart = Text.literal(icon).styled(s -> s.withColor(0xFFFFFF));

            if (needsSpace) {
                container.append(iconPart).append(Text.literal(" ")).append(ret);
            } else {
                container.append(iconPart).append(ret);
            }
            cir.setReturnValue(container);
        }
    }


    @ModifyArg(
            method = "createModifierComponent(Lnet/minecraft/entity/attribute/EntityAttributeModifier;)Lnet/minecraft/text/MutableText;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"
            ),
            index = 1
    )
    private static Object[] ilt$stripIcon_ModRange(Object[] args) {
        if (args != null && args.length >= 2 && args[1] instanceof Text attrText) {
            String raw = attrText.getString();
            int[] span = IconLeadingUtil.firstIconSpan(raw);
            if (span[0] >= 0) {
                ILT$ICON.set(raw.substring(span[0], span[1]));
                String restRaw = raw.substring(0, span[0]) + raw.substring(span[1]);
                String rest = IconLeadingUtil.stripSectionCodes(restRaw).replaceFirst("^\\s+", "");
                args[1] = Text.literal(rest);
            } else {
                ILT$ICON.remove();
            }
        }
        return args;
    }

    @Inject(
            method = "createModifierComponent(Lnet/minecraft/entity/attribute/EntityAttributeModifier;)Lnet/minecraft/text/MutableText;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void ilt$prefixIcon_ModRange(EntityAttributeModifier modifier, CallbackInfoReturnable<MutableText> cir) {
        String icon = ILT$ICON.get();
        ILT$ICON.remove();
        if (icon != null && !icon.isEmpty()) {
            MutableText ret = cir.getReturnValue();
            boolean needsSpace = ret.getString().isEmpty() || !ret.getString().startsWith(" ");

            MutableText container = Text.empty();
            MutableText iconPart = Text.literal(icon).styled(s -> s.withColor(0xFFFFFF));

            if (needsSpace) {
                container.append(iconPart).append(Text.literal(" ")).append(ret);
            } else {
                container.append(iconPart).append(ret);
            }
            cir.setReturnValue(container);
        }
    }
}
