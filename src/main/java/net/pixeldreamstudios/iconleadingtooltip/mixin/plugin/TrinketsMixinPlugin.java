package net.pixeldreamstudios.iconleadingtooltip.mixin.plugin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.throwables.MixinApplyError;

import java.util.List;
import java.util.Set;

public final class TrinketsMixinPlugin implements IMixinConfigPlugin, IMixinErrorHandler {
    private static final String MOD_ID = "trinkets";
    private boolean enabled;

    @Override
    public void onLoad(String mixinPackage) {
        enabled = FabricLoader.getInstance().isModLoaded(MOD_ID);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return enabled;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        System.out.println("[IconLeadingTooltip] Applied mixin: " + mixinClassName + " -> " + targetClassName);
    }



    @Override
    public ErrorAction onPrepareError(IMixinConfig config, Throwable th, IMixinInfo mixin, ErrorAction action) {
        System.err.println("[IconLeadingTooltip] Error preparing mixin " + mixin.getName() + " for " + mixin.getTargetClasses() + ": " + th.getMessage());
        th.printStackTrace();
        return action;
    }

    @Override
    public IMixinErrorHandler.ErrorAction onApplyError(String targetClassName, Throwable th, IMixinInfo mixinInfo, IMixinErrorHandler.ErrorAction action) {
        System.err.println("[IconLeadingTooltip] Error applying mixin " + mixinInfo.getName() + " to " + targetClassName + ": " + th.getMessage());
        if (th instanceof MixinApplyError && th.getCause() != null) {
            th.getCause().printStackTrace();
        } else {
            th.printStackTrace();
        }
        return action;
    }
}
