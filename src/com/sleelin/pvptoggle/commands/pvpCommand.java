package com.sleelin.pvptoggle.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sleelin.pvptoggle.PvPToggle;

public class pvpCommand implements CommandExecutor {
	private final PvPToggle plugin;
	
	public pvpCommand(PvPToggle instance){
		plugin = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		Player player = (Player) sender;
		boolean haspermissions = false;
		player.sendMessage("Trying command");
		if (PvPToggle.permissionHandler != null){
			if (PvPToggle.permissionHandler.has(player, "pvptoggle")){
				haspermissions = true;
			}
		} else if (player.isOp()){
			haspermissions = true;
		}
		
		if (haspermissions){
			if ((args[0].equalsIgnoreCase("on"))||(args[0].equalsIgnoreCase("enable"))){
				plugin.pvpEnable(player);
				player.sendMessage(ChatColor.GOLD + "PvP Enabled!");
				plugin.log.info("Player enabled pvp");
			} else if ((args[0].equalsIgnoreCase("off"))||(args[0].equalsIgnoreCase("disable"))){
				plugin.pvpDisable(player);
				player.sendMessage(ChatColor.GOLD + "PvP Disabled!");
				plugin.log.info("Player disabled pvp");
			}
		}
		return haspermissions;
	}

	
	
}
