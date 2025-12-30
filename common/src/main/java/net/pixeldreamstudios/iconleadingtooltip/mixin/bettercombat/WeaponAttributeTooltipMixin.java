package net.pixeldreamstudios.iconleadingtooltip.mixin.bettercombat;

import dev.architectury.platform.Platform;
import net.bettercombat.client.WeaponAttributeTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(WeaponAttributeTooltip.class)
public class WeaponAttributeTooltipMixin {

    @Inject(method = "modifyTooltip", at = @At("HEAD"), cancellable = true)
    private static void cancelTooltip(ItemStack itemStack, List<Component> lines, CallbackInfo ci) {
        if (Platform.isModLoaded("dynamictooltips")) {
            return;
        }
        if (! Platform.isModLoaded("bettercombat")) {
            return;
        }
        ci.cancel();
    }
}