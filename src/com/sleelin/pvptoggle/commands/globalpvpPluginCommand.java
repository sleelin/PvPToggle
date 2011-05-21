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
			
			if (plugin.permissionsCheck((Player) sender, "pvptoggle.gadmin")){
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
			
			if (plugin.permissionsCheck((Player) sender, "pvptoggle.gadmin")){
				gtoggleWorld(sender, pvpPluginCommand.isWorld(args[1]), pvpPluginCommand.checkNewValue(args[0]));			
			} else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			}
			return true;
		}
		
		sendUsage(sender);
		return true;
	}

	private void printWorldStatus(CommandSender sender, String world) {
		String worldname = pvpPluginCommand.isWorld(world);
		if (worldname != null){
			if (plugin.getWorldValue(worldname)){
				sender.sendMessage(ChatColor.GOLD + "PvP Status in world " + worldname + ": on");
			} else {
				sender.sendMessage(ChatColor.GOLD + "PvP Status in world " + worldname + ": off");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "No world matching name \"" + worldname + "\"");
		}
	}

	private void sendUsage(CommandSender sender) {
		sender.sendMessage("Usage: /gpvp [on|off|status] [world]");		
	}
	
	private void gtoggleGlobal(CommandSender sender, boolean newval) {
		Player players[] = plugin.getServer().getOnlinePlayers();

		String displayname = null;
		if (!(sender instanceof Player)){
			displayname = "Console User";
		} else {
			displayname = ((Player) sender).getDisplayName();
		}
		
		for (Player p : players){
			for (String worldname : PvPToggle.worldnames){
				if (newval){
					plugin.pvpEnable(p, worldname);
					p.sendMessage(ChatColor.GOLD + "Global PvP enabled by " + displayname + "!");
				} else {
					plugin.pvpDisable(p, worldname);
					p.sendMessage(ChatColor.GOLD + "Global PvP disabled by " + displayname + "!");
				}
			}
			
		}
		for (String worldname : PvPToggle.worldnames){
			plugin.setWorldValue(worldname, newval);
		}
		
		String message = null;
		if (newval){
			message = "Successfully enabled global PvP!";
		} else {
			message = "Successfully disabled global PvP!";
		}
		sender.sendMessage(message);
	}
	
	private void gtoggleWorld(CommandSender sender, String targetworld, boolean newval){
		
		Player players[] = plugin.getServer().getOnlinePlayers();
		
		if (targetworld != null){		
			for (Player p : players){
				if (newval){
					plugin.pvpEnable(p, targetworld);
					p.sendMessage(ChatColor.GOLD + "World-wide PvP enabled in world " + targetworld + " by " + ((Player) sender).getDisplayName() + "!");
				} else {
					plugin.pvpDisable(p, targetworld);
					p.sendMessage(ChatColor.GOLD + "World-wide PvP disabled in world " + targetworld + " by " + ((Player) sender).getDisplayName() + "!");
				}	
			}
			plugin.setWorldValue(targetworld, newval);
			String message;
			if (newval){
				message = "Successfully enabled world-wide PvP in " + targetworld;
			} else {
				message = "Successfully enabled world-wide PvP in " + targetworld;
			}
			sender.sendMessage(message);
		} else {
			sender.sendMessage(ChatColor.RED + "No world matching name \"" + targetworld + "\"");
		}
		
	}

}
