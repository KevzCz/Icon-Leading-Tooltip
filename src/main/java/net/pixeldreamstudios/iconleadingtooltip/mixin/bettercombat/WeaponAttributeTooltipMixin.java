package net.pixeldreamstudios.iconleadingtooltip.mixin.bettercombat;

import net.bettercombat.client.WeaponAttributeTooltip;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.Component;
import net.minecraft.item.ItemStack;
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
        if (FabricLoader.getInstance().isModLoaded("dynamictooltips")) {
            return;
        }
        if (!FabricLoader.getInstance().isModLoaded("bettercombat")) {
            return;
        }
        ci.cancel();
    }
}