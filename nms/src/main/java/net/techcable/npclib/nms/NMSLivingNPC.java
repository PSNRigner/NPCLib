package net.techcable.npclib.nms;

import java.util.UUID;

import net.techcable.npclib.LivingNPC;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import com.google.common.base.Preconditions;

public abstract class NMSLivingNPC<T extends ILivingNPCHook> extends NMSNPC<T> implements LivingNPC {

    public NMSLivingNPC(NMSRegistry registry, UUID id, String name) {
        super(registry, id, name);
    }

    @Override
    public void setName(String s) {
        getHook().setName(s);
    }

    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) super.getEntity();
    }

    @Override
    public void walkTo(Location l) {
        Preconditions.checkNotNull(l, "Location can't be null");
        Preconditions.checkArgument(l.getWorld().equals(getEntity().getWorld()), "Can't walk to a location in a different world");
        getHook().navigateTo(l);
    }

    @Override
    public boolean isAbleToWalk() {
        return isSpawned();
    }

    @Override
    public void faceLocation(Location toLook) {
        if (!getEntity().getWorld().equals(toLook.getWorld())) return;
        Location fromLocation = getEntity().getLocation();
        double xDiff, yDiff, zDiff;
        xDiff = toLook.getX() - fromLocation.getX();
        yDiff = toLook.getY() - fromLocation.getY();
        zDiff = toLook.getZ() - fromLocation.getZ();

        double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

        double yaw = Math.toDegrees(Math.acos(xDiff / distanceXZ));
        double pitch = Math.toDegrees(Math.acos(yDiff / distanceY)) - 90;
        if (zDiff < 0.0) yaw += Math.abs(180 - yaw) * 2;

        getHook().look((float) pitch, (float) yaw - 90);
    }

    @Override
    public void run() {
        super.run();
        getHook().onTick();
    }
}
