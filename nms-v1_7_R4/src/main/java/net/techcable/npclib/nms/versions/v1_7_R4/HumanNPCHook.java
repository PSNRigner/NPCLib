package net.techcable.npclib.nms.versions.v1_7_R4;

import java.lang.reflect.Field;
import java.util.UUID;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.techcable.npclib.HumanNPC;
import net.techcable.npclib.nms.IHumanNPCHook;
import net.techcable.npclib.nms.versions.v1_7_R4.entity.EntityNPCPlayer;
import net.techcable.npclib.utils.Reflection;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

public class HumanNPCHook extends LivingNPCHook implements IHumanNPCHook {

    public HumanNPCHook(HumanNPC npc, Location toSpawn) {
        super(npc, toSpawn, EntityType.PLAYER);
        getNmsEntity().setHook(this);
    }

    @Override
    public void setSkin(UUID id) {
        NMS.setSkin(getNmsEntity().getProfile(), id);
        respawn();
    }

    private boolean shownInTabList;

    @Override
    public void showInTablist() {
        if (shownInTabList) return;
        if (ProtocolHack.isProtocolHack()) {
            Packet packet18 = ProtocolHack.newPlayerInfoDataAdd(getNmsEntity());
            NMS.sendToAll(packet18);
        } else {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(getNmsEntity().getProfile().getName(), true, 0);
            NMS.sendToAll(packet);
        }
        shownInTabList = true;
    }

    @Override
    public void hideFromTablist() {
        if (!shownInTabList) return;
        if (ProtocolHack.isProtocolHack()) {
            Packet packet18 = ProtocolHack.newPlayerInfoDataRemove(getNmsEntity());
            NMS.sendToAll(packet18);
        } else {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(getNmsEntity().getProfile().getName(), false, 0);
            NMS.sendToAll(packet);
        }
        shownInTabList = false;
    }

    public EntityNPCPlayer getNmsEntity() {
        return (EntityNPCPlayer) super.getNmsEntity();
    }

    @Override
    public HumanNPC getNpc() {
        return (HumanNPC) super.getNpc();
    }

    private static final Field nameField = Reflection.makeField(GameProfile.class, "name");
    private static final Field modifiersField = Reflection.makeField(Field.class, "modifiers");

    @Override
    public void setName(String s) {
        respawn();
    }

    public void respawn() {
        Location lastLocation = getEntity().getLocation();
        boolean wasShown = shownInTabList;
        hideFromTablist();
        getNmsEntity().setHook(null);
        getNmsEntity().dead = true; // Kill old entity
        this.nmsEntity = spawn(lastLocation, EntityType.PLAYER);
        getNmsEntity().setHook(this);
        showInTablist();
        if (!wasShown) hideFromTablist();
    }

    @Override
    public void onDespawn() {
        hideFromTablist();
        super.onDespawn();
    }

    @Override
    protected EntityNPCPlayer spawn(Location toSpawn, EntityType type) {
        Preconditions.checkArgument(type == EntityType.PLAYER, "HumanNPCHook can only handle players");
        EntityNPCPlayer entity = new EntityNPCPlayer(getNpc(), toSpawn);
        this.nmsEntity = entity;
        showInTablist();
        this.nmsEntity = null;
        return entity;
    }

    public void onJoin(Player joined) {

        if (ProtocolHack.isProtocolHack()) {
            Packet packet18 = ProtocolHack.newPlayerInfoDataAdd(getNmsEntity());
            NMS.getHandle(joined).playerConnection.sendPacket(packet18);
        } else {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(getNmsEntity().getProfile().getName(), true, 0);
            NMS.getHandle(joined).playerConnection.sendPacket(packet);
        }
        if (!shownInTabList) {
            if (ProtocolHack.isProtocolHack()) {
                Packet removePacket18 = ProtocolHack.newPlayerInfoDataRemove(getNmsEntity());
                NMS.getHandle(joined).playerConnection.sendPacket(removePacket18);
            } else {
                PacketPlayOutPlayerInfo removePacket = new PacketPlayOutPlayerInfo(getNmsEntity().getProfile().getName(), false, 0);
                NMS.getHandle(joined).playerConnection.sendPacket(removePacket);
            }
        }
    }
}
