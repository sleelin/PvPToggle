package com.sleelin.pvptoggle.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sleelin.pvptoggle.PvPToggle;

public class globalpvpPluginCommand implements CommandExecutor {
	private final PvPToggle plugin;
	
	public globalpvpPluginCommand(PvPToggle instance){
		plugin = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (pvpPluginCommand.checkArgs(args[0])){
			if (args.length == 1){
				if (args[0].equalsIgnoreCase("status")){
					if (plugin.permissionsCheck((Player) sender, "pvptoggle.gcommand.status")){
						if (plugin.gpvpEnabled()){
							sender.sendMessage(ChatColor.GOLD + "Global PvP Status: on");
						} else {
							sender.sendMessage(ChatColor.GOLD + "Global PvP Status: off");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
					}
					return true;
				}
				
				if ((plugin.permissionsCheck((Player) sender, "pvptoggle.gadmin"))||(plugin.permissionsCheck((Player) sender, "pvptoggle.gcommand.admin"))){
					gtoggleGlobal(sender, pvpPluginCommand.checkNewValue(args[0]));			
				} else {
					sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
				}
				return true;
			}
			
			if (args.length == 2){
				if (args[0].equalsIgnoreCase("status")){
					if (plugin.permissionsCheck((Player) sender, "pvptoggle.gcommand.status")){
						printWorldStatus(sender, pvpPluginCommand.isWorld(args[1]));
					} else {
						sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
					}
					return true;
				}
				
				if ((plugin.permissionsCheck((Player) sender, "pvptoggle.gadmin"))||(plugin.permissionsCheck((Player) sender, "pvptoggle.gcommand.admin"))){
					gtoggleWorld(sender, pvpPluginCommand.isWorld(args[1]), pvpPluginCommand.checkNewValue(args[0]));			
				} else {
					sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
				}
				return true;
			}
			
		} else {
			sendUsage(sender);
		}
		return true;
	}

	private void printWorldStatus(CommandSender sender, String world) {
		String worldname = pvpPluginCommand.isWorld(world);
		if (worldname != null){
			if (PvPToggle.worldstatus.get(worldname)){
				sender.sendMessage(ChatColor.GOLD + "PvP Status in world " + worldname + ": on");
			} else {
				sender.sendMessage(ChatColor.GOLD + "PvP Status in world " + worldname + ": off");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "No world matching that name!");
		}
	}

	private void sendUsage(CommandSender sender) {
		if ((plugin.permissionsCheck((Player) sender, "pvptoggle.gadmin"))||(plugin.permissionsCheck((Player) sender, "pvptoggle.gcommand.admin"))){
			sender.sendMessage("Usage: /gpvp [on|off|status] [world]");
		} else if (plugin.permissionsCheck((Player) sender, "pvptoggle.gcommand.status")){
			sender.sendMessage("Usage: /gpvp [status] [world]");
		}
	}
	
	private void gtoggleGlobal(CommandSender sender, boolean newval) {
		plugin.gpvpToggle(newval);
		String message = null;
		if (newval){
			message = "Successfully enabled global PvP!";
		} else {
			message = "Successfully disabled global PvP!";
		}
		sender.sendMessage(message);
	}
	
	private void gtoggleWorld(CommandSender sender, String targetworld, boolean newval){
		
		if (targetworld != null){		
			plugin.setWorldStatus(targetworld, newval);
			String message;
			if (newval){
				message = "Successfully enabled world-wide PvP in " + targetworld;
			} else {
				message = "Successfully disabled world-wide PvP in " + targetworld;
			}
			sender.sendMessage(message);
		} else {
			sender.sendMessage(ChatColor.RED + "No world matching that name!");
		}
		
	}

}
