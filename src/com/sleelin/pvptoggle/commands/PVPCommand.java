package com.sleelin.pvptoggle.commands;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sleelin.pvptoggle.PvPToggle;

public class PVPCommand implements CommandExecutor {
	private final PvPToggle plugin;
	
	public PVPCommand(PvPToggle instance){
		plugin = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){

		Player player = null;
		
		if (args.length == 0){
			// if no arguments, send usage
			sendUsage(sender);
			return true;
		}
		
		if (sender instanceof Player){
			player = (Player) sender;
		}
		
		if ((args[0].equalsIgnoreCase("on"))||(args[0].equalsIgnoreCase("enable"))||(args[0].equalsIgnoreCase("off"))||(args[0].equalsIgnoreCase("disable"))){
			switch (args.length){
			case 1:
				// toggle command sender
				if (player != null){
					togglePlayer(sender, player.getName(), checkNewValue(args[0]), player.getWorld().getName());
				} else {
					sender.sendMessage(ChatColor.RED + "Cannot run this command from the console!");
				}
				break;
			case 2:
				if (args[1].equalsIgnoreCase("*")){
					// toggle all players across all worlds
					toggleWorld(sender, "*", checkNewValue(args[0]), "*");
				} else {
					// toggle specific player in their current world
					Player retrieved = getPlayer(sender, args[1]);
					if (retrieved != null){
						togglePlayer(sender, args[1], checkNewValue(args[0]), retrieved.getWorld().getName());
					}
				}
				break;
			case 3:
				if ((args[1].equalsIgnoreCase("*"))||(args[2].equalsIgnoreCase("*"))){
					// also toggle all players across all worlds
					toggleWorld(sender, "*", checkNewValue(args[0]), "*");
				} else if (args[2].equalsIgnoreCase("*")){
					// toggle specific player across all worlds
					toggleWorld(sender, args[1], checkNewValue(args[0]), "*");
				} else {
					// toggle specific player in specific world
					togglePlayer(sender, args[1], checkNewValue(args[0]), isWorld(sender, args[2]));
				}
				break;
			}
			return true;
		} else if (args[0].equalsIgnoreCase("status")){
			switch (args.length){
			case 1:
				// get command sender's status
				if (player != null){
					getStatus(sender, player.getName(), player.getWorld().getName());
				} else {
					sender.sendMessage(ChatColor.RED + "Cannot run this command fromt he console!");
				}
				break;
			case 2:
				// get player's status in sender's world
				Player retrieved = getPlayer(sender, args[1]);
				if (retrieved != null){
					getStatus(sender, args[1], retrieved.getWorld().getName());
				}
				break;
			case 3:
				// get player's status in specified world
				getStatus(sender, args[1], isWorld(sender, args[2]));
				break;
			}
			return true;
		}
		
		return true;
	}

	/**
	 * Prints target player's status to chat/console
	 * @param sender - command sender
	 * @param target - who to check for
	 * @param world - what world to check in
	 */
	private void getStatus(CommandSender sender, String target, String world) {
		Player player = getPlayer(sender, target);
		
		// check for permission to view own or other player's status
		if (!(((plugin.permissionsCheck(sender, "pvptoggle.command.status", true))&&(sender.getName().equalsIgnoreCase(target))) 
				|| (plugin.permissionsCheck(sender, "pvptoggle.command.admin", true)))){
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return;
		}
		if ((target != null)&&(world != null)){
			if (sender.getName().equalsIgnoreCase(target)){
				// report personal PvP status
				if (plugin.permissionsCheck(player, "pvptoggle.pvp.force", false)){
					sender.sendMessage(ChatColor.GOLD + "PvP Status in " + world + ": forced");
				} else if (plugin.pvpEnabled(player, player.getWorld().getName())){
					sender.sendMessage(ChatColor.GOLD + "PvP Status in " + world + ": on");
				} else {
					sender.sendMessage(ChatColor.GOLD + "PvP Status in " + world + ": off");
				}
			} else {
				// report someone else's PvP status
				if (plugin.permissionsCheck(player, "pvptoggle.pvp.force", false)){
					sender.sendMessage(ChatColor.GOLD + player.getDisplayName() + " is forced to PvP in " + world);
				} else if (plugin.pvpEnabled(player, world)){
					sender.sendMessage(ChatColor.GOLD + player.getDisplayName() + " has PvP on in " + world);
				} else {
					sender.sendMessage(ChatColor.GOLD + player.getDisplayName() + " has PvP off in " + world);
				}
			}
		}
	}

	/**
	 * Gets player whose name partially matches the input string
	 * @param sender - command sender
	 * @param player - input string
	 * @return player whose name partially matches input string 
	 */
	private Player getPlayer(CommandSender sender, String player) {
		
		List<Player> found = new ArrayList<Player>();	// list of found players
		Player[] players = plugin.getServer().getOnlinePlayers();	// list of online players
		
		// look through players to see if any of them match
		for (Player search : players){
			if ((search.getDisplayName().toLowerCase().contains(player.toLowerCase())||(search.getName().toLowerCase().contains(player.toLowerCase())))){
				found.add(search);
			}
		}
		
		if (found.size() == 1){
			return found.get(0);	// return found player
		} else if (found.size() > 1) {
			// found multiple matches
			sender.sendMessage("Found " + found.size() + " online players matching that partial name:");
			for (Player p : found){
				sender.sendMessage("- "+p.getDisplayName());
			}
		} else {
			// found no matches
			sender.sendMessage(ChatColor.RED + "Couldn't find a player matching that name!");
		}
		
		return null;
	}
	

	/**
	 * Toggle the PvP status of a player
	 * @param sender - command sender
	 * @param targetplayer - name of player to toggle
	 * @param newval - new PvP status of player
	 * @param world - world to toggle in
	 */
	private void togglePlayer(CommandSender sender, String targetplayer, boolean newval, String world) {
		
		// check for permission to view own or other player's status
		if (!(((plugin.permissionsCheck(sender, "pvptoggle.command.toggle", true))&&(sender.getName().equalsIgnoreCase(targetplayer))) 
				|| (plugin.permissionsCheck(sender, "pvptoggle.command.admin", true)))){
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return;	// no permission, return out
		}
		
		Player player = getPlayer(sender, targetplayer);	// get target player
		
		if ((player == null)||(world == null)){
			// target not found, return out
			return;
		}
		
		if (sender.getName().equalsIgnoreCase(targetplayer)){
			// if sender is the target
			if (newval){
				// if enabling
				plugin.pvpEnable(player, player.getWorld().getName());
				player.sendMessage(ChatColor.GOLD + "PvP Enabled in " + player.getWorld().getName() + "!");
				plugin.log.info("[PvPToggle] Player " + player.getDisplayName() + " enabled pvp");
				PvPToggle.lasttoggle.put(player, new GregorianCalendar().getTime().getTime());
			} else if (!(newval)){
				// if disabling
				if (checkCooldown(player)){
					plugin.pvpDisable(player, player.getWorld().getName());
					player.sendMessage(ChatColor.GOLD + "PvP Disabled in " + player.getWorld().getName() + "!");
					plugin.log.info("[PvPToggle] Player " + player.getDisplayName() + " disabled pvp");
				} else {
					player.sendMessage(ChatColor.RED + "You were just in combat, can't disable PvP yet!");
				}
			}
		} else {
			// if the target is another player
			if (newval){
				// if enabling
				plugin.pvpEnable(player, world);
				player.sendMessage(ChatColor.GOLD + "PvP Enabled in world " + world + " by " + sender.getName() + "!");
				sender.sendMessage(ChatColor.GOLD + "Successfully enabled PvP for player " + player.getName() + "!");
			} else {
				// if disabling
				plugin.pvpDisable(player, world);
				player.sendMessage(ChatColor.GOLD + "PvP Disabled in world " + world + " by " + sender.getName() + "!");
				sender.sendMessage(ChatColor.GOLD + "Successfully disabled PvP for player " + player.getName() + "!");
			}			
		}
	}

	private void toggleWorld(CommandSender sender, String player, boolean newval, String world) {
		
		Player players[] = plugin.getServer().getOnlinePlayers();	// get list of online players
		String message;
		
		// set preliminary message
		if (newval){
			message = "enabled";
		} else {
			message = "disabled";
		}
		
		if ((player.equalsIgnoreCase("*")) && (world.equalsIgnoreCase("*"))){
			// if globally toggling
			for (String worldname : PvPToggle.worldnames){
				for (Player p : players){
					togglePlayer(sender, p.getName(), newval, worldname);
				}
			}
			message = "Successfully reset all players PvP to " + message + " across all worlds";
		} else if (player.equalsIgnoreCase("*")){
			// if toggling all players in specific world
			for (Player p : players){
				togglePlayer(sender, p.getName(), newval, world);
			}
			message = "Successfully reset all players PvP to " + message + " in world " + world;
		} else if (world.equalsIgnoreCase("*")){
			// if toggling specific player in all worlds
			for (String worldname : PvPToggle.worldnames){
				togglePlayer(sender, player, newval, worldname);
			}
			message = "Successfully reset " + getPlayer(sender, player).getName() + "'s PvP to " + message + " across all worlds";			
		}	
		sender.sendMessage(ChatColor.GOLD + message);
	}
	
	/**
	 * Checks whether or not it has been longer than the specified cooldown period since last player PvP 
	 * @param player - whose cooldown to check
	 * @return boolean true for wait over, false for still waiting
	 */
	private boolean checkCooldown(Player player) {
		GregorianCalendar cal = new GregorianCalendar();
		Long difference = cal.getTime().getTime() - PvPToggle.lastpvp.get(player);
		int before = difference.compareTo(((long) PvPToggle.cooldown) * 1000);
		if (before>=0){
			return true;
		}
		return false;
	}

	/**
	 * Print command usage to screen/chat
	 * @param sender - command sender
	 */
	private void sendUsage(CommandSender sender) {
		if (sender instanceof Player){
			if ((plugin.permissionsCheck(sender, "pvptoggle.command.status", true))&&
					((plugin.permissionsCheck(sender, "pvptoggle.admin", true))||(plugin.permissionsCheck(sender, "pvptoggle.command.admin", true)))){
				sender.sendMessage("Usage: /pvp [on|off|status] [player] [world]");
			} else if ((plugin.permissionsCheck(sender, "pvptoggle.admin", true))||(plugin.permissionsCheck(sender, "pvptoggle.command.admin", true))){
				sender.sendMessage("Usage: /pvp [on|off] [player] [world]");
			} else if (plugin.permissionsCheck(sender, "pvptoggle.command.toggle", true)){
				sender.sendMessage("Usage: /pvp [on|off]");
			} else if ((plugin.permissionsCheck(sender, "pvptoggle.command.toggle", true))&&
					(plugin.permissionsCheck(sender, "pvptoggle.command.status", true))){
				sender.sendMessage("Usage: /pvp [on|off|status]");
			}
		} else {
			sender.sendMessage("Usage: pvp [on|off|status] [player] [world]");
		}
	}

	/**
	 * Converts string enable/disable parameters to booleans
	 * @param string - string enable/disable input parameter
	 * @return boolean true for on, false for off
	 */
	public static boolean checkNewValue(String string){
		boolean enable = false;
		if ((string.equalsIgnoreCase("on"))||(string.equalsIgnoreCase("enable"))){
			enable = true;
		} else if ((string.equalsIgnoreCase("off"))||(string.equalsIgnoreCase("disable"))){
			enable = false;
		}
		return enable;
	}
		
	/**
	 * Completes partial worldname
	 * @param targetworld
	 * @return
	 */
	public static String isWorld(CommandSender sender, String targetworld){
		String output = null;
		for (String worldname : PvPToggle.worldnames){
			if (worldname.contains(targetworld.toLowerCase())){
				output = worldname;
				break;
			}
		}
		if (output == null) sender.sendMessage(ChatColor.RED + "No world matching that name!");
		return output;
	}
	
}
