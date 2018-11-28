package net.livecar.nuttyworks.destinations_rancher.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.EntityType;

public class LocationSetting
{
    public UUID locationID;
    public String regionName;
    public int maxAdults = 0;
    public int maxBabies = 0;
    public List<EntityType> enabledAnimals;
    public List<ActionType> enabledActions;


    public LocationSetting()
    {
        this.regionName = "";
        this.maxAdults = 0;
        this.maxBabies = 0;
        this.enabledAnimals = new ArrayList<EntityType>();
        this.enabledActions = new ArrayList<ActionType>();

    }
}
