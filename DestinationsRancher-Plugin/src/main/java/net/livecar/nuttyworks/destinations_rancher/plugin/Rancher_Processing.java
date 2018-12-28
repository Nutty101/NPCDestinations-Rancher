package net.livecar.nuttyworks.destinations_rancher.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

import net.livecar.nuttyworks.destinations_rancher.Destinations_Rancher;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.destinations_rancher.animals.RanchAnimal;
import net.livecar.nuttyworks.destinations_rancher.storage.ActionType;
import net.livecar.nuttyworks.destinations_rancher.storage.LocationSetting;
import net.livecar.nuttyworks.destinations_rancher.storage.MonitoredEntity;
import net.livecar.nuttyworks.destinations_rancher.storage.MonitoredEntity.ACTION;
import net.livecar.nuttyworks.destinations_rancher.storage.NPCSetting;
import net.livecar.nuttyworks.destinations_rancher.storage.NPCSetting.CurrentAction;
import net.livecar.nuttyworks.destinations_rancher.storage.ValidActionResults;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;

public class Rancher_Processing {

    public void pluginTick() {

        if (Destinations_Rancher.Instance.breedingHistory != null) {
            Iterator<Entry<UUID, Date>> breedingIterator = Destinations_Rancher.Instance.breedingHistory.entrySet().iterator();
            while (breedingIterator.hasNext()) {
                Entry<UUID, Date> item = breedingIterator.next();
                if ((item.getValue().getTime() + 2000) < new Date().getTime()) {
                    breedingIterator.remove();
                }
            }
        }

        if (Destinations_Rancher.Instance.milkingHistory != null) {
            Iterator<Entry<UUID, Date>> milkingIterator = Destinations_Rancher.Instance.milkingHistory.entrySet().iterator();
            while (milkingIterator.hasNext()) {
                Entry<UUID, Date> item = milkingIterator.next();
                if ((item.getValue().getTime() + 2000) < new Date().getTime()) {
                    milkingIterator.remove();
                }
            }
        }

        if (Destinations_Rancher.Instance.monitoredNPCs == null)
            return;

        for (Entry<Integer, LocationSetting> entry : Destinations_Rancher.Instance.monitoredNPCs.entrySet()) {

            if (entry.getValue().locationID == null)
                continue;

            NPC npc = CitizensAPI.getNPCRegistry().getById(entry.getKey());
            if (!npc.isSpawned())
                continue;

            if (npc.getNavigator().isNavigating())
                return;

            NPCSetting ranchSet = Destinations_Rancher.Instance.npcSettings.get(entry.getKey());
            NPCDestinationsTrait npcTrait = npc.getTrait(NPCDestinationsTrait.class);

            if (Destinations_Rancher.Instance.getDestinationsPlugin != null)
                Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINE, "Rancher_Processing.pluginTick|NPC:" + npc.getId() + "|Current Action|" + ranchSet.currentAction.toString());

            switch (ranchSet.currentAction) {
            case ABORTING:
                if (ranchSet.destinationsTrait != null && ranchSet.destinationsTrait.currentLocation != null && ranchSet.destinationsTrait.currentLocation.destination.distanceSquared(npc.getEntity().getLocation()) > 3) {
                    if (Destinations_Rancher.Instance.getDestinationsPlugin != null)
                        Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINEST, "Rancher_Processing.pluginTick|NPC:" + npc.getId() + "|Not at destination|"
                                + ranchSet.destinationsTrait.currentLocation.destination + " Dist:" + npc.getEntity().getLocation().add(0.0D, -1.0D, 0.0D).distanceSquared(ranchSet.destinationsTrait.currentLocation.destination));

                    DestinationsPlugin.Instance.getPathClass.addToQueue(npc, ranchSet.destinationsTrait, npc.getEntity().getLocation().add(0.0D, -1.0D, 0.0D), ranchSet.destinationsTrait.currentLocation.destination, 120,
                            new ArrayList<Material>(), 0, true, true, true, "Destinations_Rancher");
                } else {
                    /*
                     * if (Destinations_Rancher.Instance.getDestinationsPlugin
                     * != null)
                     * Destinations_Rancher.Instance.getDestinationsPlugin.
                     * getMessageManager.debugMessage(Level.FINEST,
                     * "Rancher_Processing.pluginTick|NPC:" + npc.getId() +
                     * "|ABORTED|Removing monitors");
                     * 
                     * Destinations_Rancher.Instance.npcSettings.get(npc.getId()
                     * ).currentAction = CurrentAction.IDLE;
                     * Destinations_Rancher.Instance.npcSettings.get(npc.getId()
                     * ).currentDestination = null;
                     * Destinations_Rancher.Instance.monitoredNPCs.remove(npc.
                     * getId());
                     * ranchSet.destinationsTrait.unsetMonitoringPlugin(
                     * "Aborted"); continue;
                     */
                }

                break;
            case TRAVERSING:
                if (ranchSet.currentDestination != null && !npc.getNavigator().isNavigating() && ranchSet.currentDestination.distanceSquared(npc.getEntity().getLocation()) < 4) {
                    // No actions can be done.
                    ranchSet.currentDestination = null;
                    ranchSet.currentAction = CurrentAction.IDLE;
                    ranchSet.lastAction = new Date();
                } else if (ranchSet.lastAction.getTime() < (new Date().getTime() - 5000)) {
                    // Timeout..
                    ranchSet.currentDestination = null;
                    ranchSet.currentAction = CurrentAction.IDLE;
                    ranchSet.lastAction = new Date();
                    if (Destinations_Rancher.Instance.getDestinationsPlugin != null)
                        Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINEST, "Rancher_Processing.pluginTick|NPC:" + npc.getId() + "|Activity Timeout");
                }
                break;
            case IDLE:
            default:

                LocationSetting rancherLocation = entry.getValue();
                ValidActionResults results = null;

                if (!rancherLocation.regionName.equals("")) {

                    if (!Destinations_Rancher.Instance.getDestinationsPlugin.getWorldGuardPlugin.isInRegion(npc.getEntity().getLocation(), rancherLocation.regionName)) {
                        Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "NPC:" + npc.getId() + "|New Location, returning to destination.");
                        if (Destinations_Rancher.Instance.getDestinationsPlugin != null)
                            Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINEST, "NPC:" + npc.getId() + "|Not at destination|"
                                    + ranchSet.destinationsTrait.currentLocation.destination + " Dist:" + npc.getEntity().getLocation().distanceSquared(ranchSet.destinationsTrait.currentLocation.destination));

                        DestinationsPlugin.Instance.getPathClass.addToQueue(npc, ranchSet.destinationsTrait, npc.getEntity().getLocation(), ranchSet.destinationsTrait.currentLocation.destination, 120,
                                new ArrayList<Material>(), 0, true, true, true, "Destinations_Rancher");
                        ranchSet.lastAction = new Date();
                        ((NPCSetting) Destinations_Rancher.Instance.npcSettings.get(npc.getId())).currentAction = NPCSetting.CurrentAction.TRAVERSING;
                        return;
                    }

                    for (EntityType workingAnimal : rancherLocation.enabledAnimals) {

                        results = Destinations_Rancher.Instance.getProcessingClass.getValidActions(npc, rancherLocation, ActionType.SLAUGHTER, ranchSet.lastInteraction, workingAnimal);
                        if (results.closest != null) {
                            if (rancherLocation.enabledActions.contains(ActionType.SLAUGHTER) && results.validAnimals.size() > rancherLocation.maxAdults) {
                                if (results.distance < 5) {
                                    if (Destinations_Rancher.Instance.getPluginReference.monitorDrops.containsKey(results.closest.getEntity().getUniqueId()))
                                        Destinations_Rancher.Instance.getPluginReference.monitorDrops.remove(results.closest.getEntity().getUniqueId());

                                    if (Destinations_Rancher.Instance.getDestinationsPlugin.debugTargets != null) {
                                        Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.sendDebugMessage("rancher", "Debug_Messages.killing_animal", npc, npcTrait);
                                    }

                                    MonitoredEntity monEnt = new MonitoredEntity();
                                    monEnt.actionBy = npc;
                                    monEnt.entityAction = ACTION.KILL_STORE;
                                    monEnt.relatedEntity = results.closest.getEntity();
                                    Destinations_Rancher.Instance.getPluginReference.monitorDrops.put(results.closest.getEntity().getUniqueId(), monEnt);
                                    ranchSet.lastInteraction = results.closest.getEntity().getUniqueId();
                                    results.closest.damageAnimal(99999);

                                    continue;
                                } else if (walkToLocation(npc, results.closest.getEntity().getLocation(), ranchSet)) {
                                    if (Destinations_Rancher.Instance.getDestinationsPlugin.debugTargets != null) {
                                        Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.sendDebugMessage("rancher", "Debug_Messages.walking_animal", npc, npcTrait);
                                    }
                                    continue;
                                }
                            }
                        }

                        results = Destinations_Rancher.Instance.getProcessingClass.getValidActions(npc, rancherLocation, ActionType.BREED, ranchSet.lastInteraction, workingAnimal);
                        if (results.closest != null) {
                            if (Destinations_Rancher.Instance.getProcessingClass.getBabyCount(npc, rancherLocation, workingAnimal) < rancherLocation.maxBabies && rancherLocation.enabledActions.contains(ActionType.BREED)
                                    && results.validAnimals.size() > 0) {
                                if (results.distance < 7) {
                                    results.closest.setTamed(true);
                                    results.closest.setInLove();
                                    for (Player plr : Bukkit.getServer().getOnlinePlayers()) {
                                        if (plr.getLocation().getWorld().equals(results.closest.getEntity().getWorld())) {
                                            if (plr.getLocation().distanceSquared(results.closest.getEntity().getLocation()) < 30 * 30) {
                                                Destinations_Rancher.Instance.getDestinationsPlugin.getParticleManager.PlayOutHeartParticle(results.closest.getEntity().getLocation().clone().add(0, 1, 0), plr);
                                                Destinations_Rancher.Instance.getDestinationsPlugin.getParticleManager.PlayOutHeartParticle(results.closest.getEntity().getLocation().clone().add(0.2, 0.5, -0.2), plr);
                                            }
                                        }
                                    }

                                    Destinations_Rancher.Instance.breedingHistory.put(results.closest.getEntity().getUniqueId(), new Date());
                                    continue;
                                } else if (walkToLocation(npc, results.closest.getEntity().getLocation(), ranchSet)) {
                                    if (Destinations_Rancher.Instance.getDestinationsPlugin.debugTargets != null) {
                                        Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.sendDebugMessage("rancher", "Debug_Messages.walking_animal", npc, npcTrait);
                                    }
                                    continue;
                                }
                            }
                        }

                        results = Destinations_Rancher.Instance.getProcessingClass.getValidActions(npc, rancherLocation, ActionType.MILK, ranchSet.lastInteraction, workingAnimal);
                        if (results.closest != null) {
                            if (rancherLocation.enabledActions.contains(ActionType.MILK) && results.validAnimals.size() > 0) {
                                if (results.distance < 5) {
                                    if (Destinations_Rancher.Instance.getPluginReference.monitorDrops.containsKey(results.closest.getEntity().getUniqueId()))
                                        Destinations_Rancher.Instance.getPluginReference.monitorDrops.remove(results.closest.getEntity().getUniqueId());

                                    if (Destinations_Rancher.Instance.getDestinationsPlugin.debugTargets != null) {
                                        Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.sendDebugMessage("rancher", "Debug_Messages.killing_animal", npc, npcTrait);
                                    }

                                    if (Destinations_Rancher.Instance.milkingHistory.containsKey(results.closest.getEntity().getUniqueId()))
                                        Destinations_Rancher.Instance.milkingHistory.remove(results.closest.getEntity().getUniqueId());

                                    Destinations_Rancher.Instance.milkingHistory.put(results.closest.getEntity().getUniqueId(), new Date());
                                    ranchSet.lastInteraction = results.closest.getEntity().getUniqueId();

                                    ItemStack milk = new ItemStack(Material.MILK_BUCKET, (int) 1);
                                    Destinations_Rancher.Instance.addToInventory(npc, new ItemStack[] { milk });
                                    Destinations_Rancher.Instance.milkingHistory.put(results.closest.getEntity().getUniqueId(), new Date());
                                    continue;
                                } else if (walkToLocation(npc, results.closest.getEntity().getLocation(), ranchSet)) {
                                    if (Destinations_Rancher.Instance.getDestinationsPlugin.debugTargets != null) {
                                        Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.sendDebugMessage("rancher", "Debug_Messages.walking_animal", npc, npcTrait);
                                    }
                                    continue;
                                }
                            }
                        }

                        results = Destinations_Rancher.Instance.getProcessingClass.getValidActions(npc, rancherLocation, ActionType.SHEAR, ranchSet.lastInteraction, workingAnimal);
                        if (results.closest != null) {
                            if (rancherLocation.enabledActions.contains(ActionType.SHEAR) && results.validAnimals.size() > 0) {
                                if (results.distance < 5) {
                                    ranchSet.lastInteraction = results.closest.getEntity().getUniqueId();
                                    if (rancherLocation.enabledActions.contains(ActionType.STORE_ITEMS)) {
                                        ItemStack wool = new ItemStack(Destinations_Rancher.Instance.getAnimalHelper.getWool(), (int) (Math.random() * 3) + 1);
                                        wool.setData(new Wool(results.closest.getColor()));
                                        Destinations_Rancher.Instance.addToInventory(npc, new ItemStack[] { wool });
                                    }
                                    results.closest.shearWool();
                                    continue;
                                } else if (walkToLocation(npc, results.closest.getEntity().getLocation(), ranchSet)) {
                                    if (Destinations_Rancher.Instance.getDestinationsPlugin.debugTargets != null) {
                                        Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.sendDebugMessage("rancher", "Debug_Messages.walking_animal", npc, npcTrait);
                                    }
                                    continue;
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public int getBabyCount(NPC npc, LocationSetting locSetting, EntityType targetAnimals) {
        int babies = 0;

        for (RanchAnimal animal : this.regionGetAnimals(npc.getEntity().getLocation().getWorld(), locSetting.regionName, targetAnimals)) {
            if (!animal.isAdult())
                babies++;
        }

        return babies;
    }

    public ValidActionResults getValidActions(NPC npc, LocationSetting locSetting, ActionType action, UUID lastInteraction, EntityType targetAnimals) {

        ValidActionResults validResults = new ValidActionResults();
        for (RanchAnimal animal : this.regionGetAnimals(npc.getEntity().getLocation().getWorld(), locSetting.regionName, targetAnimals)) {
            if (!animal.isAdult())
                continue;

            switch (action) {
            case SLAUGHTER:
                if (animal.getHealth() > 0 && !Destinations_Rancher.Instance.breedingHistory.containsKey(animal.getEntity().getUniqueId())) {
                    validResults.validAnimals.add(animal);
                    if (animal.getEntity().getLocation().distanceSquared(npc.getEntity().getLocation()) < validResults.distance && animal.getAge() > (validResults.closest == null ? 0 : validResults.closest.getAge())
                            && lastInteraction != animal.getEntity().getUniqueId()) {
                        validResults.closest = animal;
                        validResults.distance = animal.getEntity().getLocation().distanceSquared(npc.getEntity().getLocation());
                    }
                }
                break;
            case MILK:
                if ((animal.getEntity().getType() == EntityType.COW && Destinations_Rancher.Instance.itemInHand(locSetting, npc, Material.BUCKET)) || (animal.getEntity().getType() == EntityType.MUSHROOM_COW && Destinations_Rancher.Instance
                        .itemInHand(locSetting, npc, Material.BOWL))) {
                    if (!Destinations_Rancher.Instance.milkingHistory.containsKey(animal.getEntity().getUniqueId())) {

                        if (!animal.isSheered()) {
                            validResults.validAnimals.add(animal);

                            if (animal.getEntity().getLocation().distanceSquared(npc.getEntity().getLocation()) < validResults.distance && lastInteraction != animal.getEntity().getUniqueId()) {
                                validResults.closest = animal;
                                validResults.distance = animal.getEntity().getLocation().distanceSquared(npc.getEntity().getLocation());
                            }
                        }
                    }
                }
                break;
            case SHEAR:
                if (animal.getEntity().getType() == EntityType.SHEEP && Destinations_Rancher.Instance.itemInHand(locSetting, npc, Material.SHEARS)) {
                    if (!animal.isSheered()) {
                        validResults.validAnimals.add(animal);

                        if (animal.getEntity().getLocation().distanceSquared(npc.getEntity().getLocation()) < validResults.distance && lastInteraction != animal.getEntity().getUniqueId()) {
                            validResults.closest = animal;
                            validResults.distance = animal.getEntity().getLocation().distanceSquared(npc.getEntity().getLocation());
                        }
                    }
                }
                break;
            case BREED:
                // Validate if the animal was set to inlove recently.
                if (Destinations_Rancher.Instance.breedingHistory.containsKey(animal.getEntity().getUniqueId()))
                    continue;

                if (animal.canBreed() && !Destinations_Rancher.Instance.breedingHistory.containsKey(animal.getEntity().getUniqueId())) {
                    boolean possibleMate = false;

                    for (RanchAnimal testForAnimal : this.regionGetAnimals(npc.getEntity().getLocation().getWorld(), locSetting.regionName, targetAnimals)) {
                        if (testForAnimal.getEntity().getUniqueId() == animal.getEntity().getUniqueId())
                            continue;

                        if (!testForAnimal.isAdult())
                            continue;

                        if (testForAnimal.getEntity().getType().equals(animal.getEntity().getType())) {
                            if (testForAnimal.getEntity().getLocation().distanceSquared(animal.getEntity().getLocation()) < 12) {
                                possibleMate = true;
                            }
                        }
                    }

                    if (possibleMate) {
                        validResults.validAnimals.add(animal);
                        if (animal.getEntity().getLocation().distanceSquared(npc.getEntity().getLocation()) < validResults.distance && lastInteraction != animal.getEntity().getUniqueId()) {
                            validResults.closest = animal;
                            validResults.distance = animal.getEntity().getLocation().distanceSquared(npc.getEntity().getLocation());
                        }
                    }
                }
                break;
            default:
                break;
            }
        }
        return validResults;

    }

    private boolean walkToLocation(NPC npc, Location newLocation, NPCSetting ranchSet) {
        if (newLocation == null)
            return false;

        Location walkToLocation = newLocation;

        /*
         * if (newLocation.distanceSquared(npc.getEntity().getLocation()) > 2) {
         * // walkToLocation = findWalkableNextTo(npc, newLocation); if
         * (walkToLocation == null) return false; }
         */

        if (DestinationsPlugin.Instance.getPathClass.path_Queue.containsKey(npc.getId()))
            return false;

        ranchSet.currentAction = CurrentAction.TRAVERSING;
        if (newLocation.getBlock().getType() == Material.AIR)
            newLocation.add(0, -1, 0);
        ranchSet.currentDestination = newLocation;
        ranchSet.lastAction = new Date();
        if (Destinations_Rancher.Instance.getDestinationsPlugin != null)
            Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINEST, "NPC:" + npc.getId() + "|NewLocation|" + newLocation + "Walkto: " + walkToLocation + " Dist:" + npc
                    .getEntity().getLocation().distanceSquared(walkToLocation) + " Block:" + newLocation.getBlock().getType().toString());

        net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), walkToLocation);
        if (npc.getEntity().getLocation().distanceSquared(walkToLocation) < 300)
            npc.getNavigator().setTarget(walkToLocation);
        else
            DestinationsPlugin.Instance.getPathClass.addToQueue(npc, ranchSet.destinationsTrait, npc.getEntity().getLocation(), walkToLocation, 120, new ArrayList<Material>(), 0, true, true, true, "Destinations_Rancher");
        
        return true;
    }

    public Location findWalkableNextTo(NPC npc, Location blockLocation) {
        float yaw = blockLocation.setDirection(npc.getEntity().getLocation().toVector().subtract(blockLocation.toVector())).getYaw();

        // North: -Z
        // East: +X
        // South: +Z
        // West: -X
        int xAxis = 0;
        int zAxis = 0;

        if (0 <= yaw && yaw < 22.5) {
            xAxis = 0;
            zAxis = -1;
        } else if (22.5 <= yaw && yaw < 67.5) {
            xAxis = 1;
            zAxis = -1;
        } else if (67.5 <= yaw && yaw < 112.5) {
            xAxis = 1;
            zAxis = 0;
        } else if (112.5 <= yaw && yaw < 157.5) {
            xAxis = 1;
            zAxis = 1;
        } else if (157.5 <= yaw && yaw < 202.5) {
            xAxis = 0;
            zAxis = 1;
        } else if (202.5 <= yaw && yaw < 247.5) {
            xAxis = -1;
            zAxis = 1;
        } else if (247.5 <= yaw && yaw < 292.5) {
            xAxis = -1;
            zAxis = 0;
        } else if (292.5 <= yaw && yaw < 337.5) {
            xAxis = -1;
            zAxis = -1;
        } else if (337.5 <= yaw && yaw < 360.0) {
            xAxis = 0;
            zAxis = -1;
        }

        Location animalLocation = blockLocation.clone().add(xAxis, 0, zAxis);
        if (DestinationsPlugin.Instance.getPathClass.isLocationWalkable(animalLocation.clone())) {
            return animalLocation.clone();
        }

        int counter = 0;
        while (counter < 25) {
            for (byte y = -1; y <= 1; y++) {
                xAxis = (Math.random() * 2 + 1) == 1 ? -1 : 1;
                zAxis = (Math.random() * 2 + 1) == 1 ? -1 : 1;
                if (DestinationsPlugin.Instance.getPathClass.isLocationWalkable(blockLocation.clone().add(xAxis, y, zAxis))) {
                    return blockLocation.clone().add(xAxis, y, zAxis);
                }
            }
            counter++;
        }
        return null;
    }

    public void PlaySound(Location soundLocation, soundType sound) {
        switch (sound) {
        default:
            break;

        }
    }

    public RanchAnimal[] regionGetAnimals(World world, String region, EntityType allowedAnimal) {
        if (DestinationsPlugin.Instance.getWorldGuardPlugin.getRegionList(world).contains(region)) {

            Location[] regionBounds = DestinationsPlugin.Instance.getWorldGuardPlugin.getRegionBounds(world,region);

            List<Integer> checkedChunks = new ArrayList<Integer>();

            List<RanchAnimal> foundAnimals = new ArrayList<RanchAnimal>();

            for (int x = regionBounds[0].getBlockX(); x < regionBounds[1].getBlockX(); ++x) {
                for (int z = regionBounds[0].getBlockZ(); z < regionBounds[1].getBlockZ(); ++z) {

                    Location checkLocation = new Location(world, x, 1, z);
                    if (checkedChunks.contains(checkLocation.getChunk().hashCode()))
                        continue;

                    checkedChunks.add(checkLocation.getChunk().hashCode());

                    if (checkLocation.getChunk().isLoaded()) {

                        for (Entity ent : checkLocation.getChunk().getEntities()) {

                            if (allowedAnimal.equals(ent.getType())) {
                                if (ent.getLocation().getBlockY() >= regionBounds[0].getBlockY() && ent.getLocation().getBlockY() <= regionBounds[1].getBlockY()) {
                                    // test if it's actually in the region
                                    // (Ellipse etc)
                                    if (DestinationsPlugin.Instance.getWorldGuardPlugin.isInRegion(ent.getLocation(),region))
                                    {
                                        foundAnimals.add(Destinations_Rancher.Instance.getAnimalHelper.getRanchAnimal(ent));
                                    }
                                }
                            }

                        }
                    }
                }

            }
            return foundAnimals.toArray(new RanchAnimal[foundAnimals.size()]);
        }
        return new RanchAnimal[0];
    }

    public enum soundType {
        TILL_DIRT, PLANT,
    }
}
