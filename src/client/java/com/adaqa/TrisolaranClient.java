package com.adaqa;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.minecraft.world.World;

public class TrisolaranClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// 创建自定义的天空渲染器实例
		CustomSkyRenderer skyRenderer = new CustomSkyRenderer();

		// 注册自定义的天空渲染器到主世界
		DimensionRenderingRegistry.registerSkyRenderer(World.OVERWORLD, skyRenderer);
	}
}