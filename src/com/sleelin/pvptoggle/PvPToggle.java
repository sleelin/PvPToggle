package com.sleelin.pvptoggle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
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
	static Properties prop = new Properties();
	static File configfile = new File (mainDirectory + File.separator + "config.conf");
	public static boolean defaultdisabled;
	static boolean globaldisabled;
		
	public static PermissionHandler permissionHandler;
	
	private final PvPTogglePlayerListener playerListener = new PvPTogglePlayerListener(this);
	private final PvPToggleEntityListener EntityListener = new PvPToggleEntityListener(this);
	public final HashMap<Player, Boolean> players = new HashMap<Player, Boolean>();
	public Logger log = Logger.getLogger("Minecraft");
	
	public void onEnable(){
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println("[" + pdfFile.getName() + "] Loading...");
		new File(mainDirectory).mkdir();
		if (!configfile.exists()){
			try {
				log.info("[" + pdfFile.getName() + "] Config file not found, autogenerating...");
				configfile.createNewFile();
				FileOutputStream out = new FileOutputStream(configfile);
				prop.put("defaultDisabled", "false");
				prop.put("globalDisabled", "false");
				prop.store(out, "PvPToggle Config File");
			} catch (IOException ex){
				ex.printStackTrace();
			}
		} else {
			try {
				loadProcedure();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		setupPermissions();
		
		getCommand("pvp").setExecutor(new pvpPluginCommand(this));
		getCommand("gpvp").setExecutor(new globalpvpPluginCommand(this));
		
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.EntityListener, Event.Priority.Normal, this);
		
		registerOnlinePlayers(this.getServer().getOnlinePlayers());
		
		System.out.println("["+ pdfFile.getName() + "] Enabled!");
	}
	
	private void registerOnlinePlayers(Player[] onlinePlayers) {
		for (Player player : onlinePlayers){
			if (PvPToggle.defaultdisabled){
				this.pvpDisable(player);
			} else {
				this.pvpEnable(player);
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
		FileInputStream in = new FileInputStream(configfile);
		prop.load(in);
		defaultdisabled = Boolean.parseBoolean(prop.getProperty("defaultDisabled", "false"));
		globaldisabled = Boolean.parseBoolean(prop.getProperty("globalDisabled", "false"));
	}

	public void onDisable(){
		log.info("PvPToggle Disabled");
	}
	
	public void pvpEnable(Player player) {
		players.put(player, true);		
	}

	public void pvpDisable(Player player) {
		players.put(player, false);		
	}

	public boolean pvpEnabled(Player player) {
		
		return players.get(player);
	}

	public boolean gpvpEnabled() {
		if (globaldisabled == false){
			return true;
		} else {
			return false;
		}
	}

	public void gpvpEnable() {
		globaldisabled = false;
	}

	public void gpvpDisable() {
		globaldisabled = true;
	}
	
}