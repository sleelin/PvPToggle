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

		if (checkArgs(args[0])){
			Player player = (Player) sender;
			
			if (args.length == 0){
				sendUsage(sender);
				return true;
			}
			if (checkArgs(args[0])){
				if (args.length == 1){
					
					if (args[0].equalsIgnoreCase("status")){
						if (plugin.permissionsCheck((Player) sender, "pvptoggle.command.status")){					
							if (plugin.pvpEnabled(player, player.getWorld().getName())){
								sender.sendMessage(ChatColor.GOLD + "PvP Status in " + player.getWorld().getName() + ": on");
							} else {
								sender.sendMessage(ChatColor.GOLD + "PvP Status in " + player.getWorld().getName() + ": off");
							}
						} else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
						}
						return true;
					}
					
					if (plugin.permissionsCheck(player, "pvptoggle.command.toggle")){
						boolean newval = checkNewValue(args[0]);
						if (newval){
							plugin.pvpEnable(player, player.getWorld().getName());
							player.sendMessage(ChatColor.GOLD + "PvP Enabled in " + player.getWorld().getName() + "!");
							plugin.log.info("[PvPToggle] Player " + player.getDisplayName() + " enabled pvp");
						} else if (!(newval)){
							plugin.pvpDisable(player, player.getWorld().getName());
							player.sendMessage(ChatColor.GOLD + "PvP Disabled in " + player.getWorld().getName() + "!");
							plugin.log.info("[PvPToggle] Player " + player.getDisplayName() + " disabled pvp");
						}
						return true;
					} else {
						player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
					}
				}
				
				if (args.length == 2){	
					if (plugin.permissionsCheck(player, "pvptoggle.admin")){
						if (args[0].equalsIgnoreCase("status")){
							checkPlayerStatus(sender, args[0], player.getWorld().getName());
						} else {
							if (args[1].equalsIgnoreCase("*")){
								toggleAcrossAllWorlds(sender, checkNewValue(args[0]));
							} else {
								toggleSpecificPlayer(sender, args[1], checkNewValue(args[0]), ((Player) sender).getWorld().getName());
							}
						}
						return true;
					} else {
						player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
					}
				}
					
				if (args.length == 3){
					if (plugin.permissionsCheck(player, "pvptoggle.admin")){
						if (args[1].equalsIgnoreCase("*")){
							toggleSpecificWorld(sender, isWorld(args[2]), checkNewValue(args[0]));
						} else {
							toggleSpecificPlayer(sender, args[1], checkNewValue(args[0]), isWorld(args[2]));
						}
					} else {
						player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
					}
				}
			} else {
				sendUsage(sender);
			}
		} else {
			sendUsage(sender);
		}
		return true;
	}
	
	private void sendUsage(CommandSender sender) {
		if ((plugin.permissionsCheck((Player) sender, "pvptoggle.command.status"))&&
				(plugin.permissionsCheck((Player) sender, "pvptoggle.admin"))){
			sender.sendMessage("Usage: /pvp [on|off|status] [player] [world]");
		} else if (plugin.permissionsCheck((Player) sender, "pvptoggle.admin")){
			sender.sendMessage("Usage: /pvp [on|off] [player] [world]");
		} else if (plugin.permissionsCheck((Player) sender, "pvptoggle.command.toggle")){
			sender.sendMessage("Usage: /pvp [on|off]");
		} else if ((plugin.permissionsCheck((Player) sender, "pvptoggle.command.toggle"))&&
				(plugin.permissionsCheck((Player) sender, "pvptoggle.command.status"))){
			sender.sendMessage("Usage: /pvp [on|off|status]");
		}
	}
	
	static boolean checkArgs(String argument){
		boolean valid = false;
		if ((argument.equalsIgnoreCase("on"))||(argument.equalsIgnoreCase("enable"))){
			valid = true;
		}
		if ((argument.equalsIgnoreCase("off"))||(argument.equalsIgnoreCase("disable"))){
			valid = true;
		}
		if (argument.equalsIgnoreCase("status")){
			valid = true;
		}
		return valid;
	}
		
	
	private void checkPlayerStatus(CommandSender sender, String playername, String targetworld) {
		if (targetworld != null){
			List<Player> found = findPlayers(plugin.getServer().getOnlinePlayers(), playername);
			
			if (found.size() == 1){
				Player target = found.get(0);
				if (plugin.pvpEnabled(target, targetworld)){
					sender.sendMessage(ChatColor.GOLD + target.getDisplayName() + " has PvP on in " + targetworld);
				} else {
					sender.sendMessage(ChatColor.GOLD + target.getDisplayName() + " has PvP off in " + targetworld);
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
			sender.sendMessage(ChatColor.RED + "No world matching that name!");
		}
	}

	public static boolean checkNewValue(String string){
		boolean enable = false;
		if ((string.equalsIgnoreCase("on"))||(string.equalsIgnoreCase("enable"))){
			enable = true;
		} else if ((string.equalsIgnoreCase("off"))||(string.equalsIgnoreCase("disable"))){
			enable = false;
		}
		return enable;
	}

	private void toggleAcrossAllWorlds(CommandSender sender, boolean newval) {
		Player players[] = plugin.getServer().getOnlinePlayers();

		for (Player p : players){
			for (String worldname : PvPToggle.worldnames){
				if (newval){
					plugin.pvpEnable(p, worldname);
					if (p.getWorld().getName()==worldname){
						p.sendMessage(ChatColor.GOLD + "PvP Enabled by " + ((Player) sender).getDisplayName() + "!");
					}
				} else {
					plugin.pvpDisable(p, worldname);
					if (p.getWorld().getName()==worldname){
						p.sendMessage(ChatColor.GOLD + "PvP Disabled by " + ((Player) sender).getDisplayName() + "!");
					}
				}
			}
		}
		
		String message = null;
		if (newval){
			message = "Successfully reset all players PvP to enabled across all worlds!";
		} else {
			message = "Successfully reset all players PvP to disabled across all worlds!";
		}
		sender.sendMessage(message);
	}
	
	public static String isWorld(String targetworld){
		String output = null;
		for (String worldname : PvPToggle.worldnames){
			if (worldname.contains(targetworld.toLowerCase())){
				output = worldname;
				break;
			}
		}
		if (output == null){
			
		}
		return output;
	}
	
	
	private void toggleSpecificWorld(CommandSender sender, String targetworld, boolean newval){
		
		Player players[] = plugin.getServer().getOnlinePlayers();
		
		if (targetworld != null){		
			for (Player p : players){
				if (newval){
					plugin.pvpEnable(p, targetworld);
					if (p.getWorld().getName().equals(targetworld)){
						p.sendMessage(ChatColor.GOLD + "PvP Enabled in world " + targetworld + " by " + ((Player) sender).getDisplayName() + "!");
					}
				} else {
					plugin.pvpDisable(p, targetworld);
					if (p.getWorld().getName().equals(targetworld)){
						p.sendMessage(ChatColor.GOLD + "PvP Disabled in world " + targetworld + " by " + ((Player) sender).getDisplayName() + "!");
					}
				}	
			}
			String message;
			if (newval){
				message = "Successfully reset all players PvP to enabled in world " + targetworld;
			} else {
				message = "Successfully reset all players PvP to disabled in world " + targetworld;
			}
			sender.sendMessage(message);
		} else {
			sender.sendMessage(ChatColor.RED + "No world matching that name!");
		}
		
	}
	
	private List<Player> findPlayers(Player players[], String playername){
		List<Player> found = new ArrayList<Player>();
		
		for (Player search : players){
			if (search.getDisplayName().toLowerCase().contains(playername.toLowerCase())){
				found.add(search);
			}
		}
		return found;
	}
	
	private void toggleSpecificPlayer(CommandSender sender, String playername, boolean newval, String targetworld){
		
		if (targetworld != null){
			List<Player> found = findPlayers(plugin.getServer().getOnlinePlayers(), playername);
			if (found.size() == 1){
				Player target = found.get(0);
				if (newval){
					plugin.pvpEnable(target, targetworld);
					target.sendMessage(ChatColor.GOLD + "PvP Enabled in world " + targetworld + " by " + ((Player) sender).getDisplayName() + "!");
					sender.sendMessage(ChatColor.GOLD + "Successfully enabled PvP for player " + target.getDisplayName() + "!");
				} else {
					plugin.pvpDisable(target, targetworld);
					target.sendMessage(ChatColor.GOLD + "PvP Disabled in world " + targetworld + " by " + ((Player) sender).getDisplayName() + "!");
					sender.sendMessage(ChatColor.GOLD + "Successfully disabled PvP for player " + target.getDisplayName() + "!");
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
			sender.sendMessage(ChatColor.RED + "No world matching that name!");
		}
	}
}
