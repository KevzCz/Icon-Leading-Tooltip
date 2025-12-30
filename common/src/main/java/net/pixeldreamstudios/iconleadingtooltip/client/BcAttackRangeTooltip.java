package net.pixeldreamstudios.iconleadingtooltip.client;

import dev.architectury.platform.Platform;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.EntityAttributeHelper;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.pixeldreamstudios.iconleadingtooltip.util.IconLeadingUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class BcAttackRangeTooltip {

    private BcAttackRangeTooltip() {}

    public static void append(ItemStack stack, LocalPlayer player, List<Component> lines) {
        if (! Platform.isModLoaded("bettercombat")) return;
        if (Platform.isModLoaded("dynamictooltips")) return;

        WeaponAttributes attributes = WeaponRegistry.getAttributes(stack);
        if (attributes == null || attributes.attacks() == null || attributes.attacks().length == 0) return;

        AttributeInstance reachAttr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (reachAttr == null) return;

        ModifierTracker tracker = new ModifierTracker(reachAttr.getBaseValue());
        addAllReachModifiersFrom(player, tracker);

        ItemStack equipped = player.getMainHandItem();
        boolean viewingEquipped = ItemStack.isSameItemSameComponents(stack, equipped);

        if (!viewingEquipped) {
            removeMainHandReachFrom(equipped, tracker);
            addMainHandReachFrom(stack, tracker);
        }

        tracker.calculateValue();
        if (! EntityAttributeHelper.itemHasRangeAttribute(stack)) {
            tracker.currentTrackedValue += attributes.rangeBonus();
        }

        double totalRange = tracker.currentTrackedValue;
        if (totalRange <= 0.0) return;

        int insertAt = findInsertIndex(lines);
        MutableComponent rangeLine = buildRangeLine(totalRange);
        lines.add(Math.min(insertAt + 1, lines.size()), rangeLine);
    }

    private static int findInsertIndex(List<Component> lines) {
        int lastAttributeLine = -1;
        Integer lastGreenAttributeIndex = null;

        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
            var content = line.getContents();

            if (content instanceof TranslatableContents tr) {
                String key = tr.getKey();
                if (key.startsWith("attribute.modifier")) {
                    lastAttributeLine = i;
                }
            } else {
                for (Component part : line.getSiblings()) {
                    var pc = part.getContents();
                    if (pc instanceof TranslatableContents ptr) {
                        String pKey = ptr.getKey();
                        if (pKey.contains("attribute.modifier.equals.0")) {
                            lastGreenAttributeIndex = i;
                        }
                        if (pKey.startsWith("attribute.modifier")) {
                            lastAttributeLine = i;
                        }
                    }
                }
            }
        }

        if (lastGreenAttributeIndex != null) return lastGreenAttributeIndex;
        if (lastAttributeLine >= 0) return lastAttributeLine;
        return lines.size() - 1;
    }

    private static MutableComponent buildRangeLine(double range) {
        Component attr = Component.translatable("attribute.name.generic.attack_range");
        String raw = attr.getString();
        int[] span = IconLeadingUtil.firstIconSpan(raw);

        if (span[0] >= 0) {
            String icon = raw.substring(span[0], span[1]);
            String restRaw = raw.substring(0, span[0]) + raw.substring(span[1]);
            String rest = IconLeadingUtil.stripSectionCodes(restRaw).replaceFirst("^\\s+", "");
            MutableComponent attrNoIcon = Component.literal(rest);
            MutableComponent body = Component.translatable(
                    "attribute.modifier.equals.0",
                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(range),
                    attrNoIcon
            ).withStyle(ChatFormatting.DARK_GREEN);
            return Component.literal("").append(Component.literal(icon + " ")).append(body);
        } else {
            return Component.literal("").append(Component.translatable(
                    "attribute.modifier.equals.0",
                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(range),
                    attr
            ).withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    private static void addAllReachModifiersFrom(LocalPlayer player, ModifierTracker tracker) {
        AttributeInstance reach = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (reach == null) return;
        for (AttributeModifier m : reach.getModifiers()) {
            tracker.addModifier(m);
        }
    }

    private static void addMainHandReachFrom(ItemStack stack, ModifierTracker tracker) {
        stack.forEachModifier(EquipmentSlotGroup.MAINHAND, (Holder<Attribute> attr, AttributeModifier m) -> {
            if (attr.is(Attributes.BLOCK_INTERACTION_RANGE)) {
                tracker.addModifier(m);
            }
        });
    }

    private static void removeMainHandReachFrom(ItemStack stack, ModifierTracker tracker) {
        if (stack.isEmpty()) return;
        stack.forEachModifier(EquipmentSlotGroup.MAINHAND, (Holder<Attribute> attr, AttributeModifier m) -> {
            if (attr.is(Attributes.BLOCK_INTERACTION_RANGE)) {
                tracker.removeModifierById(m.id());
            }
        });
    }

    private static final class ModifierTracker {
        final double initialBaseReach;
        final List<AttributeModifier> applicableModifiers = new ArrayList<>();
        double currentTrackedValue;

        ModifierTracker(double playerBaseReach) {
            this.initialBaseReach = playerBaseReach;
            this.currentTrackedValue = playerBaseReach;
        }

        void addModifier(AttributeModifier modifier) {
            if (modifier.amount() == 0.0) return;
            for (AttributeModifier m : applicableModifiers) {
                if (m.id().equals(modifier.id())) return;
            }
            applicableModifiers.add(modifier);
        }

        void removeModifierById(ResourceLocation id) {
            applicableModifiers.removeIf(m -> m.id().equals(id));
        }

        void calculateValue() {
            applicableModifiers.sort(Comparator
                    .comparing(AttributeModifier::operation)
                    .thenComparing((a) -> -Math.abs(a.amount()))
                    .thenComparing(AttributeModifier::id));

            double value = initialBaseReach;

            for (AttributeModifier m : applicableModifiers) {
                if (m.operation() == AttributeModifier.Operation.ADD_VALUE) {
                    value += m.amount();
                }
            }

            double addMultBase = 0.0;
            for (AttributeModifier m : applicableModifiers) {
                if (m.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
                    addMultBase += initialBaseReach * m.amount();
                }
            }
            value += addMultBase;

            for (AttributeModifier m : applicableModifiers) {
                if (m.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    value *= (1.0 + m.amount());
                }
            }

            currentTrackedValue = value;
        }
    }
}