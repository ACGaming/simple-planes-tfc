package xyz.przemyk.simpleplanes.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import xyz.przemyk.simpleplanes.entities.PlaneEntity;
import xyz.przemyk.simpleplanes.math.MathUtil;
import xyz.przemyk.simpleplanes.math.Quaternion;
import xyz.przemyk.simpleplanes.upgrades.Upgrade;

import static xyz.przemyk.simpleplanes.math.MathUtil.rotationDegreesY;

public abstract class AbstractPlaneRenderer<T extends PlaneEntity> extends Render<T> {
    protected PropellerModel propellerModel;

    protected AbstractPlaneRenderer(RenderManager renderManager) {
        super(renderManager);
        propellerModel = new PropellerModel();
    }

    @Override
    public void doRender(T planeEntity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 0.375F, (float) z);
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.translate(0, -0.5, 0);
        GlStateManager.rotate(rotationDegreesY(180).convert());
        this.bindEntityTexture((planeEntity));

        double firstPersonYOffset = -0.7D;
        boolean isPlayerRidingInFirstPersonView = Minecraft.getMinecraft().player != null && planeEntity.isPassenger(Minecraft.getMinecraft().player) && (Minecraft.getMinecraft()).gameSettings.thirdPersonView == 0;
        if (isPlayerRidingInFirstPersonView) {
            GlStateManager.translate(0.0D, firstPersonYOffset, 0.0D);
        }
        Quaternion q = MathUtil.lerpQ(partialTicks, planeEntity.getQ_Prev(), planeEntity.getQ_Client());
        GlStateManager.rotate(q.convert());

        float rockingAngle = planeEntity.getRockingAngle(partialTicks);
        float f = (float) planeEntity.getTimeSinceHit() - partialTicks;
        float f1 = planeEntity.getDamageTaken() - partialTicks;
        if (f1 < 0.1F) {
            f1 = 0.1F;
        }

        if (f > 0.0F) {
            float angle = MathUtil.clamp(f * f1 / 200.0F, -30, 30);
            f = planeEntity.ticksExisted + partialTicks;
            GlStateManager.rotate(MathUtil.rotationDegreesZ(MathHelper.sin(f) * angle).convert());
        }

        GlStateManager.translate(0, -0.6, 0);

        if (isPlayerRidingInFirstPersonView) {
            GlStateManager.translate(0.0D, -firstPersonYOffset, 0.0D);
        }

        ModelBase planeModel = getModel();

        boolean enchanted_plane = planeEntity.getHealth() > planeEntity.getMaxHealth();

        planeModel.setRotationAngles(partialTicks, 0, 0, 0, 0, 0, planeEntity);
        planeModel.render(planeEntity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        int seat = 0;
        for (Upgrade upgrade : planeEntity.upgrades.values()) {
            GlStateManager.pushMatrix();
            ResourceLocation texture = upgrade.getTexture();
            if (texture != null) {
                bindTexture(texture);
            }

            if (upgrade.getType().occupyBackSeat) {
                for (int i = 0; i < upgrade.getSeats(); i++) {
                    GlStateManager.pushMatrix();
                    BackSeatBlockModel.moveMatrix(planeEntity, seat);
                    upgrade.render(partialTicks, 0.0625F);
                    seat++;
                    GlStateManager.popMatrix();
                }
            } else {
                upgrade.render(partialTicks, 0.0625F);
            }
            GlStateManager.popMatrix();
        }

        propellerModel.setRotationAngles(partialTicks, 0, 0, 0, 0, 0, planeEntity);
        this.bindTexture(new ResourceLocation("tfc", "textures/blocks/metal/wrought_iron.png"));
        propellerModel.render(planeEntity, partialTicks, 0, 0, 0, 0, 0.0625F);

        GlStateManager.pushMatrix();
        renderAdditional(planeEntity, partialTicks);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();

        super.doRender(planeEntity, x, y, z, entityYaw, partialTicks);
    }

    protected void renderAdditional(T planeEntity, float partialTicks) {
    }

    protected abstract ModelBase getModel();
}