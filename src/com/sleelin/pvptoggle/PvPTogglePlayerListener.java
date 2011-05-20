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
		if ((PvPToggle.defaultdisabled)||(PvPToggle.globaldisabled)){
			plugin.pvpDisable(player);
		} else {
			plugin.pvpEnable(player);
		}
	}

}
