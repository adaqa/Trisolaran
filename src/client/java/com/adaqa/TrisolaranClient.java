package com.adaqa;

import com.adaqa.gui.CelestialLocatorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import com.adaqa.simulation.TrisolaranSystem;

public class TrisolaranClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// 创建自定义的天空渲染器实例
		CustomSkyRenderer skyRenderer = new CustomSkyRenderer();

		// 注册自定义的天空渲染器到主世界
		DimensionRenderingRegistry.registerSkyRenderer(World.OVERWORLD, skyRenderer);

		// 客户端不再直接驱动物理模拟，而是依赖服务端数据同步 (简化起见，目前单机模式下内存共享)
		// 但为了平滑渲染，客户端仍需 Tick？
		// 实际上，如果单机游戏，TrisolaranSystem 的静态实例在服务端和客户端线程间是共享的（需注意并发，但这里简化处理）
		// 如果是联机，需要网络包同步。鉴于当前任务未提及网络同步，假设为单机环境优化。
		
		// 监听物品使用，打开 GUI
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (world.isClient && player.getStackInHand(hand).isOf(Trisolaran.CELESTIAL_LOCATOR)) {
				net.minecraft.client.MinecraftClient.getInstance().setScreen(new CelestialLocatorScreen());
				return TypedActionResult.success(player.getStackInHand(hand));
			}
			return TypedActionResult.pass(player.getStackInHand(hand));
		});
	}
}
