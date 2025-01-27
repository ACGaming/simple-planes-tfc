package xyz.przemyk.simpleplanes.handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.przemyk.simpleplanes.entities.PlaneEntity;

@Mod.EventBusSubscriber
public class SimplePlanesEvents {
    @SubscribeEvent
    public static void interact(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        Entity entity = player.getLowestRidingEntity();
        if (entity instanceof PlaneEntity) {
            ItemStack itemStack = player.getHeldItem(event.getHand());

            if (itemStack.isEmpty()) {
                return;
            }

            PlaneEntity planeEntity = (PlaneEntity) entity;
            planeEntity.rightClickUpgrades(player, event.getHand(), itemStack);

            if (itemStack.isEmpty()) {
                return;
            }

            planeEntity.tryToAddUpgrade(player, itemStack);
        }
    }

    @SubscribeEvent
    public static void onImpact(ProjectileImpactEvent.Arrow event) {
        RayTraceResult rayTraceResult = event.getRayTraceResult();
        Entity entityHit = rayTraceResult.entityHit;
        if (entityHit == null) {
            return;
        }
        if (entityHit instanceof PlaneEntity || entityHit instanceof EntityPlayer && entityHit.getLowestRidingEntity() instanceof PlaneEntity) {
            EntityArrow arrow = (EntityArrow) event.getEntity();
            if (arrow.getEntityWorld().isRemote || ((arrow.shootingEntity != null) && entityHit.isRidingSameEntity(arrow.shootingEntity))) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onImpact(ProjectileImpactEvent.Fireball event) {
        RayTraceResult rayTraceResult = event.getRayTraceResult();
        Entity entityHit = rayTraceResult.entityHit;
        if (entityHit == null) {
            return;
        }
        if (entityHit instanceof PlaneEntity || entityHit instanceof EntityPlayer && entityHit.getLowestRidingEntity() instanceof PlaneEntity) {
            EntityFireball arrow = (EntityFireball) event.getEntity();
            if (arrow.getEntityWorld().isRemote || ((arrow.shootingEntity != null) && entityHit.isRidingSameEntity(arrow.shootingEntity))) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onImpact(ProjectileImpactEvent.Throwable event) {
        RayTraceResult rayTraceResult = event.getRayTraceResult();
        Entity entityHit = rayTraceResult.entityHit;
        if (entityHit == null) {
            return;
        }
        if (entityHit instanceof PlaneEntity || entityHit instanceof EntityPlayer && entityHit.getLowestRidingEntity() instanceof PlaneEntity) {
            EntityThrowable arrow = (EntityThrowable) event.getEntity();
            if (arrow.getEntityWorld().isRemote || ((arrow.getThrower() != null) && entityHit.isRidingSameEntity(arrow.getThrower()))) {
                event.setCanceled(true);
            }
        }
    }
}