package com.sleelin.pvptoggle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
import com.sleelin.pvptoggle.commands.*;

/**
 * PvPToggle
 * 
 * @author sleelin
 *
 */

public class PvPToggle extends JavaPlugin {
	static String mainDirectory = "plugins/PvPToggle";
	static String fileName = "config.yml";
	static File configfile = new File (mainDirectory + File.separator + fileName);
	static boolean globaldisabled;
		
	public static PermissionHandler permissionHandler;
	
	private final PvPTogglePlayerListener playerListener = new PvPTogglePlayerListener(this);
	private final PvPToggleEntityListener EntityListener = new PvPToggleEntityListener(this);
	private final ArrayList<HashMap<Player, Boolean>> worlds = new ArrayList<HashMap<Player, Boolean>>();
	public Logger log = Logger.getLogger("Minecraft");
	public static List<String> worldnames = new ArrayList<String>();
	public static HashMap<String, Boolean> defaultenabled = new HashMap<String, Boolean>();
	public static HashMap<String, Boolean> worldstatus = new HashMap<String, Boolean>();
	
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
		
		getCommand("tpvp").setExecutor(new pvpPluginCommand(this));
		getCommand("pvp").setExecutor(new pvpPluginCommand(this));
		getCommand("gpvp").setExecutor(new globalpvpPluginCommand(this));
		
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.EntityListener, Event.Priority.Normal, this);
		
		registerOnlinePlayers(this.getServer().getOnlinePlayers());
		
		System.out.println("["+ pdfFile.getName() + "] Enabled!");
	}
	
	private void createNewConfigFile(){
		PluginDescriptionFile pdfFile = this.getDescription();
		try {
			log.info("[" + pdfFile.getName() + "] Config file not found, autogenerating...");
			FileWriter fstream = new FileWriter(mainDirectory+File.separator+fileName);
			BufferedWriter out = new BufferedWriter(fstream);
			configfile.createNewFile();
			
			out.write("globalDiabled: false\n");
							
			out.write("worlds:\n");
			for (World world : this.getServer().getWorlds()){
				log.info("[" +pdfFile.getName() + "] found world " + world.getName().toString());
				out.write("    "+world.getName().toString()+":\n");
				out.write("        logindefault: true\n");
				out.write("        pvpenabled: true\n");
			}
			out.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}
	
	private void registerOnlinePlayers(Player[] onlinePlayers) {
		for (Player player : onlinePlayers){
			for (String worldname : PvPToggle.worldnames){
				if (defaultenabled.get(worldname)){
					this.pvpDisable(player, worldname);
				} else {
					this.pvpEnable(player, worldname);
				}
			}
		}
		
	}

	private void setupPermissions(){
		PluginDescriptionFile pdfFile = this.getDescription();
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
		
		if (PvPToggle.permissionHandler == null){
			if (permissionsPlugin != null){
				PvPToggle.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
				log.info("[" + pdfFile.getName() + "] Permissions system detected!");
			} else {
				log.info("[" + pdfFile.getName() + "] Permissions system not detected, defaulting to OP");
			}
		}
	}
	
	public void loadProcedure() throws IOException {
		Configuration config = new Configuration(configfile);
		config.load();
		globaldisabled = config.getBoolean("globalDisabled", false);
		List<World> tmpworldnames = this.getServer().getWorlds();
		for (World world : tmpworldnames){
			worldnames.add(world.getName());
			worldstatus.put(world.getName(), config.getBoolean("worlds."+world.getName()+".pvpenabled",true));
			defaultenabled.put(world.getName(), config.getBoolean("worlds."+world.getName()+".logindefault",true));
			HashMap<Player, Boolean> players = new HashMap<Player, Boolean>();
			worlds.add(players);
		}

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
		HashMap<Player, Boolean> players = worlds.get(getWorldIndex(world));
		players.put(player, true);
		worlds.remove(getWorldIndex(world));
		worlds.add(getWorldIndex(world), players);
	}

	public void pvpDisable(Player player, String world) {
		HashMap<Player, Boolean> players = worlds.get(getWorldIndex(world));
		players.put(player, false);
		worlds.remove(getWorldIndex(world));
		worlds.add(getWorldIndex(world), players);	
	}

	public boolean pvpEnabled(Player player, String world) {
		HashMap<Player, Boolean> players = worlds.get(getWorldIndex(world));
		return players.get(player);
	}
	
	public boolean alreadyLoaded(Player player){
		for (String worldname : worldnames){
			HashMap<Player, Boolean> players = worlds.get(getWorldIndex(worldname));
			if (players.containsKey(player)){
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
			if (PvPToggle.permissionHandler.has(player, permissions)){
				haspermissions = true;
			}
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