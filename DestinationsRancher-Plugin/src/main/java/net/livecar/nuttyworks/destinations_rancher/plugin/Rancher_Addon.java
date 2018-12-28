package net.livecar.nuttyworks.destinations_rancher.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import net.livecar.nuttyworks.destinations_rancher.Destinations_Rancher;
import net.livecar.nuttyworks.destinations_rancher.storage.ActionType;
import net.livecar.nuttyworks.destinations_rancher.storage.LocationSetting;
import net.livecar.nuttyworks.destinations_rancher.storage.NPCSetting;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;

public class Rancher_Addon extends DestinationsAddon {
    Destinations_Rancher pluginReference = null;

    public Rancher_Addon(Destinations_Rancher instanceRef) {
        pluginReference = instanceRef;
    }

    @Override
    public String getActionName() {
        return "Rancher";
    }

    @Override
    public String getPluginIcon() {
        return "♉";
    }

    @Override
    public String getQuickDescription() {
        String[] response = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("rancher", "messages.plugin_description", "");
        return response[0];
    }

    @Override
    public String getDestinationHelp(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location) {
        String[] response = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("rancher", null, "messages.plugin_destination", npcTrait, location, npc, null, 0);
        return response[0];
    }

    @Override
    public String parseLanguageLine(String message, NPCDestinationsTrait npcTrait, Destination_Setting locationSetting, Material blockMaterial, NPC npc, int ident) {

        if (locationSetting != null) {
            if (pluginReference.npcSettings.containsKey(npc.getId())) {
                if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(locationSetting.LocationIdent)) {

                    if (message.toLowerCase().contains("<rancher.")) {
                        LocationSetting locSetting = pluginReference.npcSettings.get(npc.getId()).locations.get(locationSetting.LocationIdent);

                        if (!locSetting.regionName.equals("")) {
                            message = message.replaceAll("<rancher\\.regionname>", locSetting.regionName);
                        } else {
                            message = message.replaceAll("<rancher\\.regionname>", "Not set");
                        }

                        if (message.toLowerCase().contains("<rancher.maxbabies>"))
                            message = message.replaceAll("<rancher\\.maxbabies>", String.valueOf(locSetting.maxBabies));
                        if (message.toLowerCase().contains("<rancher.maxadults>"))
                            message = message.replaceAll("<rancher\\.maxadults>", String.valueOf(locSetting.maxAdults));
                        if (message.toLowerCase().contains("<rancher.animallist>")) {
                            if (pluginReference.getAnimalHelper.getSupportedAnimals().size() == 0) {
                                message = message.replaceAll("<rancher\\.animallist>", "{\"text\":\"There are no animals available\",\"color\":\"yellow\"}");
                            } else {
                                String[] toggleAnimal_Help = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("rancher", null, "help_messages.toggleanimal", npcTrait, locationSetting, npc, null, 0);
                                String animalList = ""; // "{\"text\":\" \"},"
                                                        // "X\",\"color\":\"red":"✔\",\"color\":\"white");;
                                for (EntityType animalType : pluginReference.getAnimalHelper.getSupportedAnimals()) {
                                    String animalLine = "{\"text\":\"[\",\"color\":\"yellow\"},";
                                    if (locSetting.enabledAnimals.contains(animalType)) {
                                        animalLine += "{\"text\":\"✔\",\"color\":\"green\",";
                                    } else {
                                        animalLine += "{\"text\":\"X\",\"color\":\"red\",";
                                    }
                                    animalLine += "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/npcdest locrancher --npc <npc.id> <location.id> toggleanimal " + animalType.toString()
                                            + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + toggleAnimal_Help[0] + "\"}},";
                                    animalLine += "{\"text\":\"] \",\"color\":\"yellow\"},";
                                    animalLine += "{\"text\":\"" + animalType.toString() + "\",\"color\":\"white\"},{\"text\":\" \",\"color\":\"yellow\"},";
                                    animalList += animalLine;
                                }
                                message = message.replaceAll("<rancher\\.animallist>", animalList + "{\"text\":\" \"}");
                            }
                        }
                        if (message.toLowerCase().contains("<rancher.actions>")) {
                            if (pluginReference.getAnimalHelper.getSupportedAnimals().size() == 0) {
                                message = message.replaceAll("<rancher\\.actions>", "{\"text\":\"There are no actions available\",\"color\":\"yellow\"}");
                            } else {
                                String[] toggleAnimal_Help = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("rancher", null, "help_messages.toggleanimal", npcTrait, locationSetting, npc, null, 0);

                                String actionList = ""; // "{\"text\":\" \"},"
                                                        // "X\",\"color\":\"red":"✔\",\"color\":\"white");;
                                for (ActionType actionType : ActionType.values()) {
                                    String actionLine = "{\"text\":\"[\",\"color\":\"yellow\"},";
                                    if (locSetting.enabledActions.contains(actionType)) {
                                        actionLine += "{\"text\":\"✔\",\"color\":\"green\",";
                                    } else {
                                        actionLine += "{\"text\":\"X\",\"color\":\"red\",";
                                    }
                                    actionLine += "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/npcdest locrancher --npc <npc.id> <location.id> toggleaction " + actionType.toString()
                                            + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + toggleAnimal_Help[0] + "\"}},";
                                    actionLine += "{\"text\":\"] \",\"color\":\"yellow\"},";
                                    actionLine += "{\"text\":\"" + actionType.toString() + "\",\"color\":\"white\"},{\"text\":\" \",\"color\":\"yellow\"},";
                                    actionList += actionLine;
                                }
                                message = message.replaceAll("<rancher\\.actions>", actionList + "{\"text\":\" \"}");
                            }
                        }
                    }
                }
            }
        }

        if (message.toLowerCase().contains("<rancher.regionname>"))
            message = message.replaceAll("<rancher\\.regionname>", "Not Set");
        if (message.toLowerCase().contains("<rancher.maxbabies>"))
            message = message.replaceAll("<rancher\\.maxbabies>", "Not set");
        if (message.toLowerCase().contains("<rancher.maxadults>"))
            message = message.replaceAll("<rancher\\.maxadults>", "Not set");

        if (message.toLowerCase().contains("<rancher.animallist>")) {
            if (pluginReference.getAnimalHelper.getSupportedAnimals().size() == 0) {
                message = message.replaceAll("<rancher\\.animallist>", "{\"text\":\"There are no animals available\",\"color\":\"yellow\"}");
            } else {
                String[] toggleAnimal_Help = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("rancher", null, "help_messages.toggleanimal", npcTrait, locationSetting, npc, null, 0);
                String animalList = ""; // "{\"text\":\" \"},"
                                        // "X\",\"color\":\"red":"✔\",\"color\":\"white");;
                for (EntityType animalType : pluginReference.getAnimalHelper.getSupportedAnimals()) {
                    String animalLine = "{\"text\":\"[\",\"color\":\"yellow\"},";
                    animalLine += "{\"text\":\"X\",\"color\":\"red\",";
                    animalLine += "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/npcdest locrancher --npc <npc.id> <location.id> toggleanimal " + animalType.toString() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\""
                            + toggleAnimal_Help[0] + "\"}},";
                    animalLine += "{\"text\":\"] \",\"color\":\"yellow\"},";
                    animalLine += "{\"text\":\"" + animalType.toString() + "\",\"color\":\"white\"},{\"text\":\" \",\"color\":\"yellow\"},";
                    animalList += animalLine;
                    if (animalType == EntityType.PIG) {
                        animalList += "{\"text\":\"\n\"},";
                    }
                }
                message = message.replaceAll("<rancher\\.animallist>", animalList + "{\"text\":\" \"}");
            }
        }
        if (message.toLowerCase().contains("<rancher.actions>")) {
            if (pluginReference.getAnimalHelper.getSupportedAnimals().size() == 0) {
                message = message.replaceAll("<rancher\\.actions>", "{\"text\":\"There are no actions available\",\"color\":\"yellow\"}");
            } else {
                String[] toggleAnimal_Help = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("rancher", null, "help_messages.toggleanimal", npcTrait, locationSetting, npc, null, 0);

                String actionList = ""; // "{\"text\":\" \"},"
                                        // "X\",\"color\":\"red":"✔\",\"color\":\"white");;
                for (ActionType actionType : ActionType.values()) {
                    String actionLine = "{\"text\":\"[\",\"color\":\"yellow\"},";
                    actionLine += "{\"text\":\"X\",\"color\":\"red\",";
                    actionLine += "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/npcdest locrancher --npc <npc.id> <location.id> toggleaction" + actionType.toString() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\""
                            + toggleAnimal_Help[0] + "\"}},";
                    actionLine += "{\"text\":\"]\",\"color\":\"yellow\"},";
                    actionLine += "{\"text\":\"" + actionType.toString() + "\",\"color\":\"white\"},{\"text\":\" \",\"color\":\"yellow\"},";
                    actionList += actionLine;
                    if (actionType == ActionType.MILK) {
                        actionList += "{\"text\":\"\n\"},";
                    }
                }
                message = message.replaceAll("<rancher\\.actions>", actionList + "{\"text\":\" \"}");
            }
        }

        return message;
    }

    @Override
    public boolean isDestinationEnabled(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location) {
        if (pluginReference.npcSettings.containsKey(npc.getId())) {

            boolean pendingActions = false;

            // Does this NPC have a set location already?
            if (pluginReference.monitoredNPCs.containsKey(npc.getId())) {
                // Is there anything to do at this location?
                LocationSetting rancherLocation = pluginReference.monitoredNPCs.get(npc.getId());

                for (EntityType workingAnimal : rancherLocation.enabledAnimals) {
                    if (pluginReference.getProcessingClass.getBabyCount(npc, rancherLocation, workingAnimal) < rancherLocation.maxBabies && rancherLocation.enabledActions.contains(ActionType.BREED) && pluginReference.getProcessingClass
                            .getValidActions(npc, rancherLocation, ActionType.BREED, new UUID(0L, 0L), workingAnimal).validAnimals.size() > 0) {
                        pendingActions = true;
                    } else if (rancherLocation.enabledActions.contains(ActionType.SLAUGHTER) && pluginReference.getProcessingClass.getValidActions(npc, rancherLocation, ActionType.SLAUGHTER, new UUID(0L, 0L), workingAnimal).validAnimals
                            .size() > rancherLocation.maxAdults) {
                        pendingActions = true;
                    } else if (rancherLocation.enabledActions.contains(ActionType.MILK) && pluginReference.getProcessingClass.getValidActions(npc, rancherLocation, ActionType.MILK, new UUID(0L, 0L), workingAnimal).validAnimals
                            .size() > 0) {
                        pendingActions = true;
                    } else if (rancherLocation.enabledActions.contains(ActionType.SHEAR) && pluginReference.getProcessingClass.getValidActions(npc, rancherLocation, ActionType.SHEAR, new UUID(0L, 0L), workingAnimal).validAnimals
                            .size() > 0) {
                        pendingActions = true;
                    }
                }
                if (rancherLocation.locationID.equals(location.LocationIdent))
                    pendingActions = false;
            }

            if (pendingActions)
                return false;

            if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(location.LocationIdent)) {

                LocationSetting rancherLocation = pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent);

                if (!rancherLocation.regionName.equals("")) {
                    if (rancherLocation.enabledActions.contains(ActionType.NOACTION_BLOCK)) {
                        for (EntityType workingAnimal : rancherLocation.enabledAnimals) {
                            if (pluginReference.getProcessingClass.getBabyCount(npc, rancherLocation, workingAnimal) < rancherLocation.maxBabies && rancherLocation.enabledActions.contains(ActionType.BREED)
                                    && pluginReference.getProcessingClass.getValidActions(npc, rancherLocation, ActionType.BREED, new UUID(0L, 0L), workingAnimal).validAnimals.size() > 0) {
                                return true;
                            }

                            if (rancherLocation.enabledActions.contains(ActionType.SLAUGHTER) && pluginReference.getProcessingClass.getValidActions(npc, rancherLocation, ActionType.SLAUGHTER, new UUID(0L, 0L),
                                    workingAnimal).validAnimals.size() > rancherLocation.maxAdults) {
                                return true;
                            }

                            if (rancherLocation.enabledActions.contains(ActionType.MILK) && pluginReference.getProcessingClass.getValidActions(npc, rancherLocation, ActionType.MILK, new UUID(0L, 0L), workingAnimal).validAnimals
                                    .size() > 0) {
                                return true;
                            }

                            if (rancherLocation.enabledActions.contains(ActionType.SHEAR) && pluginReference.getProcessingClass.getValidActions(npc, rancherLocation, ActionType.SHEAR, new UUID(0L, 0L), workingAnimal).validAnimals
                                    .size() > 0) {
                                return true;
                            }
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onLocationLoading(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location, DataKey storageKey) {
        if (!storageKey.keyExists("ranchers"))
            return;

        NPCSetting npcRancher;
        if (!pluginReference.npcSettings.containsKey(npc.getId())) {
            npcRancher = new NPCSetting();
            npcRancher.setNPC(npc.getId());
            pluginReference.npcSettings.put(npc.getId(), npcRancher);
        } else {
            npcRancher = pluginReference.npcSettings.get(npc.getId());
        }

        LocationSetting locationConfig = new LocationSetting();
        locationConfig.locationID = UUID.fromString(storageKey.getString("ranchers.LocationID", ""));

        if (storageKey.keyExists("ranchers.region"))
            locationConfig.regionName = storageKey.getString("ranchers.region", "");

        if (storageKey.keyExists("ranchers.maxbabies"))
            locationConfig.maxBabies = storageKey.getInt("ranchers.maxbabies", 0);

        if (storageKey.keyExists("ranchers.maxadults"))
            locationConfig.maxAdults = storageKey.getInt("ranchers.maxadults", 0);

        if (storageKey.keyExists("ranchers.enabledanimals")) {
            locationConfig.enabledAnimals.clear();
            List<String> animalTypes = storageKey.getRawUnchecked("ranchers.enabledanimals");
            for (String animalType : animalTypes) {
                if (EnumUtils.isValidEnum(EntityType.class, animalType.toUpperCase())) {
                    locationConfig.enabledAnimals.add(EntityType.valueOf(animalType.toUpperCase()));
                }
            }
        }

        if (storageKey.keyExists("ranchers.enabledactions")) {
            locationConfig.enabledActions.clear();
            List<String> actionTypes = storageKey.getRawUnchecked("ranchers.enabledactions");
            for (String actionType : actionTypes) {
                if (EnumUtils.isValidEnum(ActionType.class, actionType.toUpperCase())) {
                    locationConfig.enabledActions.add(ActionType.valueOf(actionType.toUpperCase()));
                }
            }
        }

        npcRancher.locations.put(location.LocationIdent, locationConfig);
    }

    @Override
    public void onLocationSaving(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location, DataKey storageKey) {
        if (!pluginReference.npcSettings.containsKey(npc.getId()))
            return;
        if (!pluginReference.npcSettings.get(npc.getId()).locations.containsKey(location.LocationIdent))
            return;

        LocationSetting rancherLocation = pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent);

        storageKey.setString("ranchers.LocationID", rancherLocation.locationID.toString());
        storageKey.setString("ranchers.region", rancherLocation.regionName);
        storageKey.setInt("ranchers.maxbabies", rancherLocation.maxBabies);
        storageKey.setInt("ranchers.maxadults", rancherLocation.maxAdults);

        List<String> animalTypes = new ArrayList<String>();
        for (EntityType animalType : rancherLocation.enabledAnimals) {
            animalTypes.add(animalType.name());
        }
        storageKey.setRaw("ranchers.enabledanimals", animalTypes);

        List<String> actionTypes = new ArrayList<String>();
        for (ActionType actionType : rancherLocation.enabledActions) {
            actionTypes.add(actionType.name());
        }
        storageKey.setRaw("ranchers.enabledactions", actionTypes);

    }

    @Override
    public void onEnableChanged(NPC npc, NPCDestinationsTrait trait, boolean enabled) {
        if (enabled) {
            if (pluginReference.npcSettings.containsKey(npc.getId())) {
                if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(trait.currentLocation.LocationIdent)) {
                    if (!pluginReference.monitoredNPCs.containsKey(npc.getId())) {
                        pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationReached|NPC:" + npc.getId() + "|Monitored location reached, assigning as monitor");
                        trait.setMonitoringPlugin(pluginReference.getPluginReference, trait.currentLocation);
                        pluginReference.monitoredNPCs.put(npc.getId(), pluginReference.npcSettings.get(npc.getId()).locations.get(trait.currentLocation.LocationIdent));
                        return;
                    }
                }
            }
        } else {
            if (pluginReference.monitoredNPCs.containsKey(npc.getId())) {
                if ((npc.getEntity().getLocation().getBlockX() != trait.currentLocation.destination.getBlockX()) || (npc.getEntity().getLocation().getBlockZ() != trait.currentLocation.destination.getBlockZ())) {
                    pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationNewDestination|NPC:" + npc.getId()
                            + "|plugin disabled for this npc, aborting and returning to destination");
                    ((NPCSetting) pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentAction = NPCSetting.CurrentAction.ABORTING;
                    return;
                }
                pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationNewDestination|NPC:" + npc.getId() + "|plugin disabled for this npc, removing monitors.");
                trait.unsetMonitoringPlugin("Plugin Disabled");
                ((NPCSetting) pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentAction = NPCSetting.CurrentAction.IDLE;
                ((NPCSetting) pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentDestination = null;
                pluginReference.monitoredNPCs.remove(npc.getId());
            }
        }
    }

    @Override
    public boolean onNavigationReached(NPC npc, NPCDestinationsTrait trait, Destination_Setting destination) {
        if (pluginReference.npcSettings.containsKey(npc.getId())) {
            if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(destination.LocationIdent)) {

                LocationSetting rancherLocation = pluginReference.npcSettings.get(npc.getId()).locations.get(destination.LocationIdent);

                if (rancherLocation.regionName.equals("")) {
                    if (pluginReference.monitoredNPCs.containsKey(npc.getId()))
                        pluginReference.monitoredNPCs.remove(npc.getId());
                } else {
                    if (!pluginReference.monitoredNPCs.containsKey(npc.getId())) {
                        pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationReached|NPC:" + npc.getId() + "|Monitored location reached, assigning as monitor");
                        trait.setMonitoringPlugin(pluginReference.getPluginReference, destination);
                        pluginReference.monitoredNPCs.put(npc.getId(), pluginReference.npcSettings.get(npc.getId()).locations.get(destination.LocationIdent));
                    } else if (pluginReference.monitoredNPCs.containsKey(npc.getId()) && !pluginReference.monitoredNPCs.get(npc.getId()).locationID.toString().equals(destination.LocationIdent.toString())) {
                        pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationReached|NPC:" + npc.getId() + "|Monitored location reached, updating monitored location.");
                        pluginReference.monitoredNPCs.remove(npc.getId());
                        pluginReference.monitoredNPCs.put(npc.getId(), pluginReference.npcSettings.get(npc.getId()).locations.get(destination.LocationIdent));
                    }
                }
            } else {
                // Undo the monitoring for this NPC
                if (pluginReference.monitoredNPCs.containsKey(npc.getId()))
                    pluginReference.monitoredNPCs.remove(npc.getId());
                trait.unsetMonitoringPlugin("No actions at location");
            }
        }
        return false;
    }

    @Override
    public boolean onNewDestination(NPC npc, NPCDestinationsTrait trait, Destination_Setting destination) {
        if (pluginReference.npcSettings.containsKey(Integer.valueOf(npc.getId()))) {

            if (pluginReference.monitoredNPCs.containsKey(npc.getId())) {
                // Is this the same location??
                if (destination.LocationIdent.toString().equals(pluginReference.monitoredNPCs.get(npc.getId()).locationID.toString())) {
                    return false;
                }
            } else {
                return false;
            }

            Location entityLocation = npc.getEntity().getLocation();

            if (((NPCSetting) pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).locations.containsKey(trait.currentLocation.LocationIdent)) {

                if (entityLocation.distanceSquared(trait.currentLocation.destination) > 2) {

                    if (!DestinationsPlugin.Instance.getPathClass.path_Queue.containsKey(npc.getId())) {

                        pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationNewDestination|NPC:" + npc.getId() + "|New Location, returning to destination.");
                        if (Destinations_Rancher.Instance.getDestinationsPlugin != null)
                            Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINEST, "Rancher_Processing.pluginTick|NPC:" + npc.getId() + "|Not at destination|" + trait.currentLocation.destination
                                    + " Dist:" + npc.getEntity().getLocation().add(0.0D, -1.0D, 0.0D).distanceSquared(trait.currentLocation.destination));

                        DestinationsPlugin.Instance.getPathClass.addToQueue(npc, trait, npc.getEntity().getLocation().add(0.0D, -1.0D, 0.0D), trait.currentLocation.destination, 120, new ArrayList<Material>(), 0, true, true, true,
                                "Destinations_Rancher");
                        NPCSetting ranchSet = Destinations_Rancher.Instance.npcSettings.get(npc.getId());
                        ranchSet.lastAction = new Date();
                        ((NPCSetting) pluginReference.npcSettings.get(npc.getId())).currentAction = NPCSetting.CurrentAction.TRAVERSING;
                        return true;
                    }
                } else if (entityLocation.distanceSquared(trait.currentLocation.destination) > 0.6) {
                    npc.getEntity().teleport(trait.currentLocation.destination);
                } else if (pluginReference.monitoredNPCs.containsKey(npc.getId())) {
                    pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationNewDestination|NPC:" + npc.getId() + "|New Location,clearing monitors and releasing control.");
                    trait.unsetMonitoringPlugin("New Destination [" + destination.LocationIdent.toString() + ":" + pluginReference.monitoredNPCs.get(npc.getId()).locationID.toString() + "]");
                    pluginReference.monitoredNPCs.remove(npc.getId());
                    ((NPCSetting) pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentAction = NPCSetting.CurrentAction.IDLE;
                    ((NPCSetting) pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentDestination = null;
                }
            }
        }
        return false;
    }
}
