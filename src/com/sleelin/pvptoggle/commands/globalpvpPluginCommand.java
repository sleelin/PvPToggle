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
		
		if (args.length == 0){
			sender.sendMessage("Usage: /gpvp [on|off|status]");
			return true;
		}

		if (args[0].equalsIgnoreCase("status")){
			if (plugin.gpvpEnabled()){
				sender.sendMessage(ChatColor.GOLD + "Global PvP Status: on");
			} else {
				sender.sendMessage(ChatColor.GOLD + "Global PvP Status: off");
			}
			return true;
		}
		
		if (args.length == 1){
			boolean haspermissions = false;
			if (sender instanceof Player){
				if (PvPToggle.permissionHandler != null){
					if (PvPToggle.permissionHandler.has((Player) sender, "pvptoggle.gadmin")){
						haspermissions = true;
					}
				}
			}
			if (sender.isOp()){
				haspermissions = true;
			}
			
			if (haspermissions){
				Player players[] = plugin.getServer().getOnlinePlayers();
				boolean enable = false;
				if ((args[1].equalsIgnoreCase("on"))||(args[1].equalsIgnoreCase("enable"))){
					enable = true;
					plugin.gpvpEnable();
				} else if ((args[1].equalsIgnoreCase("off"))||(args[1].equalsIgnoreCase("disable"))){
					enable = false;
					plugin.gpvpDisable();
				}
				String displayname = null;
				if (!(sender instanceof Player)){
					displayname = "Console User";
				} else {
					displayname = ((Player) sender).getDisplayName();
				}
				
				
				for (Player p : players){
					if (enable){
						plugin.pvpEnable(p);
						p.sendMessage(ChatColor.GOLD + "Global PvP Enabled by " + displayname + "!");
					} else {
						plugin.pvpDisable(p);
						p.sendMessage(ChatColor.GOLD + "Global PvP Disabled by " + displayname + "!");
					}
				}
				if (enable){
					sender.sendMessage("Successfully enabled Global PvP!");
				} else {
					sender.sendMessage("Successfully disabled Global PvP!");
				}
				
			} else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			}
		}		
		
		return true;
	}

}
