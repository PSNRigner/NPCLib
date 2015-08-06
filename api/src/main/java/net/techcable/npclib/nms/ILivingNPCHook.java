package net.techcable.npclib.nms;

import net.techcable.npclib.Animation;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface ILivingNPCHook extends INPCHook {

    public LivingEntity getEntity();

    public void look(float pitch, float yaw);

    public void onTick();

    public void setName(String s);

    public void navigateTo(Location l);

    public void animate(Animation animation); // NOTE -- API performs validation
}
