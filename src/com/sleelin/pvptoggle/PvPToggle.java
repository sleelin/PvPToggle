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
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * PvPToggle
 * 
 * @author sleelin
 *
 */

public class PvPToggle extends JavaPlugin {
	static String mainDirectory = "plugins/PvPToggle";
	static Properties prop = new Properties();
	static File configfile = new File (mainDirectory + File.separator + "config.yml");
	static boolean defaultenabled;
	
	public static PermissionHandler permissionHandler;
	
	private final PvPTogglePlayerListener playerListener = new PvPTogglePlayerListener(this);
	private final PvPToggleEntityListener EntityListener = new PvPToggleEntityListener(this);
	public final HashMap<Player, Boolean> players = new HashMap<Player, Boolean>();
	Logger log = Logger.getLogger("Minecraft");
	
	public void onEnable(){
		new File(mainDirectory).mkdir();
		if (!configfile.exists()){
			try {
				configfile.createNewFile();
				FileOutputStream out = new FileOutputStream(configfile);
				prop.put("PvPEnabledOnStartup", "false");
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
		
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_LOGIN, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.EntityListener, Event.Priority.Normal, this);
		log.info("PvPToggle Enabled");
	}
	
	private void setupPermissions(){
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
		
		if (PvPToggle.permissionHandler == null){
			if (permissionsPlugin != null){
				PvPToggle.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
			} else {
				log.info("Permissions system not detected, defaulting to OP");
			}
		}
	}
	
	public void loadProcedure() throws IOException {
		FileInputStream in = new FileInputStream(configfile);
		prop.load(in);
		defaultenabled = Boolean.parseBoolean(prop.getProperty("PvPEnabledOnStartup", "false"));
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
	
}
