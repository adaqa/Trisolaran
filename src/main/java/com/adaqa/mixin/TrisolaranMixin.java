package com.adaqa.mixin;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;


@Mixin(DimensionType.class)
public abstract class TrisolaranMixin {
	@ModifyArg(method = "getSkyAngle", at = @At(value = "INVOKE", target = "net/minecraft/util/math/MathHelper.fractionalPart(D)D"))
	private double getSkyAngle(double days) {
		System.out.println(days);
		return Math.sin(days*100)*10 + days*5;
	}
}