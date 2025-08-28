package net.pixeldreamstudios.iconleadingtooltip.mixin.trinkets;

import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.TrinketsAttributeModifiersComponent;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.pixeldreamstudios.iconleadingtooltip.util.IconLeadingUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
@Mixin(ItemStack.class)
public abstract class TrinketsTooltipRedirectMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    private void ilt$moveIconsForTrinkets(Item.TooltipContext context, @Nullable net.minecraft.entity.player.PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        if (!FabricLoader.getInstance().isModLoaded("trinkets")) return;
        if (!ilt$isTrinketStack()) return;

        List<Text> in = cir.getReturnValue();
        List<Text> out = new ArrayList<>(in.size());
        for (Text t : in) out.add(ilt$transformIfAttributeLine(t));
        cir.setReturnValue(out);
    }

    @Unique
    private boolean ilt$isTrinketStack() {
        ItemStack self = (ItemStack) (Object) this;
        boolean viaApi = TrinketsApi.getTrinket(self.getItem()) != null;
        TrinketsAttributeModifiersComponent comp = self.get(TrinketsAttributeModifiersComponent.TYPE);
        boolean viaComponent = comp != null && comp.showInTooltip();
        return viaApi || viaComponent;
    }

    @Unique
    private Text ilt$transformIfAttributeLine(Text text) {
        TextContent content = text.getContent();
        if (!(content instanceof TranslatableTextContent tr)) return text;

        String key = tr.getKey();
        if (!(key.startsWith("attribute.modifier.plus.")
                || key.startsWith("attribute.modifier.take.")
                || key.startsWith("attribute.modifier.equals."))) {
            return text;
        }

        Object[] args = tr.getArgs().clone();
        if (args.length < 2 || !(args[1] instanceof Text attrText)) return text;

        String attrRaw = attrText.getString();
        if (!attrRaw.isEmpty() && IconLeadingUtil.isIconGlyph(attrRaw.codePointAt(0))) return text;

        int[] span = ilt$findIconAnywhere(attrRaw);
        if (span == null) return text;

        String icon = attrRaw.substring(span[0], span[1]);
        String cleanedAttr = IconLeadingUtil.stripSectionCodes(
                attrRaw.substring(0, span[0]) + attrRaw.substring(span[1])
        ).trim();
        args[1] = Text.literal(cleanedAttr);

        MutableText rebuilt = Text.translatable(key, args).setStyle(text.getStyle());

        Text iconText = Text.literal(icon + " ").styled(s ->
                s.withColor(0xFFFFFF)
                        .withBold(false)
                        .withItalic(false)
                        .withUnderline(false)
                        .withStrikethrough(false)
                        .withObfuscated(false)
        );

        return Text.empty().append(iconText).append(rebuilt);
    }

    @Unique
    private static int[] ilt$findIconAnywhere(String s) {
        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            int len = Character.charCount(cp);
            if (IconLeadingUtil.isIconGlyph(cp)) return new int[]{i, i + len};
            i += len;
        }
        return null;
    }
}
