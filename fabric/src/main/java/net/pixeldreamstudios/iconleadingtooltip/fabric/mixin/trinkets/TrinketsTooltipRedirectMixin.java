package net.pixeldreamstudios.iconleadingtooltip.fabric.mixin.trinkets;

import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.TrinketsAttributeModifiersComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.pixeldreamstudios.iconleadingtooltip.util.IconLeadingUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ItemStack.class, priority = 1500)
public abstract class TrinketsTooltipRedirectMixin {

    @Inject(
            method = "getTooltipLines",
            at = @At("RETURN"),
            cancellable = true
    )
    private void ilt$moveIconsForTrinkets(
            Item.TooltipContext context,
            @Nullable Player player,
            TooltipFlag type,
            CallbackInfoReturnable<List<Component>> cir
    ) {
        if (!ilt$isTrinketStack()) {
            return;
        }

        List<Component> in = cir.getReturnValue();
        List<Component> out = new ArrayList<>(in.size());
        boolean anyTransformed = false;

        for (Component c :  in) {
            Component transformed = ilt$transformIfAttributeLine(c);
            out.add(transformed);
            if (transformed != c) {
                anyTransformed = true;
            }
        }

        if (anyTransformed) {
            cir.setReturnValue(out);
        }
    }

    @Unique
    private boolean ilt$isTrinketStack() {
        ItemStack self = (ItemStack) (Object) this;

        try {
            boolean viaApi = TrinketsApi.getTrinket(self.getItem()) != null;
            TrinketsAttributeModifiersComponent comp = self.get(TrinketsAttributeModifiersComponent.TYPE);
            boolean viaComponent = comp != null && comp.showInTooltip();
            return viaApi || viaComponent;
        } catch (Exception e) {
            return false;
        }
    }

    @Unique
    private Component ilt$transformIfAttributeLine(Component component) {
        if (!(component.getContents() instanceof TranslatableContents tr)) {
            return component;
        }

        String key = tr.getKey();
        if (!(key.startsWith("attribute.modifier.plus.")
                || key.startsWith("attribute.modifier.take.")
                || key.startsWith("attribute.modifier.equals."))) {
            return component;
        }

        Object[] args = tr.getArgs();
        if (args.length < 2 || !(args[1] instanceof Component attrText)) {
            return component;
        }

        String attrRaw = attrText.getString();
        if (!attrRaw.isEmpty() && IconLeadingUtil.isIconGlyph(attrRaw.codePointAt(0))) {
            return component;
        }

        int[] span = ilt$findIconAnywhere(attrRaw);
        if (span == null) {
            return component;
        }

        String icon = attrRaw.substring(span[0], span[1]);
        String cleanedAttr = IconLeadingUtil.stripSectionCodes(
                attrRaw.substring(0, span[0]) + attrRaw.substring(span[1])
        ).trim();

        Object[] newArgs = args.clone();
        newArgs[1] = Component.literal(cleanedAttr);

        MutableComponent rebuilt = Component.translatable(key, newArgs)
                .withStyle(component.getStyle());

        Component iconText = Component.literal(icon + " ").withStyle(s ->
                s.withColor(0xFFFFFF)
                        .withBold(false)
                        .withItalic(false)
                        .withUnderlined(false)
                        .withStrikethrough(false)
                        .withObfuscated(false)
        );

        return Component.empty().append(iconText).append(rebuilt);
    }

    @Unique
    private static int[] ilt$findIconAnywhere(String s) {
        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            int len = Character.charCount(cp);
            if (IconLeadingUtil.isIconGlyph(cp)) {
                return new int[]{i, i + len};
            }
            i += len;
        }
        return null;
    }
}