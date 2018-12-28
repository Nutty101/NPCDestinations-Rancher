package net.livecar.nuttyworks.destinations_rancher.storage;

import org.bukkit.entity.Entity;

import net.citizensnpcs.api.npc.NPC;

public class MonitoredEntity {

    public NPC actionBy;
    public Entity relatedEntity;
    public ACTION entityAction;

    public enum ACTION
    {
        KILL,KILL_STORE,BREED,
    }
}