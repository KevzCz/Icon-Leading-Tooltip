package net.pixeldreamstudios.iconleadingtooltip.mixin.plugin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class TrinketsMixinPlugin implements IMixinConfigPlugin {
    private static final String MOD_ID = "trinkets";
    private boolean enabled;

    @Override
    public void onLoad(String mixinPackage) {
        enabled = FabricLoader.getInstance().isModLoaded(MOD_ID);
    }

    @Override public String getRefMapperConfig() { return null; }
    @Override
    public List<String> getMixins() {
        return enabled ? List.of("TrinketsTooltipRedirectMixin") : List.of();
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return enabled;
    }

    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
