package com.toast.apocalypse.client.renderer.entity.living.ghost;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.toast.apocalypse.client.ApocalypseRenderTypes;
import com.toast.apocalypse.common.entity.living.Ghost;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.util.Mth;

// Model author: Frenderman

public class GhostModel<T extends Ghost> extends HierarchicalModel<T> {

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public GhostModel(ModelPart root) {
        super((resourceLocation) -> ApocalypseRenderTypes.entityCutoutNoCullBlend(resourceLocation, RenderStateShard.TransparencyStateShard.ADDITIVE_TRANSPARENCY));
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.rightArm = body.getChild("rightArm");
        this.leftArm = body.getChild("leftArm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(20, 41).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 19.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -13.0F, -4.0F, 10.0F, 13.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition rightArm = body.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(0, 32).addBox(-4.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 2.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition leftArm = body.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(0, 32).mirror().addBox(0.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.0F, 2.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T ghost, float limbSwing, float limbSwingAmount, float partialTick, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * (float) Math.PI / 180.0F;
        head.xRot = headPitch * (float) Math.PI / 180.0F;

        if (!ghost.isFrozen()) {
            bobArmsAndHead(rightArm, leftArm, head, partialTick);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return body;
    }

    private static void bobArmsAndHead(ModelPart rightArm, ModelPart leftArm, ModelPart head, float partialTick) {
        float f = Mth.sin(1 * (float)Math.PI);
        float f1 = Mth.sin((float) Math.PI);
        leftArm.zRot = 0.0F;
        rightArm.zRot = 0.0F;
        leftArm.yRot = -(0.1F - f * 0.6F);
        rightArm.yRot = 0.1F - f * 0.6F;
        float f2 = -(float)Math.PI / 2.25F;
        leftArm.xRot = f2;
        rightArm.xRot = f2;
        leftArm.xRot += f * 1.2F - f1 * 0.4F;
        rightArm.xRot += f * 1.2F - f1 * 0.4F;
        AnimationUtils.bobArms(rightArm, leftArm, partialTick);
    }

    public void setRotationAngle(ModelPart modelPart, float x, float y, float z) {
        modelPart.xRot = x;
        modelPart.yRot = y;
        modelPart.zRot = z;
    }
}
