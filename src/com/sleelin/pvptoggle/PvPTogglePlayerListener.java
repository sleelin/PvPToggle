package com.sleelin.pvptoggle;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;



public class PvPTogglePlayerListener extends PlayerListener {

	public static PvPToggle plugin;
	
	public PvPTogglePlayerListener(PvPToggle instance) {
		plugin = instance;
	}
	
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = (Player) event.getPlayer();
		if (!plugin.alreadyLoaded(player)){
			for (String worldname : PvPToggle.worldnames){
				if (!PvPToggle.defaultenabled.get(worldname)){
					plugin.pvpDisable(player, worldname);
				} else {
					plugin.pvpEnable(player, worldname);
				}
			}
		}
	}

}
