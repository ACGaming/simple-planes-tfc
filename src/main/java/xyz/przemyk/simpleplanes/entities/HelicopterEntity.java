package xyz.przemyk.simpleplanes.entities;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xyz.przemyk.simpleplanes.SimplePlanesMod;
import xyz.przemyk.simpleplanes.math.MathUtil;
import xyz.przemyk.simpleplanes.math.Quaternion;
import xyz.przemyk.simpleplanes.math.Vector3f;
import xyz.przemyk.simpleplanes.setup.PlaneMaterial;
import xyz.przemyk.simpleplanes.upgrades.Upgrade;
import xyz.przemyk.simpleplanes.upgrades.UpgradeType;

public class HelicopterEntity extends LargePlaneEntity {
    public HelicopterEntity(World worldIn) {
        super(worldIn);
    }

    public HelicopterEntity(World worldIn, PlaneMaterial material) {
        super(worldIn, material);
    }

    public HelicopterEntity(World worldIn, PlaneMaterial material, double x, double y, double z) {
        super(worldIn, material, x, y, z);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    @Override
    protected Vars getMotionVars() {
        Vars motionVars = super.getMotionVars();
        motionVars.passive_engine_push = 0.028f;
        motionVars.push = 0.05f;
        motionVars.drag_quad *= 10;
        motionVars.drag_mul *= 2;
        return motionVars;
    }

    @Override
    protected void tickMotion(Vars vars) {
        super.tickMotion(vars);
    }

    @Override
    protected Vector3f getTickPush(Vars vars) {
        if (vars.moveForward < 0 && isPowered() && !vars.passengerSprinting) {
            vars.push *= 0.2;
        }
        return transformPos(new Vector3f(0, vars.push, 0));
    }

    @Override
    protected Item getItem() {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(SimplePlanesMod.MODID, getMaterial().name + "_helicopter"));
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (world.isRemote && Minecraft.getMinecraft().player == passenger) {
            (Minecraft.getMinecraft()).ingameGUI.setOverlayMessage(new TextComponentString("sprint to take off"), false);
        }
    }

    @Override
    public double getCameraDistanceMultiplayer() {
        return super.getCameraDistanceMultiplayer();
    }

    @Override
    protected boolean isEasy() {
        return true;
    }

    protected void tickPitch(Vars vars) {
        if (vars.moveForward > 0.0F) {
            rotationPitch = Math.max(rotationPitch - 1, -20);
        } else if (vars.moveForward < 0 && vars.passengerSprinting) {
            rotationPitch = Math.min(rotationPitch + 1, 10);
        } else {
            rotationPitch = MathUtil.lerpAngle(0.2f, rotationPitch, 0);
            double drag = 0.999;
            setMotion(getMotion().scale(drag));
        }
    }

    @Override
    protected boolean tickOnGround(Vars vars) {
        float push = vars.push;
        super.tickOnGround(vars);
        if (vars.passengerSprinting) {
            vars.push = push;
        } else {
            vars.push = 0;
        }
        return false;
    }

    @Override
    protected float getGroundPitch() {
        return 0;
    }

    @Override
    protected Quaternion tickRotateMotion(Vars vars, Quaternion q, Vec3d motion) {
        //        float yaw = MathUtil.getYaw(motion);
        //        double speed = getMotion().length();
        //
        //        setMotion(MathUtil.rotationToVector(lerpAngle180(0.1f, yaw, rotationYaw),
        //                rotationPitch,
        //                speed));
        return q;
    }

    @Override
    protected void tickRotation(Vars vars) {
        int yawdiff = 2;
        double turn = vars.moveStrafing > 0 ? yawdiff : vars.moveStrafing == 0 ? 0 : -yawdiff;
        rotationRoll = 0;
        rotationYaw -= turn;
    }

    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return super.canFitPassenger(passenger) && passenger instanceof EntityPlayer;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);
    }

    @Override
    public boolean canAddUpgrade(UpgradeType upgradeType) {
        if (upgradeType.occupyBackSeat) {
            if (getPassengers().size() > 1) {
                return false;
            }
            for (Upgrade upgrade : upgrades.values()) {
                if (upgrade.getType().occupyBackSeat) {
                    return false;
                }
            }
        }
        return !upgrades.containsKey(upgradeType.getRegistryName()) && upgradeType.isPlaneApplicable(this);
    }

    @Override
    protected Vector3f getPassengerTwoPos(Entity passenger) {
        return new Vector3f(0, (float) (super.getMountedYOffset() + passenger.getYOffset()), -0.8f);
    }
}