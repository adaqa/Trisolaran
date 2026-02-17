package com.adaqa.item;

import com.adaqa.simulation.TrisolaranSystem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class CelestialLocatorItem extends Item {
    public CelestialLocatorItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        for (int i = 0; i < 3; i++) {
            Vec3d pos = TrisolaranSystem.getSunPos(i);
            tooltip.add(Text.literal(String.format("Sun %d: (%.1f, %.1f, %.1f)", i + 1, pos.x, pos.y, pos.z))
                    .formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
