package com.toast.apocalypse.common.item;

import com.toast.apocalypse.client.ClientUtil;
import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.util.References;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class BucketHelmetItem extends ArmorItem {

    public static final String TEXTURE = Apocalypse.resourceLoc("textures/models/armor/bucket_helm.png").toString();

    public BucketHelmetItem() {
        super(ArmorMaterials.IRON, Type.HELMET, new Item.Properties().defaultDurability(0));
    }

    @Override
    @Nullable
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return TEXTURE;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                return equipmentSlot == EquipmentSlot.HEAD ? ClientUtil.BUCKET_HELMET_MODEL.copyProps(original) : original;
            }

            @Override
            public void renderHelmetOverlay(ItemStack stack, Player player, int width, int height, float partialTick) {
                ClientUtil.renderBucketHelmOverlay(width, height);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(References.BUCKET_HELM_DESC).withStyle(ChatFormatting.GRAY));
    }
}
