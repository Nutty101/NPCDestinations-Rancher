package net.livecar.nuttyworks.destinations_rancher.storage;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import org.bukkit.Location;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class NPCSetting {

    public int npcID;
    public HashMap<UUID, LocationSetting> locations;
    public Location currentDestination;
    public CurrentAction currentAction = CurrentAction.IDLE;
    public NPCDestinationsTrait destinationsTrait;
    public Date lastAction;
    public UUID lastInteraction;

    public NPCSetting()
    {
        locations = new HashMap<UUID, LocationSetting>();
        lastAction = new Date();
        lastInteraction = new UUID(0L, 0L);
    }
    public void setNPC(Integer npcid)
    {
        this.npcID = npcid;
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcid);
        destinationsTrait = npc.getTrait(NPCDestinationsTrait.class);
        locations = new HashMap<UUID, LocationSetting>();
    }
    public Integer getNPCID()
    {
        return npcID;
    }

    public enum CurrentAction
    {
        IDLE,
        TRAVERSING,
        BREEDING,
        ABORTING,
    }
}