package net.pixeldreamstudios.iconleadingtooltip.mixin.dynamic;

import dev.muon.dynamictooltips.handlers.BlockRangeTooltipHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.pixeldreamstudios.iconleadingtooltip.util.IconLeadingUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(BlockRangeTooltipHandler.class)
public abstract class BlockRangeTooltipHandlerMixin {
    private static final ThreadLocal<String> ILT$ICON = new ThreadLocal<>();

    @ModifyArg(
            method = "createRangeLine(DZ)Lnet/minecraft/text/MutableText;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;literal(Ljava/lang/String;)Lnet/minecraft/text/MutableText;"
            ),
            index = 0
    )
    private static String ilt$removeLeadingSpace(String content) {
        return " ".equals(content) ? "" : content;
    }

    @ModifyArg(
            method = "createRangeLine(DZ)Lnet/minecraft/text/MutableText;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"
            ),
            index = 1
    )
    private static Object[] ilt$stripIconFromAttr(Object[] args) {
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
            method = "createRangeLine(DZ)Lnet/minecraft/text/MutableText;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void ilt$prefixIcon(double value, boolean isModified, CallbackInfoReturnable<MutableText> cir) {
        String icon = ILT$ICON.get();
        ILT$ICON.remove();
        if (icon == null || icon.isEmpty()) return;

        MutableText ret = cir.getReturnValue();
        MutableText iconPart = Text.literal(icon).styled(s -> s.withColor(0xFFFFFF));
        MutableText out = Text.empty().append(iconPart).append(Text.literal(" ")).append(ret);
        cir.setReturnValue(out);
    }
}
