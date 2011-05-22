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
		for (String worldname : PvPToggle.worldnames){
			if ((PvPToggle.defaultdisabled)||(PvPToggle.globaldisabled)||(!(plugin.getWorldValue(worldname)))){
				plugin.pvpDisable(player, worldname);
			} else {
				plugin.pvpEnable(player, worldname);
			}			
		}
	}

}
