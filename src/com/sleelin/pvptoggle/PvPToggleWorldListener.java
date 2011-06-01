package com.sleelin.pvptoggle;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.config.Configuration;

public class PvPToggleWorldListener extends WorldListener {

	public static PvPToggle plugin;
	
	public PvPToggleWorldListener(PvPToggle instance) {
		plugin = instance;
	}
	
	public void onWorldLoad(WorldLoadEvent event){
		PluginDescriptionFile pdfFile = plugin.getDescription();
		
		Configuration config = new Configuration(PvPToggle.configfile);
		PvPToggle.worldnames.add(event.getWorld().getName());
		PvPToggle.worldstatus.put(event.getWorld().getName(), config.getBoolean("worlds."+event.getWorld().getName()+".pvpenabled",true));
		PvPToggle.defaultenabled.put(event.getWorld().getName(), config.getBoolean("worlds."+event.getWorld().getName()+".logindefault",true));
		HashMap<Player, Boolean> players = new HashMap<Player, Boolean>();
		PvPToggle.worlds.add(players); 
		for (Player player : plugin.getServer().getOnlinePlayers()){
			if (PvPToggle.defaultenabled.get(event.getWorld().getName())){
				plugin.pvpDisable(player, event.getWorld().getName());
			} else {
				plugin.pvpEnable(player, event.getWorld().getName());
			}
		}

		plugin.log.info("[" +pdfFile.getName() + "] found and loaded world " + event.getWorld().getName().toString());
		
	}
	
}
