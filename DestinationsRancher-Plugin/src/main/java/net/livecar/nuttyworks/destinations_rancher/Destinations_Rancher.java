package net.livecar.nuttyworks.destinations_rancher;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.livecar.nuttyworks.destinations_rancher.animals.RanchAnimal;
import net.livecar.nuttyworks.destinations_rancher.plugin.Rancher_Addon;
import net.livecar.nuttyworks.destinations_rancher.plugin.Rancher_Commands;
import net.livecar.nuttyworks.destinations_rancher.plugin.Rancher_Processing;
import net.livecar.nuttyworks.destinations_rancher.storage.LocationSetting;
import net.livecar.nuttyworks.destinations_rancher.storage.NPCSetting;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

public class Destinations_Rancher {
    // For quick reference to this instance of the plugin.
    public static Destinations_Rancher Instance = null;

    // Links to classes
    public Citizens getCitizensPlugin;
    public DestinationsPlugin getDestinationsPlugin = null;
    public DestRancher_Plugin getPluginReference = null;
    public Rancher_Processing getProcessingClass = null;
    public Rancher_Addon getRancherPlugin = null;

    // variables
    public int Version = 10000;
    public int entityRadius = 47 * 47;

    public Map<Integer, NPCSetting> npcSettings = new HashMap<Integer, NPCSetting>();
    public Map<Integer, LocationSetting> monitoredNPCs = new HashMap<Integer, LocationSetting>();
    public Map<UUID, Date> milkingHistory = new HashMap<UUID, Date>();
    public Map<UUID, Date> breedingHistory = new HashMap<UUID, Date>();

    public RanchAnimal getAnimalHelper = null;

    // Storage locations
    public File languagePath;

    public Destinations_Rancher() {
        this.getRancherPlugin = new Rancher_Addon(this);
        DestinationsPlugin.Instance.getPluginManager.registerPlugin(getRancherPlugin);
        DestinationsPlugin.Instance.getCommandManager.registerCommandClass(Rancher_Commands.class);
        this.getProcessingClass = new Rancher_Processing();
    }

    void getDefaultConfigs() {
        // Create the default folders
        if (!DestinationsPlugin.Instance.getDataFolder().exists())
            DestinationsPlugin.Instance.getDataFolder().mkdirs();
        if (!languagePath.exists())
            languagePath.mkdirs();

        // Validate that the default package is in the MountPackages folder. If
        // not, create it.
        exportConfig(languagePath, "en_def-rancher.yml");

    }

    void exportConfig(File path, String filename) {
        DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINEST, "nuDestinationsRancher.exportConfig()|");
        File fileConfig = new File(path, filename);
        if (!fileConfig.isDirectory()) {
            // Reader defConfigStream = null;
            try {
                FileUtils.copyURLToFile((URL) getClass().getResource("/" + filename), fileConfig);
            } catch (IOException e1) {
                if (getDestinationsPlugin != null)
                    DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.SEVERE, "nuDestinationsRancher.exportConfig()|FailedToExtractFile(" + filename + ")");
                else
                    logToConsole(" Failed to extract default file (" + filename + ")");
                return;
            }
        }
    }

    public void logToConsole(String logLine) {
        Bukkit.getLogger().log(Level.INFO, "[" + DestinationsPlugin.Instance.getDescription().getName() + "] " + logLine);
    }

    public void addToInventory(NPC npc, ItemStack[] drops) {
        ItemStack[] npcInventory = npc.getTrait(Inventory.class).getContents();
        for (ItemStack item : drops) {
            int emptySlot = -1;
            for (int slot = 1; slot < npcInventory.length; slot++) {
                if (slot < 2 || (slot > 35 && slot < 41))
                    continue;

                if (npcInventory[slot] == null && emptySlot == -1) {
                    emptySlot = slot;
                    continue;
                } else if (npcInventory[slot] != null && npcInventory[slot].getType() == Material.AIR && emptySlot == -1) {
                    emptySlot = slot;
                    continue;
                }

                if (npcInventory[slot] != null)
                    if (Destinations_Rancher.Instance.getDestinationsPlugin != null)
                        Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "NPC:" + npc.getId() + "|Slot:" + slot + " Item:" + npcInventory[slot].getType() + "/"
                                + npcInventory[slot].getAmount() + "/" + npcInventory[slot].getType().getMaxStackSize() + " Inv Item:" + item.getType() + "/" + item.getAmount());
                if (npcInventory[slot] == null)
                    if (Destinations_Rancher.Instance.getDestinationsPlugin != null)
                        Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "NPC:" + npc.getId() + "|Slot: null" + " Inv Item:" + item.getType() + "/" + item
                                .getAmount());

                if (npcInventory[slot] != null) {
                    if (npcInventory[slot].getType() == item.getType() && npcInventory[slot].getAmount() < npcInventory[slot].getType().getMaxStackSize()) {
                        if (Destinations_Rancher.Instance.getDestinationsPlugin != null)
                            Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "NPC:" + npc.getId() + "|SlotCheck: " + npcInventory[slot].getAmount() + item
                                    .getAmount() + ">" + npcInventory[slot].getType().getMaxStackSize());
                        if ((npcInventory[slot].getAmount() + item.getAmount()) > (npcInventory[slot].getType().getMaxStackSize())) {
                            int leftOver = Math.abs(npcInventory[slot].getType().getMaxStackSize() - (npcInventory[slot].getAmount() + item.getAmount()));
                            npcInventory[slot].setAmount(item.getAmount() - leftOver);
                            item.setAmount(item.getAmount() - (item.getAmount() - leftOver));
                        } else {
                            npcInventory[slot].setAmount(npcInventory[slot].getAmount() + item.getAmount());
                            item = null;
                            break;
                        }
                    }
                }
            }

            if (emptySlot != -1 && npcInventory[emptySlot] == null && item != null) {
                npcInventory[emptySlot] = item;
                item = null;
                emptySlot = npcInventory.length;
                continue;
            }
        }
        npc.getTrait(Inventory.class).setContents(npcInventory);
    }

    public boolean itemInHand(LocationSetting ranchLocation, NPC npc, Material handItem) {

        NPCDestinationsTrait npcTrait = npc.getTrait(NPCDestinationsTrait.class);
        Destination_Setting destinationLocation = null;

        for (Destination_Setting destinationLoc : npcTrait.NPCLocations) {
            if (destinationLoc.LocationIdent.equals(ranchLocation.locationID)) {
                destinationLocation = destinationLoc;
                break;
            }
        }

        Equipment npcEquip = npc.getTrait(Equipment.class);
        if (destinationLocation == null) {
            if (npcEquip != null && npcEquip.get(EquipmentSlot.HAND) != null) {
                if (npcEquip.get(EquipmentSlot.HAND).getType() == handItem)
                    return true;
            }
            return false;
        }

        // Does the location clear inventory?
        if (destinationLocation.items_Clear == true && destinationLocation.items_Hand.getType() == handItem)
            return true;
        else if (destinationLocation.items_Clear == true)
            return false;

        if (npcEquip != null && npcEquip.get(EquipmentSlot.HAND) != null)
            if (npcEquip.get(EquipmentSlot.HAND).getType() == handItem)
                return true;

        return false;
    }
}
