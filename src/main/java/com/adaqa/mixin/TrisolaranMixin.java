package com.adaqa.mixin;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;


@Mixin(DimensionType.class)
public abstract class TrisolaranMixin {
    @ModifyArg(method = "getSkyAngle", at = @At(value = "INVOKE", target = "net/minecraft/util/math/MathHelper.fractionalPart(D)D"))
    private double getSkyAngle(double days) {
        // 使用三体系统计算的天空角度
        return com.adaqa.simulation.TrisolaranSystem.getCalculatedSkyAngle();
    }
}