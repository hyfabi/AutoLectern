package sys.exe.al.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sys.exe.al.AutoLectern;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow private volatile boolean running;

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void onClientTick(CallbackInfo ci) {
        AutoLectern.getInstance().MinecraftTickHead((MinecraftClient) (Object) this);
    }
    @Inject(method = "scheduleStop", at = @At("HEAD"))
    private void onStop(CallbackInfo ci){
        if(running)
            AutoLectern.getInstance().saveConfig();
    }
}
