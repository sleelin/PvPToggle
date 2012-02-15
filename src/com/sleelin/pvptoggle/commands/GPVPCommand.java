package com.sleelin.pvptoggle.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.sleelin.pvptoggle.PvPToggle;

public class GPVPCommand implements CommandExecutor {
	private final PvPToggle plugin;
	
	public GPVPCommand(PvPToggle instance){
		plugin = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (args.length == 0){
			// if no arguments, print usage
			sendUsage(sender);
			return true;
		}
		
		if ((args[0].equalsIgnoreCase("on"))||(args[0].equalsIgnoreCase("enable"))||(args[0].equalsIgnoreCase("off"))||(args[0].equalsIgnoreCase("disable"))){
			switch (args.length){
			case 1:
				// toggle global status
				gtoggleGlobal(sender, PVPCommand.checkNewValue(args[0]));
				break;
			case 2:
				// toggle specific world status
				gtoggleWorld(sender, PVPCommand.isWorld(sender, args[1]), PVPCommand.checkNewValue(args[0]));
				break;
			}
		} else if (args[0].equalsIgnoreCase("status")){
			switch (args.length){
			case 1:
				// check global status
				globalStatus(sender);
				break;
			case 2:
				// check world status
				worldStatus(sender, PVPCommand.isWorld(sender, args[1]));
				break;
			}
		}
		
		return true;
	}
	
	/**
	 * Prints global PvP status to chat/console
	 * @param sender - command sender
	 */
	private void globalStatus(CommandSender sender){
		if (plugin.permissionsCheck(sender, "pvptoggle.gcommand.status", true)){
			
			if (plugin.gpvpEnabled()){
				sender.sendMessage(ChatColor.GOLD + "Global PvP Status: on");
			} else {
				sender.sendMessage(ChatColor.GOLD + "Global PvP Status: off");
			}
			
		} else {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
		}
		
	}

	/**
	 *  
	 * @param sender
	 * @param world
	 */
	private void worldStatus(CommandSender sender, String worldname) {
		if (plugin.permissionsCheck(sender, "pvptoggle.gcommand.status", true)){
			
			if (worldname != null){
				if (PvPToggle.worldstatus.get(worldname)){
					sender.sendMessage(ChatColor.GOLD + "PvP Status in world " + worldname + ": on");
				} else {
					sender.sendMessage(ChatColor.GOLD + "PvP Status in world " + worldname + ": off");
				}
			}
			
		} else {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
		}

	}

	private void sendUsage(CommandSender sender) {
		if ((plugin.permissionsCheck(sender, "pvptoggle.gadmin", true))||(plugin.permissionsCheck(sender, "pvptoggle.gcommand.admin", true))){
			sender.sendMessage("Usage: /gpvp [on|off|status] [world]");
		} else if (plugin.permissionsCheck(sender, "pvptoggle.gcommand.status", true)){
			sender.sendMessage("Usage: /gpvp [status] [world]");
		}
	}
	
	private void gtoggleGlobal(CommandSender sender, boolean newval) {
		if ((plugin.permissionsCheck(sender, "pvptoggle.gcommand.toggle", true))||(plugin.permissionsCheck(sender, "pvptoggle.gcommand.admin", true))){
			plugin.gpvpToggle(newval);
			String message = null;
			if (newval){
				message = ChatColor.GOLD + "Successfully enabled global PvP!";
			} else {
				message = ChatColor.GOLD +  "Successfully disabled global PvP!";
			}
			sender.sendMessage(message);			
		}
	}
	
	private void gtoggleWorld(CommandSender sender, String targetworld, boolean newval){
		if (plugin.permissionsCheck(sender, "pvptoggle.gcommand.admin", true)){
			if (targetworld != null){
				plugin.setWorldStatus(targetworld, newval);
				String message;
				if (newval){
					message = ChatColor.GOLD +  "Successfully enabled world-wide PvP in " + targetworld;
				} else {
					message = ChatColor.GOLD + "Successfully disabled world-wide PvP in " + targetworld;
				}
				sender.sendMessage(message);
			} else {
				sender.sendMessage(ChatColor.RED + "No world matching that name!");
			}
		}
	}

}
