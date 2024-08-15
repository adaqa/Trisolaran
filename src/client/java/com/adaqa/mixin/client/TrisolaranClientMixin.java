package com.adaqa.mixin.client;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.*;


@Mixin(WorldRenderer.class)
public abstract class TrisolaranClientMixin {
	//@Shadow private @Nullable ClientWorld world;

	/*
	@ModifyConstant(method = "renderSky", constant = @Constant(floatValue = -90.0F))
	private float modifySkyRotationX(float rotation) {
		return (float)Math.sin(this.world.getTime()/240.0F) * 360.0F - 90.0F;
	}
	*/
}