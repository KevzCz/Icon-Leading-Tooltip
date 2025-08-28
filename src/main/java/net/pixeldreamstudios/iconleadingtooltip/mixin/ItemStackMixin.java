package net.pixeldreamstudios.iconleadingtooltip.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.pixeldreamstudios.iconleadingtooltip.util.IconLeadingUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@Inject(
			method = "appendAttributeModifierTooltip(Ljava/util/function/Consumer;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/entity/attribute/EntityAttributeModifier;)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private void iconleadingtooltip$iconBeforeNumber(
			Consumer<Text> textConsumer,
			@Nullable PlayerEntity player,
			RegistryEntry<EntityAttribute> attribute,
			EntityAttributeModifier modifier,
			CallbackInfo ci) {

		if (FabricLoader.getInstance().isModLoaded("dynamictooltips")) {
			return;
		}

		double d = modifier.value();
		boolean usesBase = false;
		if (player != null) {
			if (modifier.idMatches(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID)) {
				d += player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
				usesBase = true;
			} else if (modifier.idMatches(Item.BASE_ATTACK_SPEED_MODIFIER_ID)) {
				d += player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED);
				usesBase = true;
			}
		}

		double shown = switch (modifier.operation()) {
			case ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL -> d * 100.0;
			default -> attribute.matches(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) ? d * 10.0 : d;
		};

		// Pull icon from the attribute's translated name
		MutableText attrName = Text.translatable(attribute.value().getTranslationKey());
		String raw = attrName.getString();
		int[] span = IconLeadingUtil.firstIconSpan(raw);
		if (span[0] < 0) return;

		String icon = raw.substring(span[0], span[1]);
		String restRaw = raw.substring(0, span[0]) + raw.substring(span[1]);
		String rest = IconLeadingUtil.stripSectionCodes(restRaw).replaceFirst("^\\s+", "");
		MutableText attrNoIcon = Text.literal(rest);

		String numPlus = AttributeModifiersComponent.DECIMAL_FORMAT.format(shown);
		String numTake = AttributeModifiersComponent.DECIMAL_FORMAT.format(-shown); // absolute number for "take"

		Text iconText = Text.literal(icon + " ").styled(s ->
				s.withColor(0xFFFFFF)
						.withBold(false)
						.withItalic(false)
						.withUnderline(false)
						.withStrikethrough(false)
						.withObfuscated(false)
		);

		if (usesBase) {
			MutableText body = Text.translatable(
					"attribute.modifier.equals." + modifier.operation().getId(),
					numPlus, attrNoIcon
			).formatted(Formatting.DARK_GREEN);
			textConsumer.accept(Text.empty().append(iconText).append(body));
		} else if (d > 0.0) {
			MutableText body = Text.translatable(
					"attribute.modifier.plus." + modifier.operation().getId(),
					numPlus, attrNoIcon
			).formatted(attribute.value().getFormatting(true));
			textConsumer.accept(Text.empty().append(iconText).append(body));
		} else if (d < 0.0) {
			MutableText body = Text.translatable(
					"attribute.modifier.take." + modifier.operation().getId(),
					numTake, attrNoIcon
			).formatted(attribute.value().getFormatting(false));
			textConsumer.accept(Text.empty().append(iconText).append(body));
		} else {
			return;
		}

		ci.cancel();
	}
}
