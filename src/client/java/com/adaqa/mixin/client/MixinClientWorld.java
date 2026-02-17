package com.adaqa.mixin.client;

import com.adaqa.simulation.TrisolaranSystem;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class MixinClientWorld {
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void onGetSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        // 使用我们自定义的三体天空颜色计算
        // 这将影响雾气颜色和原版天空颜色
        Vec3d calculatedColor = TrisolaranSystem.getCalculatedSkyColor();
        

        
        cir.setReturnValue(calculatedColor);
    }
}
