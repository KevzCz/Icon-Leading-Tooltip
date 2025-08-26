// net/pixeldreamstudios/iconleadingtooltip/mixin/dynamic/AttributeTooltipHandlerMixin.java
package net.pixeldreamstudios.iconleadingtooltip.mixin.dynamic;

import net.minecraft.entity.attribute.EntityAttribute;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.muon.dynamictooltips.handlers.AttributeTooltipHandler")
public abstract class AttributeTooltipHandlerMixin {
    private static final ThreadLocal<String> ILT$ICON = new ThreadLocal<>();

    @Redirect(
            method = "processBaseModifiers",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;literal(Ljava/lang/String;)Lnet/minecraft/text/MutableText;"
            )
    )
    private static MutableText ilt$removeLeadingBaseSpace(String content) {
        if (" ".equals(content)) {
            return Text.empty();
        }
        return Text.literal(content);
    }

    @ModifyArg(
            method = "createModifierComponent(Lnet/minecraft/entity/attribute/EntityAttribute;Lnet/minecraft/entity/attribute/EntityAttributeModifier;)Lnet/minecraft/text/MutableText;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"
            ),
            index = 1 // Object[] varargs
    )
    private static Object[] ilt$stripIconFromAttrArg_Mod(Object[] args) {
        if (args != null && args.length >= 2 && args[1] instanceof Text attrText) {
            String raw = attrText.getString();
            int[] span = IconLeadingUtil.firstIconSpan(raw);
            if (span[0] >= 0) {
                String icon = raw.substring(span[0], span[1]);
                ILT$ICON.set(icon);

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
            method = "createModifierComponent(Lnet/minecraft/entity/attribute/EntityAttribute;Lnet/minecraft/entity/attribute/EntityAttributeModifier;)Lnet/minecraft/text/MutableText;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void ilt$prefixIcon_Mod(EntityAttribute attribute, EntityAttributeModifier modifier,
                                           CallbackInfoReturnable<MutableText> cir) {
        String icon = ILT$ICON.get();
        ILT$ICON.remove();
        if (icon != null && !icon.isEmpty()) {
            MutableText ret = cir.getReturnValue();
            boolean needsSpace = ret.getString().isEmpty() || !ret.getString().startsWith(" ");

            MutableText container = Text.empty(); // neutral root so later styling won't recolor children
            MutableText iconPart = Text.literal(icon)
                    .styled(s -> s.withColor(0xFFFFFF));   // force white; won’t inherit parent color

            if (needsSpace) {
                container.append(iconPart).append(Text.literal(" ")).append(ret);
            } else {
                container.append(iconPart).append(ret);
            }
            cir.setReturnValue(container);
        }
    }


    @ModifyArg(
            method = "createBaseComponent(Lnet/minecraft/entity/attribute/EntityAttribute;DDB)Lnet/minecraft/text/MutableText;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"
            ),
            index = 1
    )
    private static Object[] ilt$stripIconFromAttrArg_Base(Object[] args) {
        if (args != null && args.length >= 2 && args[1] instanceof Text attrText) {
            String raw = attrText.getString();
            int[] span = IconLeadingUtil.firstIconSpan(raw);
            if (span[0] >= 0) {
                String icon = raw.substring(span[0], span[1]);
                ILT$ICON.set(icon);

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
            method = "createBaseComponent(Lnet/minecraft/entity/attribute/EntityAttribute;DDB)Lnet/minecraft/text/MutableText;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void ilt$prefixIcon_Base(EntityAttribute attribute, double value, double entityBase, boolean merged,
                                            CallbackInfoReturnable<MutableText> cir) {
        String icon = ILT$ICON.get();
        ILT$ICON.remove();
        if (icon != null && !icon.isEmpty()) {
            MutableText ret = cir.getReturnValue();
            boolean needsSpace = ret.getString().isEmpty() || !ret.getString().startsWith(" ");

            MutableText container = Text.empty(); // neutral root so later styling won't recolor children
            MutableText iconPart = Text.literal(icon)
                    .styled(s -> s.withColor(0xFFFFFF));   // force white; won’t inherit parent color

            if (needsSpace) {
                container.append(iconPart).append(Text.literal(" ")).append(ret);
            } else {
                container.append(iconPart).append(ret);
            }
            cir.setReturnValue(container);
        }
    }
}
