package sys.exe.al.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sys.exe.al.ALState;
import sys.exe.al.AutoLectern;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @SuppressWarnings("unused")
    @Shadow
    private long lastWindowFocusedTime;

    @Inject(method = "render", at = @At("HEAD"))
    public void render(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (AutoLectern.getInstance().getState() != ALState.STOPPED)
            lastWindowFocusedTime = Util.getMeasuringTimeMs();
    }
}
