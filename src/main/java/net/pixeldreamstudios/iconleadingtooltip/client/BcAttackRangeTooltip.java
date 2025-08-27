package net.pixeldreamstudios.iconleadingtooltip.client;

import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.EntityAttributeHelper;
import net.bettercombat.logic.WeaponRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.iconleadingtooltip.util.IconLeadingUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
@Environment(EnvType.CLIENT)
public final class BcAttackRangeTooltip {

    private BcAttackRangeTooltip() {}

    public static void append(ItemStack stack, ClientPlayerEntity player, List<Text> lines) {
        if (!FabricLoader.getInstance().isModLoaded("bettercombat")) return;
        if (FabricLoader.getInstance().isModLoaded("dynamictooltips")) return;

        WeaponAttributes attributes = WeaponRegistry.getAttributes(stack);
        if (attributes == null || attributes.attacks() == null || attributes.attacks().length == 0) return;

        EntityAttributeInstance reachAttr = player.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
        if (reachAttr == null) return;

        ModifierTracker tracker = new ModifierTracker(reachAttr.getBaseValue());
        addAllReachModifiersFrom(player, tracker);

        ItemStack equipped = player.getMainHandStack();
        boolean viewingEquipped = ItemStack.areEqual(stack, equipped);

        if (!viewingEquipped) {
            removeMainHandReachFrom(equipped, tracker);
            addMainHandReachFrom(stack, tracker);
        }

        tracker.calculateValue();
        if (!EntityAttributeHelper.itemHasRangeAttribute(stack)) {
            tracker.currentTrackedValue += attributes.rangeBonus();
        }

        double totalRange = tracker.currentTrackedValue;
        if (totalRange <= 0.0) return;

        int insertAt = findInsertIndex(lines);
        MutableText rangeLine = buildRangeLine(totalRange);
        lines.add(Math.min(insertAt + 1, lines.size()), rangeLine);
    }

    private static int findInsertIndex(List<Text> lines) {
        int lastAttributeLine = -1;
        Integer lastGreenAttributeIndex = null;

        for (int i = 0; i < lines.size(); i++) {
            Text line = lines.get(i);
            TextContent content = line.getContent();

            if (content instanceof TranslatableTextContent tr) {
                String key = tr.getKey();
                if (key.startsWith("attribute.modifier")) {
                    lastAttributeLine = i;
                }
            } else {
                for (Text part : line.getSiblings()) {
                    TextContent pc = part.getContent();
                    if (pc instanceof TranslatableTextContent ptr) {
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

    private static MutableText buildRangeLine(double range) {
        Text attr = Text.translatable("attribute.name.generic.attack_range");
        String raw = attr.getString();
        int[] span = IconLeadingUtil.firstIconSpan(raw);

        if (span[0] >= 0) {
            String icon = raw.substring(span[0], span[1]);
            String restRaw = raw.substring(0, span[0]) + raw.substring(span[1]);
            String rest = IconLeadingUtil.stripSectionCodes(restRaw).replaceFirst("^\\s+", "");
            MutableText attrNoIcon = Text.literal(rest);
            MutableText body = Text.translatable(
                    "attribute.modifier.equals.0",
                    AttributeModifiersComponent.DECIMAL_FORMAT.format(range),
                    attrNoIcon
            ).formatted(Formatting.DARK_GREEN);
            return Text.literal("").append(Text.literal(icon + " ")).append(body);
        } else {
            return Text.literal("").append(Text.translatable(
                    "attribute.modifier.equals.0",
                    AttributeModifiersComponent.DECIMAL_FORMAT.format(range),
                    attr
            ).formatted(Formatting.DARK_GREEN));
        }
    }

    private static void addAllReachModifiersFrom(ClientPlayerEntity player, ModifierTracker tracker) {
        EntityAttributeInstance reach = player.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
        if (reach == null) return;
        for (EntityAttributeModifier m : reach.getModifiers()) {
            tracker.addModifier(m);
        }
    }

    private static void addMainHandReachFrom(ItemStack stack, ModifierTracker tracker) {
        stack.applyAttributeModifier(AttributeModifierSlot.MAINHAND, (RegistryEntry<EntityAttribute> attr, EntityAttributeModifier m) -> {
            if (attr.value() == EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE.value()) {
                tracker.addModifier(m);
            }
        });
    }

    private static void removeMainHandReachFrom(ItemStack stack, ModifierTracker tracker) {
        if (stack.isEmpty()) return;
        stack.applyAttributeModifier(AttributeModifierSlot.MAINHAND, (RegistryEntry<EntityAttribute> attr, EntityAttributeModifier m) -> {
            if (attr.value() == EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE.value()) {
                tracker.removeModifierById(m.id());
            }
        });
    }

    private static final class ModifierTracker {
        final double initialBaseReach;
        final List<EntityAttributeModifier> applicableModifiers = new ArrayList<>();
        double currentTrackedValue;

        ModifierTracker(double playerBaseReach) {
            this.initialBaseReach = playerBaseReach;
            this.currentTrackedValue = playerBaseReach;
        }

        void addModifier(EntityAttributeModifier modifier) {
            if (modifier.value() == 0.0) return;
            for (EntityAttributeModifier m : applicableModifiers) {
                if (m.id().equals(modifier.id())) return;
            }
            applicableModifiers.add(modifier);
        }

        void removeModifierById(Identifier id) {
            applicableModifiers.removeIf(m -> m.id().equals(id));
        }

        void calculateValue() {
            applicableModifiers.sort(Comparator
                    .comparing(EntityAttributeModifier::operation)
                    .thenComparing((a) -> -Math.abs(a.value()))
                    .thenComparing(EntityAttributeModifier::id));

            double value = initialBaseReach;

            for (EntityAttributeModifier m : applicableModifiers) {
                if (m.operation() == EntityAttributeModifier.Operation.ADD_VALUE) {
                    value += m.value();
                }
            }

            double addMultBase = 0.0;
            for (EntityAttributeModifier m : applicableModifiers) {
                if (m.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
                    addMultBase += initialBaseReach * m.value();
                }
            }
            value += addMultBase;

            for (EntityAttributeModifier m : applicableModifiers) {
                if (m.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    value *= (1.0 + m.value());
                }
            }

            currentTrackedValue = value;
        }
    }
}
