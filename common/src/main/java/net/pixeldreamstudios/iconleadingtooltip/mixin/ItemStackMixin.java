package net.pixeldreamstudios.iconleadingtooltip.mixin;

import dev.architectury.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.pixeldreamstudios.iconleadingtooltip.util.IconLeadingUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = ItemStack.class, priority = 500)
public abstract class ItemStackMixin {

    @Inject(
            method = "addModifierTooltip",
            at = @At("HEAD"),
            cancellable = true
    )
    private void iconleadingtooltip$iconBeforeNumber(
            Consumer<Component> consumer,
            @Nullable Player player,
            Holder<Attribute> attribute,
            AttributeModifier modifier,
            CallbackInfo ci) {
        if (Platform.isModLoaded("dynamictooltips")) {
            return;
        }
        double d = modifier.amount();
        boolean usesBase = false;
        if (player != null) {
            if (modifier.is(Item.BASE_ATTACK_DAMAGE_ID)) {
                d += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                usesBase = true;
            } else if (modifier.is(Item.BASE_ATTACK_SPEED_ID)) {
                d += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                usesBase = true;
            }
        }

        double shown = switch (modifier.operation()) {
            case ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL -> d * 100.0;
            default -> attribute.is(Attributes.KNOCKBACK_RESISTANCE) ? d * 10.0 : d;
        };

        MutableComponent attrName = Component.translatable(attribute.value().getDescriptionId());
        String raw = attrName.getString();

        int[] span = IconLeadingUtil.firstIconSpan(raw);

        if (span[0] < 0) {
            return;
        }

        String icon = raw.substring(span[0], span[1]);
        String restRaw = raw.substring(0, span[0]) + raw.substring(span[1]);
        String rest = IconLeadingUtil.stripSectionCodes(restRaw).replaceFirst("^\\s+", "");

        MutableComponent attrNoIcon = Component.literal(rest);

        String numPlus = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(shown);
        String numTake = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-shown);

        Component iconText = Component.literal(icon + " ");

        if (usesBase) {
            MutableComponent body = Component.translatable(
                    "attribute.modifier.equals." + modifier.operation().id(),
                    numPlus, attrNoIcon
            ).withStyle(ChatFormatting.DARK_GREEN);
            consumer.accept(Component.empty().append(iconText).append(body));
            ci.cancel();
        } else if (d > 0.0) {
            MutableComponent body = Component.translatable(
                    "attribute.modifier.plus." + modifier.operation().id(),
                    numPlus, attrNoIcon
            ).withStyle(attribute.value().getStyle(true));
            consumer.accept(Component.empty().append(iconText).append(body));
            ci.cancel();
        } else if (d < 0.0) {
            MutableComponent body = Component.translatable(
                    "attribute.modifier.take." + modifier.operation().id(),
                    numTake, attrNoIcon
            ).withStyle(attribute.value().getStyle(false));
            consumer.accept(Component.empty().append(iconText).append(body));
            ci.cancel();
        } else {
            if (player != null) {
                double baseValue = player.getAttributeBaseValue(attribute);
                if (baseValue != 0.0) {
                    double baseShown = switch (modifier.operation()) {
                        case ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL -> baseValue * 100.0;
                        default -> attribute.is(Attributes.KNOCKBACK_RESISTANCE) ? baseValue * 10.0 : baseValue;
                    };
                    String numBase = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(baseShown);
                    MutableComponent body = Component.translatable(
                            "attribute.modifier.equals." + modifier.operation().id(),
                            numBase, attrNoIcon
                    ).withStyle(ChatFormatting.DARK_GREEN);
                    consumer.accept(Component.empty().append(iconText).append(body));
                    ci.cancel();
                    return;
                }
            }

            ci.cancel();
        }
    }
}