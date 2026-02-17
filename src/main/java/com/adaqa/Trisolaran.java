package com.adaqa;

import com.adaqa.item.CelestialLocatorItem;
import com.adaqa.simulation.TrisolaranSavedData;
import com.adaqa.simulation.TrisolaranSystem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Trisolaran implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("trisolaran");

	public static final Item CELESTIAL_LOCATOR = new CelestialLocatorItem(new Item.Settings().rarity(Rarity.EPIC));

	public static final RegistryKey<ItemGroup> TRISOLARAN_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of("trisolaran", "item_group"));
	public static final ItemGroup TRISOLARAN_GROUP = FabricItemGroup.builder()
			.icon(() -> new ItemStack(CELESTIAL_LOCATOR))
			.displayName(Text.translatable("itemGroup.trisolaran.general"))
			.entries((context, entries) -> {
				entries.add(CELESTIAL_LOCATOR);
			})
			.build();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Trisolaran Mod...");

		Registry.register(Registries.ITEM, Identifier.of("trisolaran", "celestial_locator"), CELESTIAL_LOCATOR);
		Registry.register(Registries.ITEM_GROUP, TRISOLARAN_GROUP_KEY, TRISOLARAN_GROUP);

		// 在服务端 Tick 中更新三体系统，并保存数据
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (world.getRegistryKey() == net.minecraft.world.World.OVERWORLD && world instanceof ServerWorld) {
				ServerWorld serverWorld = (ServerWorld) world;
				// 获取（或加载）存档数据
				TrisolaranSavedData data = TrisolaranSavedData.getServerState(serverWorld);
				
				// 步进物理模拟
				TrisolaranSystem.tick();
				
				// 标记脏数据，以便保存
				data.markDirty();
			}
		});
	}
}
