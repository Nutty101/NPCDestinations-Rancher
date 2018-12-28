package net.livecar.nuttyworks.destinations_rancher;

import net.citizensnpcs.api.event.CitizensDisableEvent;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.livecar.nuttyworks.destinations_rancher.animals.RanchAnimal_V1_11_R1;
import net.livecar.nuttyworks.destinations_rancher.animals.RanchAnimal_V1_12_R1;
import net.livecar.nuttyworks.destinations_rancher.animals.RanchAnimal_V1_13_R2;
import net.livecar.nuttyworks.destinations_rancher.storage.MonitoredEntity;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class DestRancher_Plugin extends org.bukkit.plugin.java.JavaPlugin implements org.bukkit.event.Listener {

    public HashMap<UUID, MonitoredEntity> monitorDrops = null;

    @Override
    public void onEnable() {

        //Validate that Destinations Exists
        if (getServer().getPluginManager().getPlugin("NPC_Destinations") == null) {
            Bukkit.getLogger().log(Level.INFO, "[" + getDescription().getName() + "] " + "NPCDestinations2 not found, not registering as plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            if (getServer().getPluginManager().getPlugin("NPC_Destinations").getDescription().getVersion().startsWith("1")) {
                Bukkit.getLogger().log(Level.INFO, "[" + getDescription().getName() + "] " + "NPCDestinations V1 was found, This requires V2. Not registering as plugin");
                getServer().getPluginManager().disablePlugin(this);
                return;
            } else if (!getServer().getPluginManager().getPlugin("NPC_Destinations").isEnabled()) {
                Bukkit.getLogger().log(Level.INFO, "[" + getDescription().getName() + "] " + "NPCDestinations was found, but was disabled. Not registering as plugin");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            Destinations_Rancher.Instance = new Destinations_Rancher();
            Destinations_Rancher.Instance.getPluginReference = this;
            Destinations_Rancher.Instance.getDestinationsPlugin = DestinationsPlugin.Instance;
            // Force destinations to refresh its language files.
            Destinations_Rancher.Instance.getDestinationsPlugin.getLanguageManager.loadLanguages(true);
        }

        // Global references
        Destinations_Rancher.Instance.getCitizensPlugin = DestinationsPlugin.Instance.getCitizensPlugin;

        // Setup the default paths in the storage folder.
        Destinations_Rancher.Instance.languagePath = new File(DestinationsPlugin.Instance.getDataFolder(), "/Languages/");

        // Generate the default folders and files.
        Destinations_Rancher.Instance.getDefaultConfigs();

        // Setup the allowed animal types
        if (getServer().getClass().getPackage().getName().endsWith("v1_8_R3")) {
            Destinations_Rancher.Instance.getAnimalHelper = null;
        } else if (getClass().getPackage().getName().endsWith("v1_9_R1")) {
            Destinations_Rancher.Instance.getAnimalHelper = null;
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_9_R2")) {
            Destinations_Rancher.Instance.getAnimalHelper = null;
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_10_R1")) {
            Destinations_Rancher.Instance.getAnimalHelper = new RanchAnimal_V1_11_R1();
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_11_R1")) {
            Destinations_Rancher.Instance.getAnimalHelper = new RanchAnimal_V1_11_R1();
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_12_R1")) {
            Destinations_Rancher.Instance.getAnimalHelper = new RanchAnimal_V1_12_R1();
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_13_R2")) {
            Destinations_Rancher.Instance.getAnimalHelper = new RanchAnimal_V1_13_R2();
        } else {
            // Unknown version, abort loading of this plugin
            Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.consoleMessage(this, "animations", "console_messages.plugin_unknownversion");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);
        monitorDrops = new HashMap<UUID, MonitoredEntity>();

    }

    @Override
    public void onDisable() {
        if (this.isEnabled()) {
            if (Destinations_Rancher.Instance != null && Destinations_Rancher.Instance.getDestinationsPlugin != null) {
                Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.CONFIG, "nuDestinationRancher.onDisable()|Stopping Internal Processes");
            }
            Bukkit.getServer().getScheduler().cancelTasks(this);
        }
    }

    @EventHandler
    public void CitizensLoaded(final CitizensEnableEvent event) {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    Destinations_Rancher.Instance.getProcessingClass.pluginTick();
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    sw.toString(); // stack trace as a string
                    if (Destinations_Rancher.Instance.getDestinationsPlugin != null)
                        Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.logToConsole(Destinations_Rancher.Instance.getPluginReference, "Error:" + sw);
                    else
                        Destinations_Rancher.Instance.logToConsole("Error on ranchertick: " + sw);
                }
            }
        }, 1L, 10L);
    }

    @EventHandler
    public void CitizensDisabled(final CitizensDisableEvent event) {
        Bukkit.getServer().getScheduler().cancelTasks(this);
        if (Destinations_Rancher.Instance.getDestinationsPlugin == null) {
            Destinations_Rancher.Instance.logToConsole("Disabled..");
        } else {
            Destinations_Rancher.Instance.getDestinationsPlugin.getMessageManager.consoleMessage(Destinations_Rancher.Instance.getPluginReference, "rancher", "console_messages.plugin_ondisable");
        }
        Destinations_Rancher.Instance = null;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public final void onDeathEvent(EntityDeathEvent event) {
        if (monitorDrops == null)
            return;

        if (monitorDrops.containsKey(event.getEntity().getUniqueId())) {
            MonitoredEntity monEnt = monitorDrops.get(event.getEntity().getUniqueId());
            switch (monEnt.entityAction) {
                case BREED:
                    // did we drop exp?
                    event.setDroppedExp(0);
                    event.getDrops().clear();
                    monitorDrops.remove(event.getEntity().getUniqueId());
                    break;
                case KILL:
                    // did we drop exp?
                    event.setDroppedExp(0);
                    event.getDrops().clear();
                    monitorDrops.remove(event.getEntity().getUniqueId());
                    break;
                case KILL_STORE:
                    // did we drop exp?
                    event.setDroppedExp(0);
                    ItemStack[] items = new ItemStack[event.getDrops().size()];
                    Destinations_Rancher.Instance.addToInventory(monEnt.actionBy, event.getDrops().toArray(items));
                    event.getDrops().clear();
                    monitorDrops.remove(event.getEntity().getUniqueId());
                    break;
                default:
                    break;
            }
        }
    }
}
