package com.sleelin.pvptoggle.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sleelin.pvptoggle.PvPToggle;

public class pvpPluginCommand implements CommandExecutor {
	private final PvPToggle plugin;
	
	public pvpPluginCommand(PvPToggle instance){
		plugin = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "This command cannot be run from the console!");
			return true;
		}

		Player player = (Player) sender;
		
		if (args.length == 0){
			sender.sendMessage("Usage: /pvp [on|off|status]");
			return true;
		}

		if (args[0].equalsIgnoreCase("status")){
			if (plugin.pvpEnabled(player)){
				sender.sendMessage(ChatColor.GOLD + "PvP Status: on");
			} else {
				sender.sendMessage(ChatColor.GOLD + "PvP Status: off");
			}
			return true;
		}
		
		if (args.length == 1){
			boolean haspermissions = false;
			if (PvPToggle.permissionHandler != null){
				if (PvPToggle.permissionHandler.has(player, "pvptoggle.command")){
					haspermissions = true;
				}
			}
			if (player.isOp()){
				haspermissions = true;
			}
			
			if (haspermissions){
				if ((args[0].equalsIgnoreCase("on"))||(args[0].equalsIgnoreCase("enable"))){
					plugin.pvpEnable(player);
					player.sendMessage(ChatColor.GOLD + "PvP Enabled!");
					plugin.log.info("[PvPToggle] Player " + player.getDisplayName() + " enabled pvp");
				} else if ((args[0].equalsIgnoreCase("off"))||(args[0].equalsIgnoreCase("disable"))){
					plugin.pvpDisable(player);
					player.sendMessage(ChatColor.GOLD + "PvP Disabled!");
					plugin.log.info("[PvPToggle] Player " + player.getDisplayName() + " disabled pvp");
				}
			} else {
				player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			}
		}
		
		if (args.length == 2){
			boolean haspermissions = false;
			if (PvPToggle.permissionHandler != null){
				if (PvPToggle.permissionHandler.has(player, "pvptoggle.admin")){
					haspermissions = true;
				}
			}
			if (player.isOp()){
				haspermissions = true;
			}	
			
			
			if (haspermissions){
				String playername;
				Player players[] = plugin.getServer().getOnlinePlayers();
				
				if (args[0].equalsIgnoreCase("*")){
					boolean enable = false;
					if ((args[1].equalsIgnoreCase("on"))||(args[1].equalsIgnoreCase("enable"))){
						enable = true;
					} else if ((args[1].equalsIgnoreCase("off"))||(args[1].equalsIgnoreCase("disable"))){
						enable = false;
					}
					for (Player p : players){
						if (enable){
							plugin.pvpEnable(p);
							p.sendMessage(ChatColor.GOLD + "PvP Enabled by " + player.getDisplayName() + "!");
						} else {
							plugin.pvpDisable(p);
							p.sendMessage(ChatColor.GOLD + "PvP Disabled by " + player.getDisplayName() + "!");
						}
					}
					if (enable){
						sender.sendMessage("Successfully reset all players PvP to enabled!");
					} else {
						sender.sendMessage("Successfully reset all players PvP to disabled!");
					}
					return true;
				}
				
				
				List<Player> found = new ArrayList<Player>();
				
				for (Player search : players){
					playername = search.getDisplayName().toLowerCase();
					if (playername.contains(args[0].toLowerCase())){
						found.add(search);
					}
				}
				if (found.size() == 1){
					Player target = found.get(0);
					if ((args[1].equalsIgnoreCase("on"))||(args[1].equalsIgnoreCase("enable"))){
						plugin.pvpEnable(target);
						target.sendMessage(ChatColor.GOLD + "PvP Enabled by " + player.getDisplayName() + "!");
						player.sendMessage(ChatColor.GOLD + "Successfully enabled PvP for player " + target.getDisplayName() + "!");
					} else if ((args[1].equalsIgnoreCase("off"))||(args[1].equalsIgnoreCase("disable"))){
						plugin.pvpDisable(target);
						target.sendMessage(ChatColor.GOLD + "PvP Disabled by " + player.getDisplayName() + "!");
						player.sendMessage(ChatColor.GOLD + "Successfully disabled PvP for player " + target.getDisplayName() + "!");
					} else if (args[1].equalsIgnoreCase("status")){
						if (plugin.pvpEnabled(target)){
							sender.sendMessage(ChatColor.GOLD + target.getDisplayName() + " has PvP: on");
						} else {
							sender.sendMessage(ChatColor.GOLD + target.getDisplayName() + " has PvP: off");
						}
						return true;
					}
				} else if (found.size() > 1) {
					StringBuilder targets = new StringBuilder();
					for (Player p : found){
						targets.append(p.getDisplayName());
						targets.append(", ");
					}
					sender.sendMessage("Found " + found.size() + " online players matching that partial name: " + targets.toString().replaceAll("/, $/", ""));
				} else {
					sender.sendMessage(ChatColor.RED + "Couldn't find a player matching that name!");
				}
			} else {
				player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			}
		}
		
		return true;
	}

	
	
}
