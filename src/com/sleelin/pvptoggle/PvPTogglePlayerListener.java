package com.sleelin.pvptoggle;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.entity.Player;



public class PvPTogglePlayerListener extends PlayerListener {

	public static PvPToggle plugin;
	
	public PvPTogglePlayerListener(PvPToggle instance) {
		plugin = instance;
	}
	
	public void onPlayerCommand(PlayerChatEvent event){
		String[] command = event.getMessage().split(" ");
		Player player = event.getPlayer();
		boolean haspermissions = false;

		if (PvPToggle.permissionHandler != null){
			if (PvPToggle.permissionHandler.has(player, "pvptoggle")){
				haspermissions = true;
			}
		} else if (player.isOp()){
			haspermissions = true;
		}
		
		if (haspermissions){
			if ((command[0].equalsIgnoreCase("/pvp"))&&((command[1].equalsIgnoreCase("on"))||(command[1].equalsIgnoreCase("enable")))){
				plugin.pvpEnable(player);
				player.sendMessage(ChatColor.GOLD + "PvP Enabled!");
			} else if ((command[0].equalsIgnoreCase("/pvp"))&&((command[1].equalsIgnoreCase("off"))||(command[1].equalsIgnoreCase("disable")))){
				plugin.pvpDisable(player);
				player.sendMessage(ChatColor.GOLD + "PvP Disabled!");
			}
			event.setCancelled(true);
		}
	}

}
