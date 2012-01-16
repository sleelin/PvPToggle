package com.sleelin.pvptoggle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import com.sleelin.pvptoggle.commands.globalpvpPluginCommand;
import com.sleelin.pvptoggle.commands.pvpPluginCommand;

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
		
	public static PermissionHandler permissionHandler;
	
	private final PvPTogglePlayerListener playerListener = new PvPTogglePlayerListener(this);
	private final PvPToggleEntityListener entityListener = new PvPToggleEntityListener(this);
	private final PvPToggleWorldListener worldListener = new PvPToggleWorldListener(this);
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
		
		getCommand("tpvp").setExecutor(new pvpPluginCommand(this));
		getCommand("pvp").setExecutor(new pvpPluginCommand(this));
		getCommand("gpvp").setExecutor(new globalpvpPluginCommand(this));
		
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.WORLD_LOAD, this.worldListener, Event.Priority.High, this);
		
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
		PluginDescriptionFile pdfFile = this.getDescription();
		Configuration config = this.getConfig();
		log.info("[" + pdfFile.getName() + "] Config file not found, autogenerating...");

		config.set("globalDisabled", false);
		config.set("cooldown", 0);
		config.set("warmup", 0);
		config.set("debug", false);
		for (World world : this.getServer().getWorlds()){
			log.info("[" +pdfFile.getName() + "] found world " + world.getName().toString());
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
	
	public boolean permissionsCheck(Player player, String permissions, boolean opdefault){
		boolean haspermissions = opdefault;
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
	
}