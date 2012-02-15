package com.sleelin.pvptoggle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import com.sleelin.pvptoggle.commands.GPVPCommand;
import com.sleelin.pvptoggle.commands.PVPCommand;
import com.sleelin.pvptoggle.listeners.EntityListener;
import com.sleelin.pvptoggle.listeners.PlayerListener;
import com.sleelin.pvptoggle.listeners.WorldListener;

/**
 * PvPToggle
 * 
 * @author sleelin
 *
 */

public class PvPToggle extends JavaPlugin {
	static String mainDirectory = "plugins/PvPToggle";
	static String fileName = "config.yml";
	public static File configfile = new File (mainDirectory + File.separator + fileName);
	static boolean globaldisabled;
	public static int cooldown = 0;
	public static int warmup = 0;
	public static boolean debugging = false;
	private static int updateinterval = 0;
		
	public static PermissionHandler permissionHandler;
	private Runnable updateThread;
    private int updateId = -1;
    private static final String RSS_URL = "http://dev.bukkit.org/server-mods/PvPToggle/files.rss";
    private static String version;
    private static String name;
	
	private final PlayerListener playerListener = new PlayerListener(this);
	private final EntityListener entityListener = new EntityListener(this);
	private final WorldListener worldListener = new WorldListener(this);
	final static ArrayList<HashMap<Player, Boolean>> worlds = new ArrayList<HashMap<Player, Boolean>>();
	public Logger log = Logger.getLogger("Minecraft");
	public static List<String> worldnames = new ArrayList<String>();
	public static HashMap<String, Boolean> defaultenabled = new HashMap<String, Boolean>();
	public static HashMap<String, Boolean> worldstatus = new HashMap<String, Boolean>();
	public static HashMap<Player, Long> lastpvp = new HashMap<Player, Long>();
	public static HashMap<Player, Long> lasttoggle = new HashMap<Player, Long>();
	public static boolean citizensEnabled = false;
	
	public void onEnable(){
		log.info("[" + this.getDescription().getName() + "] Loading...");
		new File(mainDirectory).mkdir();
		if (!configfile.exists()){
			createNewConfigFile();
		} 
		try {
			loadProcedure();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setupPermissions();
		checkCitizens();
		
		getCommand("tpvp").setExecutor(new PVPCommand(this));
		getCommand("pvp").setExecutor(new PVPCommand(this));
		getCommand("gpvp").setExecutor(new GPVPCommand(this));
		
		this.getServer().getPluginManager().registerEvents(this.playerListener, this);
		this.getServer().getPluginManager().registerEvents(this.entityListener, this);
		this.getServer().getPluginManager().registerEvents(this.worldListener, this);
		
		PvPToggle.version = this.getDescription().getVersion();
		PvPToggle.name = this.getDescription().getName();
		startUpdateThread();
		
		System.out.println("["+ this.getDescription().getName() + "] v"+this.getDescription().getVersion()+" enabled!");
	}
	
	public void onLoad(){
		for (String worldname : worldnames){
			worlds.get(getWorldIndex(worldname)).clear();
		}
		worlds.clear();
		worldnames.clear();
	}
	
	private void createNewConfigFile(){
		Configuration config = this.getConfig();
		log.info("[" + this.getDescription().getName() + "] Config file not found, autogenerating...");

		config.set("globalDisabled", false);
		config.set("cooldown", 0);
		config.set("warmup", 0);
		config.set("debug", false);
		config.set("updateinterval", 21600);
		for (World world : this.getServer().getWorlds()){
			log.info("[" + this.getDescription().getName() + "] found world " + world.getName().toString());
			config.set("worlds."+world.getName().toString()+".logindefault", false);
			config.set("worlds."+world.getName().toString()+".pvpenabled", true);
		}
		this.saveConfig();
	}
	
	private void setupPermissions(){
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
		
		if (PvPToggle.permissionHandler == null){
			if (permissionsPlugin != null){
				if (!permissionsPlugin.getDescription().getVersion().equalsIgnoreCase("2.7.7")){
					PvPToggle.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
					log.info("[" + this.getDescription().getName() + "] Permissions "+permissionsPlugin.getDescription().getVersion()+" detected!");
				} else {
					log.info("[" + this.getDescription().getName() + "] Permissions bridge detected, using SuperPerms instead!");
				}
			} else {
				log.info("[" + this.getDescription().getName() + "] Permissions system not detected, defaulting to SuperPerms + OP");
			}
		}
	}

	private void checkCitizens(){
		Plugin citizensPlugin = this.getServer().getPluginManager().getPlugin("Citizens");
		
		if (citizensPlugin != null){
			PvPToggle.citizensEnabled = true;
			log.info("[" + this.getDescription().getName() + "] Citizens Plugin detected!");
		}
	}	
	
	private void loadProcedure() throws IOException {
		Configuration config = this.getConfig();
		globaldisabled = config.getBoolean("globalDisabled", false);
		cooldown = config.getInt("cooldown", 0);
		warmup = config.getInt("warmup", 0);
		debugging = config.getBoolean("debug", false);
		updateinterval = config.getInt("updateinterval", 21600);
		for (World world : this.getServer().getWorlds()){
			loadWorld(world);
		}

	}
	
	public void loadWorld(World world){
		Configuration config = this.getConfig();
		worldnames.add(world.getName());
		worldstatus.put(world.getName(), config.getBoolean("worlds."+world.getName()+".pvpenabled",true));
		defaultenabled.put(world.getName(), config.getBoolean("worlds."+world.getName()+".logindefault",true));
		worlds.add(new HashMap<Player, Boolean>());
		for (Player player : this.getServer().getOnlinePlayers()){
			if (!defaultenabled.get(world.getName())){
				this.pvpDisable(player, world.getName());
			} else {
				this.pvpEnable(player, world.getName());
			}
			lastpvp.put(player, new GregorianCalendar().getTime().getTime()-(1000*PvPToggle.cooldown));
		}
		log.info("[" +this.getDescription().getName() + "] found and loaded world " + world.getName().toString());
	}

	public void onDisable(){
		stopUpdateThread();
		log.info("[PvPToggle] Disabled");
	}
	
	private int getWorldIndex(String world) {
		int i = 0;
		for (String worldname : PvPToggle.worldnames){
			if (worldname.equalsIgnoreCase(world)){
				break;
			}
			i++;
		}
		return i;
	}
	
	public void pvpEnable(Player player, String world) {
		worlds.get(getWorldIndex(world)).remove(player);
		worlds.get(getWorldIndex(world)).put(player, true);
	}

	public void pvpDisable(Player player, String world) {
		worlds.get(getWorldIndex(world)).remove(player);
		worlds.get(getWorldIndex(world)).put(player, false);	
	}

	public boolean pvpEnabled(Player player, String world) {
		if (this.permissionsCheck(player, "pvptoggle.pvp.force", false)) return true;
		if (this.permissionsCheck(player, "pvptoggle.pvp.deny", false)) return false;
		if (worlds.get(getWorldIndex(world)).containsKey(player)){
			return worlds.get(getWorldIndex(world)).get(player);
		} else {
			lastpvp.put(player, new GregorianCalendar().getTime().getTime()-(1000*PvPToggle.cooldown));
			if (!defaultenabled.get(world)){
				this.pvpDisable(player, world);
				return false;
			} else {
				this.pvpEnable(player, world);
				return true;
			}
		}
	}
	
	public boolean alreadyLoaded(Player player){
		for (String worldname : worldnames){
			if (worlds.get(getWorldIndex(worldname)).containsKey(player)){
				return true;
			}
		}
		return false;
	}

	public boolean gpvpEnabled() {
		if (globaldisabled == false){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean permissionsCheck(CommandSender sender, String permissions, boolean opdefault){
		boolean haspermissions = opdefault;
		Player player;
		
		if (sender instanceof Player){
			player = (Player) sender;
		} else {
			return true;
		}
		if (debugging) log.info(player.getName().toString()+"/"+permissions+"/Start: "+haspermissions);
		
		if (PvPToggle.permissionHandler != null){
			haspermissions = PvPToggle.permissionHandler.has(player, permissions);
			if (debugging) log.info(player.getName().toString()+"/"+permissions+"/LegPerms: "+haspermissions);
			if (PvPToggle.permissionHandler.has(player, "*")){
				haspermissions = opdefault;
			}
		} else {
			haspermissions = player.hasPermission(permissions);
			if (debugging) log.info(player.getName().toString()+"/"+permissions+"/Before*: "+haspermissions);
			if (player.hasPermission("*")){
				haspermissions = opdefault;
			}
			if (debugging) log.info(player.getName().toString()+"/"+permissions+"/After*: "+haspermissions);
		}
		
		if (debugging) log.info(player.getName().toString()+"/"+permissions+"/Final: "+haspermissions);
		return haspermissions;
	}	

	public void setWorldStatus(String targetworld, boolean newval) {
		worldstatus.put(targetworld, newval);
		defaultenabled.put(targetworld, newval);
	}

	public void gpvpToggle(boolean newval) {
		if (newval == false){
			globaldisabled = true; 
		} else {
			globaldisabled = false;
		}
	}
	
	// Thanks to Defxor & Courier for code on how to create an update checking thread
	// http://dev.bukkit.org/profiles/defxor
	private void startUpdateThread() {

        if(updateinterval == 0) { // == disabled
            return;
        }
        if(updateThread == null) {
            updateThread = new Runnable() {
                public void run() {
                    String checkVersion = updateCheck(version);
                    if(!checkVersion.endsWith(version)) {
                        log.warning("["+name+"] Found new version: " + checkVersion + " (you have [v" + version + "])");
                        log.warning("["+name+"] Visit http://dev.bukkit.org/server-mods/" + name + "/ to download!");
                    }
                }
            };
        }
        // 400 = 20 seconds from start, then a period according to config (default every 24h)
        updateId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, updateThread, 400, updateinterval*20);
    }

    private void stopUpdateThread() {
        if(updateId != -1) {
            getServer().getScheduler().cancelTask(updateId);
            updateId = -1;
        }
    }
	
    // Thanks to Sleaker & vault for the hint and code on how to use BukkitDev RSS feed for this
    // http://dev.bukkit.org/profiles/Sleaker/
    public String updateCheck(String currentVersion) {
        try {
            URL url = new URL(RSS_URL);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
            doc.getDocumentElement().normalize();
            NodeList nodes = doc.getElementsByTagName("item");
            Node firstNode = nodes.item(0);
            if (firstNode.getNodeType() == 1) {
                Element firstElement = (Element)firstNode;
                NodeList firstElementTagName = firstElement.getElementsByTagName("title");
                Element firstNameElement = (Element) firstElementTagName.item(0);
                NodeList firstNodes = firstNameElement.getChildNodes();
                return firstNodes.item(0).getNodeValue();
            }
        }
        catch (Exception e) {
            return currentVersion;
        }
        return currentVersion;
    }
}