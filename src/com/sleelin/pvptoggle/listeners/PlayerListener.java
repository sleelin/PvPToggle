package com.sleelin.pvptoggle.listeners;

import java.util.GregorianCalendar;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.sleelin.pvptoggle.PvPToggle;

public class PlayerListener implements Listener {

	public static PvPToggle plugin;
	
	public PlayerListener(PvPToggle instance) {
		plugin = instance;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
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
			PvPToggle.lastpvp.put(player, new GregorianCalendar().getTime().getTime()-(1000*PvPToggle.cooldown));
		}
	}

}
