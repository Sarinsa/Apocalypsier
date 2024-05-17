package com.toast.apocalypse.common.item;

import com.toast.apocalypse.common.util.References;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class FatherlyToastItem extends Item {

    public FatherlyToastItem() {
        super(new Item.Properties()
                .fireResistant()
                .food(ApocalypseFoods.FATHERLY_TOAST)
        );
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level world, LivingEntity livingEntity) {
        if (this.isEdible()) {
            if (livingEntity instanceof Player player && !player.getAbilities().instabuild) {
                // Setting creative players on fire just makes the fire
                // extinguish instantly, which is weird to look at.
                livingEntity.setSecondsOnFire(1000);
            }
            return livingEntity.eat(world, itemStack);
        }
        return itemStack;
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(References.FATHERLY_TOAST_DESC).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));

        if (itemStack.hasTag() && itemStack.getTag().contains("ToastLevel", Tag.TAG_INT)) {
            tooltip.add(Component.translatable(References.FATHERLY_TOAST_LEVEL, itemStack.getTag().getInt("ToastLevel")).withStyle(ChatFormatting.GRAY));
        }
    }
}
