package com.sleelin.pvptoggle;

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

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
		
	public static PermissionHandler permissionHandler;
	
	private final PvPTogglePlayerListener playerListener = new PvPTogglePlayerListener(this);
	private final PvPToggleEntityListener entityListener = new PvPToggleEntityListener(this);
	private final PvPToggleWorldListener worldListener = new PvPToggleWorldListener(this);
	final static ArrayList<HashMap<Player, Boolean>> worlds = new ArrayList<HashMap<Player, Boolean>>();
	public Logger log = Logger.getLogger("Minecraft");
	public static List<String> worldnames = new ArrayList<String>();
	public static HashMap<String, Boolean> defaultenabled = new HashMap<String, Boolean>();
	public static HashMap<String, Boolean> worldstatus = new HashMap<String, Boolean>();
	public static HashMap<String, Boolean> forcepvpworld = new HashMap<String, Boolean>();
	public static HashMap<Player, Long> lasttoggle = new HashMap<Player, Long>();
	public static boolean citizensEnabled = false;
	
	public void onEnable(){
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "] Loading...");
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
		
		System.out.println("["+ pdfFile.getName() + "] Enabled!");
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
		try {
			log.info("[" + pdfFile.getName() + "] Config file not found, autogenerating...");
			FileWriter fstream = new FileWriter(mainDirectory+File.separator+fileName);
			BufferedWriter out = new BufferedWriter(fstream);
			configfile.createNewFile();
			
			out.write("globalDisabled: false\n");
			out.write("cooldown: 0\n");							
			out.write("worlds:\n");
			for (World world : this.getServer().getWorlds()){
				log.info("[" +pdfFile.getName() + "] found world " + world.getName().toString());
				out.write("    "+world.getName().toString()+":\n");
				out.write("        logindefault: true\n");
				out.write("        pvpenabled: true\n");
				out.write("        forcepvp: false\n");
			}
			out.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}
	
	private void setupPermissions(){
		PluginDescriptionFile pdfFile = this.getDescription();
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
		
		if (PvPToggle.permissionHandler == null){
			if (permissionsPlugin != null){
				if (!permissionsPlugin.getDescription().getVersion().equalsIgnoreCase("2.7.7")){
					PvPToggle.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
					log.info("[" + pdfFile.getName() + "] Permissions "+permissionsPlugin.getDescription().getVersion()+" detected!");
				} else {
					log.info("[" + pdfFile.getName() + "] Permissions bridge detected, using SuperPerms instead!");
				}
			} else {
				log.info("[" + pdfFile.getName() + "] Permissions system not detected, defaulting to SuperPerms + OP");
			}
		}
	}

	private void checkCitizens(){
		PluginDescriptionFile pdfFile = this.getDescription();
		Plugin citizensPlugin = this.getServer().getPluginManager().getPlugin("Citizens");
		
		if (citizensPlugin != null){
			PvPToggle.citizensEnabled = true;
			log.info("[" + pdfFile.getName() + "] Citizens Plugin detected!");
		}
	}	
	
	public void loadProcedure() throws IOException {
		Configuration config = new Configuration(configfile);
		config.load();
		globaldisabled = config.getBoolean("globalDisabled", false);
		cooldown = config.getInt("cooldown", 0);
		for (World world : this.getServer().getWorlds()){
			loadWorld(world);
		}

	}
	
	public void loadWorld(World world){
		PluginDescriptionFile pdfFile = this.getDescription();
		Configuration config = new Configuration(configfile);
		config.load();
		worldnames.add(world.getName());
		worldstatus.put(world.getName(), config.getBoolean("worlds."+world.getName()+".pvpenabled",true));
		defaultenabled.put(world.getName(), config.getBoolean("worlds."+world.getName()+".logindefault",true));
		forcepvpworld.put(world.getName(), config.getBoolean("worlds."+world.getName()+".forcepvp", false));
		worlds.add(new HashMap<Player, Boolean>());
		for (Player player : this.getServer().getOnlinePlayers()){
			if (!defaultenabled.get(world.getName())){
				this.pvpDisable(player, world.getName());
			} else {
				this.pvpEnable(player, world.getName());
			}
			lasttoggle.put(player, new GregorianCalendar().getTime().getTime()-(1000*PvPToggle.cooldown));
		}
		log.info("[" +pdfFile.getName() + "] found and loaded world " + world.getName().toString());
	}

	public void onDisable(){
		log.info("PvPToggle Disabled");
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
		if (worlds.get(getWorldIndex(world)).containsKey(player)){
			return worlds.get(getWorldIndex(world)).get(player);
		} else {
			lasttoggle.put(player, new GregorianCalendar().getTime().getTime()-(1000*PvPToggle.cooldown));
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
	
	public boolean permissionsCheck(Player player, String permissions){
		boolean haspermissions = false;
		if (PvPToggle.permissionHandler != null){
			haspermissions = PvPToggle.permissionHandler.has(player, permissions);
		} else {
			haspermissions = player.hasPermission(permissions);
		}
		if (player.hasPermission("pvptoggle.*")){
			haspermissions = true;
		}
		if (player.isOp()){
			haspermissions = true;
		}	
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