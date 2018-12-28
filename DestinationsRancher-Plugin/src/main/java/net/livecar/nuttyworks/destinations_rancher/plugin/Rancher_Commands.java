package net.livecar.nuttyworks.destinations_rancher.plugin;

import java.util.UUID;
import java.util.logging.Level;

import net.livecar.nuttyworks.destinations_rancher.storage.ActionType;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.destinations_rancher.storage.LocationSetting;
import net.livecar.nuttyworks.destinations_rancher.storage.NPCSetting;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.listeners.commands.CommandInfo;

public class Rancher_Commands {
    @CommandInfo(name = "locrancher",
            group = "External Plugin Commands",
            languageFile = "rancher",
            helpMessage = "command_locrancher_help",
            arguments = { "#", "<region>|#" },
            permission = { "npcdestinations.editall.locrancher","npcdestinations.editown.locrancher" },
            allowConsole = true,
            minArguments = 1,
            maxArguments = 3)
    public boolean npcDest_locSentinel(DestinationsPlugin destRef, CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait npcTrait) {
        if (!sender.hasPermission("npcdestinations.editall.locrancher") && !sender.isOp() && !(isOwner && sender.hasPermission("npcdestinations.editown.locrancher"))) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.no_permissions");
            return true;
        } else {
            if (inargs.length < 2) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.command_badargs");
                return true;
            }

            int nIndex = Integer.parseInt(inargs[1]);
            if (nIndex > npcTrait.NPCLocations.size() - 1) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
                return true;
            }

            Rancher_Addon addonReference = (Rancher_Addon) destRef.getPluginManager.getPluginByName("Rancher");

            Destination_Setting destSetting = npcTrait.NPCLocations.get(nIndex);

            if (inargs.length == 2) {
                destRef.getMessageManager.sendMessage("rancher", sender, "setting_menu", npcTrait, destSetting);
                return true;
            }

            NPCSetting ranchSetting;
            if (!addonReference.pluginReference.npcSettings.containsKey(npc.getId())) {
                ranchSetting = new NPCSetting();
                ranchSetting.setNPC(npc.getId());
            } else {
                ranchSetting = addonReference.pluginReference.npcSettings.get(npc.getId());
            }

            LocationSetting locSetting;
            if (!ranchSetting.locations.containsKey(destSetting.LocationIdent)) {
                locSetting = new LocationSetting();
                locSetting.locationID = destSetting.LocationIdent;
                ranchSetting.locations.put(locSetting.locationID, locSetting);
            } else {
                locSetting = ranchSetting.locations.get(destSetting.LocationIdent);
            }

            switch (inargs[2].toLowerCase()) {
            case "maxbabies":
                if (inargs.length > 3) {
                    if (inargs[3].matches("\\d+")) {
                        locSetting.maxBabies = Integer.parseInt(inargs[3]);
                    } else {
                        destRef.getMessageManager.sendMessage("rancher", sender, "messages.command_badargs");
                        return true;
                    }
                }
                break;
            case "maxadults":
                if (inargs.length > 3) {
                    if (inargs[3].matches("\\d+")) {
                        locSetting.maxAdults = Integer.parseInt(inargs[3]);
                    } else {
                        destRef.getMessageManager.sendMessage("rancher", sender, "messages.command_badargs");
                        return true;
                    }
                }
                break;
            case "regionname":
                if (inargs.length > 3) {
                    if (destRef.getWorldGuardPlugin == null) {
                        destRef.getMessageManager.sendMessage("rancher", sender, "messages.command_noworldguard");
                        return true;
                    } else {
                        if (!destRef.getWorldGuardPlugin.getRegionList(npc.getEntity().getWorld()).contains(inargs[3])) {
                            destRef.getMessageManager.sendMessage("rancher", sender, "messages.command_invalidregion");
                            return true;
                        } else {
                            locSetting.regionName = inargs[3];
                        }
                    }
                }
                break;
            case "toggleanimal":
                if (inargs.length > 3) {
                    if (EnumUtils.isValidEnum(EntityType.class, inargs[3].toUpperCase())) {
                        EntityType animalType = EntityType.valueOf(inargs[3].toUpperCase());

                        if (!addonReference.pluginReference.getAnimalHelper.getSupportedAnimals().contains(animalType)) {
                            addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("rancher", sender, "messages.command_badargs");
                            return true;
                        }

                        if (locSetting.enabledAnimals.contains(animalType))
                            locSetting.enabledAnimals.remove(animalType);
                        else
                            locSetting.enabledAnimals.add(animalType);
                    }
                }
                break;
            case "toggleaction":
                if (inargs.length > 3) {
                    if (EnumUtils.isValidEnum(ActionType.class, inargs[3].toUpperCase())) {
                        ActionType actionType = ActionType.valueOf(inargs[3].toUpperCase());

                        if (locSetting.enabledActions.contains(actionType))
                            locSetting.enabledActions.remove(actionType);
                        else
                            locSetting.enabledActions.add(actionType);
                    }
                }
                break;
            default:
                break;
            }

            if (!addonReference.pluginReference.npcSettings.containsKey(npc.getId())) {
                addonReference.pluginReference.npcSettings.put(npc.getId(), ranchSetting);
                addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("rancher", sender, "messages.plugin_debug");
            }

            if (locSetting.locationID.equals(npcTrait.currentLocation.LocationIdent)) {
                destRef.getMessageManager.debugMessage(Level.INFO, "rancher_Plugin.onUserCommand|NPC:" + npc.getId() + "|Location added, starting monitor");
                NPCDestinationsTrait trait = npc.getTrait(NPCDestinationsTrait.class);
                trait.setMonitoringPlugin(addonReference.pluginReference.getPluginReference, npcTrait.currentLocation);

                addonReference.pluginReference.monitoredNPCs.put(npc.getId(), addonReference.pluginReference.npcSettings.get(npc.getId()).locations.get(npcTrait.currentLocation.LocationIdent));

                destRef.getMessageManager.sendMessage("rancher", sender, "messages.command_added_active", npcTrait, npcTrait.currentLocation);
            } else {
                destRef.getMessageManager.sendMessage("rancher", sender, "messages.command_added_notactive", npcTrait, npcTrait.currentLocation);
            }
            destRef.getMessageManager.sendMessage("rancher", sender, "setting_menu", npcTrait, destSetting);
            return true;
        }
    }

    @CommandInfo(name = "rancherjob", group = "External Plugin Commands", languageFile = "rancher", helpMessage = "command_rancherjob_help", arguments = { "#", "<region>|#" }, permission = { "npcdestinations.editall.rancherjob",
            "npcdestinations.editown.rancherjob" }, allowConsole = false, minArguments = 0, maxArguments = 0)
    public boolean npcDest_rancherjob(DestinationsPlugin destRef, CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait npcTrait) {

        Rancher_Addon addonReference = (Rancher_Addon) destRef.getPluginManager.getPluginByName("Rancher");

        NPCSetting ranchSetting;
        if (!addonReference.pluginReference.npcSettings.containsKey(npc.getId())) {
            return false;
        } else {
            ranchSetting = addonReference.pluginReference.npcSettings.get(npc.getId());
        }

        for (int nLoc = 0; nLoc < npcTrait.NPCLocations.size(); nLoc++) {
            String locActions = "";

            LocationSetting locSetting;
            if (!ranchSetting.locations.containsKey(npcTrait.NPCLocations.get(nLoc).LocationIdent)) {
                continue;
            } else {
                locSetting = ranchSetting.locations.get(npcTrait.NPCLocations.get(nLoc).LocationIdent);
            }

            for (EntityType workingAnimal : locSetting.enabledAnimals) {
                if (addonReference.pluginReference.getProcessingClass.getBabyCount(npc, locSetting, workingAnimal) < locSetting.maxBabies && locSetting.enabledActions.contains(ActionType.BREED)
                        && addonReference.pluginReference.getProcessingClass.getValidActions(npc, locSetting, ActionType.BREED, new UUID(0L, 0L), workingAnimal).validAnimals.size() > 0) {
                    locActions += "Breeding ";
                }

                if (locSetting.enabledActions.contains(ActionType.SLAUGHTER) && addonReference.pluginReference.getProcessingClass.getValidActions(npc, locSetting, ActionType.SLAUGHTER, new UUID(0L, 0L), workingAnimal).validAnimals
                        .size() > locSetting.maxAdults) {
                    locActions += "Slaughter ";
                }

                if (locSetting.enabledActions.contains(ActionType.MILK) && addonReference.pluginReference.getProcessingClass.getValidActions(npc, locSetting, ActionType.MILK, new UUID(0L, 0L), workingAnimal).validAnimals.size() > 0) {
                    locActions += "Milk ";
                }

                if (locSetting.enabledActions.contains(ActionType.SHEAR) && addonReference.pluginReference.getProcessingClass.getValidActions(npc, locSetting, ActionType.SHEAR, new UUID(0L, 0L), workingAnimal).validAnimals.size() > 0) {
                    locActions += "Shear ";
                }
            }
            destRef.getMessageManager.sendMessage("rancher", sender, "messages.rancher_jobs", npcTrait, npcTrait.NPCLocations.get(nLoc), locActions);
        }
        return true;
    }

}
