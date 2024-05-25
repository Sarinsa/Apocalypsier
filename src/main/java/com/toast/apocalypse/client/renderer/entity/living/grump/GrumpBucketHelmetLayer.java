package com.toast.apocalypse.client.renderer.entity.living.grump;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.toast.apocalypse.client.ApocalypseModelLayers;
import com.toast.apocalypse.client.renderer.model.armor.GrumpBucketHelmetModel;
import com.toast.apocalypse.common.core.register.ApocalypseItems;
import com.toast.apocalypse.common.entity.living.Grump;
import com.toast.apocalypse.common.item.BucketHelmetItem;
import net.minecraft.client.model.GhastModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GrumpBucketHelmetLayer<T extends Grump, M extends GhastModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(BucketHelmetItem.TEXTURE);
    private final GrumpBucketHelmetModel<T> bucketModel;

    public GrumpBucketHelmetLayer(RenderLayerParent<T, M> parent, EntityModelSet modelSet) {
        super(parent);
        bucketModel = new GrumpBucketHelmetModel<>(modelSet.bakeLayer(ApocalypseModelLayers.GRUMP_BUCKET_HELMET));
    }


    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T grump, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack headStack = grump.getHeadItem();

        if (!headStack.isEmpty() && headStack.getItem() == ApocalypseItems.BUCKET_HELM.get()) {
            VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(TEXTURE), false, headStack.hasFoil());
            bucketModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
