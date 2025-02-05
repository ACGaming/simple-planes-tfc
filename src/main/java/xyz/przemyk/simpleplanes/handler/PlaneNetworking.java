package xyz.przemyk.simpleplanes.handler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import xyz.przemyk.simpleplanes.entities.PlaneEntity;
import xyz.przemyk.simpleplanes.math.MathUtil;
import xyz.przemyk.simpleplanes.math.MathUtil.EulerAngles;
import xyz.przemyk.simpleplanes.math.Quaternion;
import xyz.przemyk.simpleplanes.setup.SimplePlanesUpgrades;
import xyz.przemyk.simpleplanes.upgrades.Upgrade;
import xyz.przemyk.simpleplanes.upgrades.storage.ChestUpgrade;

import static xyz.przemyk.simpleplanes.SimplePlanesMod.MODID;

public class PlaneNetworking {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
    private static final String PROTOCOL_VERSION = "3";
    private static int messageID = 1;

    public static void init() {
        INSTANCE.registerMessage(
                QuaternionMSG.class, QuaternionMSG.MSG.class, messageID++, Side.SERVER
        );
        INSTANCE.registerMessage(
                BoostMSG.class, BoostMSG.MSG.class, messageID++, Side.SERVER
        );
        INSTANCE.registerMessage(
                InventoryMSG.class, InventoryMSG.MSG.class, messageID++, Side.SERVER
        );
    }

    public static class QuaternionMSG implements IMessageHandler<QuaternionMSG.MSG, IMessage> {
        @Override
        public IMessage onMessage(MSG msg, MessageContext ctx) {
            EntityPlayer ServerEntityPlayer = ctx.getServerHandler().player;
            if (ServerEntityPlayer != null && ServerEntityPlayer.getRidingEntity() instanceof PlaneEntity) {
                PlaneEntity planeEntity = (PlaneEntity) ServerEntityPlayer.getRidingEntity();
                planeEntity.setQ(msg.q);
                EulerAngles eulerAngles = MathUtil.toEulerAngles(msg.q);
                planeEntity.rotationYaw = (float) eulerAngles.yaw;
                planeEntity.rotationPitch = (float) eulerAngles.pitch;
                planeEntity.rotationRoll = (float) eulerAngles.roll;
                planeEntity.setQ_Client(msg.q);
            }
            return null;
        }

        public static class MSG implements IMessage {
            Quaternion q;

            public MSG() {
                this(new Quaternion());
            }

            public MSG(Quaternion q) {
                this.q = q;
            }

            @Override
            public void fromBytes(ByteBuf buf) {
                try {
                    q = new Quaternion(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
                } catch (IndexOutOfBoundsException e) {
                    throw new RuntimeException("packet buffer does not contain enough data to construct plane's Quaternion", e);
                }
            }

            @Override
            public void toBytes(ByteBuf buf) {
                buf.writeFloat(q.getX());
                buf.writeFloat(q.getY());
                buf.writeFloat(q.getZ());
                buf.writeFloat(q.getW());
            }
        }
    }

    public static class BoostMSG implements IMessageHandler<BoostMSG.MSG, IMessage> {
        @Override
        public IMessage onMessage(MSG msg, MessageContext ctx) {
            EntityPlayer ServerEntityPlayer = ctx.getServerHandler().player;
            if (ServerEntityPlayer != null && ServerEntityPlayer.getRidingEntity() instanceof PlaneEntity) {
                PlaneEntity planeEntity = (PlaneEntity) ServerEntityPlayer.getRidingEntity();
                planeEntity.setSprinting(msg.boost);
            }
            return null;
        }

        public static class MSG implements IMessage {
            Boolean boost;

            public MSG() {
                this(false);
            }

            public MSG(boolean boost) {
                this.boost = boost;
            }

            @Override
            public void fromBytes(ByteBuf buf) {
                try {
                    boost = buf.readBoolean();
                } catch (IndexOutOfBoundsException e) {
                    throw new RuntimeException("packet buffer does not contain enough data to construct plane's Quaternion", e);
                }
            }

            @Override
            public void toBytes(ByteBuf buf) {
                buf.writeBoolean(boost);
            }
        }
    }

    public static class InventoryMSG implements IMessageHandler<InventoryMSG.MSG, IMessage> {
        @Override
        public IMessage onMessage(MSG msg, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (player != null && player.getRidingEntity() instanceof PlaneEntity) {
                PlaneEntity planeEntity = (PlaneEntity) player.getRidingEntity();
                if (player.getRidingEntity() instanceof PlaneEntity) {
                    final PlaneEntity plane = (PlaneEntity) player.getRidingEntity();
                    Upgrade chest = plane.upgrades.getOrDefault(SimplePlanesUpgrades.CHEST.getId(), null);
                    if (chest instanceof ChestUpgrade) {
                        ChestUpgrade chest1 = (ChestUpgrade) chest;
                        if (chest1.inventory != null) {
                            //todo
                            if (player.openContainer != player.inventoryContainer) {
                                player.closeScreen();
                            }
                            player.getNextWindowId();
                            player.connection.sendPacket(new SPacketOpenWindow(player.currentWindowId, chest1.getGuiID(), chest1.getDisplayName(), chest1.inventory.getSizeInventory(), planeEntity.getEntityId()));
                            player.openContainer = chest1.createContainer(player.inventory, player);
                            player.openContainer.windowId = player.currentWindowId;
                            player.openContainer.addListener(player);
                        }
                    }
                }
            }
            return null;
        }

        public static class MSG implements IMessage {
            Boolean inv;

            public MSG() {
                this(true);
            }

            public MSG(boolean b) {
                inv = b;
            }

            @Override
            public void fromBytes(ByteBuf buf) {
                try {
                    inv = buf.readBoolean();
                } catch (IndexOutOfBoundsException e) {
                    throw new RuntimeException("packet buffer does not contain enough data to construct plane's Quaternion", e);
                }
            }

            @Override
            public void toBytes(ByteBuf buf) {
                buf.writeBoolean(inv);
            }
        }
    }
}