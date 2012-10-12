package com.sleelin.pvptoggle;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class PvPCommandHandler implements CommandExecutor {
	private final PvPToggle plugin;
	
	public PvPCommandHandler(PvPToggle instance){
		plugin = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){

		Player player = null;

		if (sender instanceof Player){
			player = (Player) sender;
		}
		
		if (args.length == 0){
			// if no arguments, send usage
			if (player != null){
				togglePlayer(sender, player, player.getWorld().getName(), !plugin.checkPlayerStatus(player, player.getWorld().getName()));
			} else {
				getGlobalStatus(sender);
			}
			return true;
		} else if ((args[0].equalsIgnoreCase("on"))||(args[0].equalsIgnoreCase("enable"))||(args[0].equalsIgnoreCase("off"))||(args[0].equalsIgnoreCase("disable"))){
			switch (args.length){
			case 1:
				// toggle command sender
				if (player != null){
					togglePlayer(sender, player, player.getWorld().getName(), checkNewValue(args[0]));
				} else {
					PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.CONSOLE_ERROR);
				}
				break;
			case 2:
				// toggle specific player in their current world
				Player retrieved = getPlayer(sender, args[1], true);
				if (retrieved != null){
					togglePlayer(sender, retrieved, retrieved.getWorld().getName(), checkNewValue(args[0]));
				}
				break;
			case 3:
				// toggle specific player in specific world
				togglePlayer(sender, getPlayer(sender, args[1], true), isWorld(sender, args[2]), checkNewValue(args[0]));
				break;
			default: 
				sendUsage(sender, "toggle");
			}
			return true;
		} else if ((args[0].equalsIgnoreCase("status"))||(args[0].equalsIgnoreCase("s"))){
			switch (args.length){
			case 1:
				// get command sender's status
				if (player != null){
					getPlayerStatus(sender, player, player.getWorld().getName());
				} else {
					getGlobalStatus(sender);	// run from console
				}
				break;
			case 2:
				// get player's status in sender's world
				Player retrieved = getPlayer(sender, args[1], true);
				if (retrieved != null){
					getPlayerStatus(sender, retrieved, retrieved.getWorld().getName());
				}
				break;
			case 3:
				// get player's status in specified world
				getPlayerStatus(sender, getPlayer(sender, args[1], true), isWorld(sender, args[2]));
				break;
			default:
				sendUsage(sender, "status");
			}
			return true;
		} else if ((args[0].equalsIgnoreCase("reset"))||(args[0].equalsIgnoreCase("r"))){
			switch (args.length){
			case 2:
				// reset specific player across all worlds
				resetPlayer(sender, getPlayer(sender, args[1], true), "*");
				break;
			case 3:
				// reset specific player in specific world
				resetPlayer(sender, getPlayer(sender, args[1], true), isWorld(sender, args[2]));
				break;
			default:
				sendUsage(sender, "reset");
			}
			return true;
		} else if ((args[0].startsWith("w:"))||(args[0].equalsIgnoreCase("world"))||(args[0].equalsIgnoreCase("w"))){
			String worldname = null;
			String action = args[args.length-1];	// last argument determines what action to perform
			
			// extract world name
			if (args[0].startsWith("w:")){
				worldname = isWorld(sender, args[0].substring(2));
			} else if (args.length>1){
				worldname = isWorld(sender, args[1]);
			} else {
				// no world specified, send usage
				sendUsage(sender, "world");
			}
			
			if (worldname != null){
				if ((action.equalsIgnoreCase("status"))||(worldname.equalsIgnoreCase(action))||((worldname.equalsIgnoreCase(action.substring(2)))&&(action.startsWith("w:")))){
					// user requested world status
					getWorldStatus(sender, worldname);
				} else if (action.equalsIgnoreCase("reset")){
					// reset all players in specified world
					resetWorld(sender, worldname, plugin.getWorldDefault(worldname));
				} else if ((action.equalsIgnoreCase("on"))||(action.equalsIgnoreCase("off"))){
					// toggle world to new value
					toggleWorld(sender, worldname, checkNewValue(action));		
				} else {
					// invalid argument, return command usage
					sendUsage(sender, "world");
				}
			}
			return true;
		} else if ((args[0].equalsIgnoreCase("global"))||(args[0].equalsIgnoreCase("g"))){
			if (args.length == 2){
				if (args[1].equalsIgnoreCase("status")){
					// user requested global status
					getGlobalStatus(sender);
				} else if (args[1].equalsIgnoreCase("reset")){
					// reset all players to global login default across all worlds
					resetGlobal(sender);
				} else if (checkNewValue(args[1]) != (Boolean) null){
					// toggle to specific global status
					toggleGlobal(sender, checkNewValue(args[1]));
				} else {
					// invalid argument, return command usage
					sendUsage(sender, "global");
				}
			} else {
				// no new value, return global status
				getGlobalStatus(sender);
			}
			return true;
		} else if (args[0].equalsIgnoreCase("help")){
			if (args.length==2){
				if (args[1].equalsIgnoreCase("toggle")) sendUsage(sender, "toggle");
				if (args[1].equalsIgnoreCase("status")) sendUsage(sender, "status");
				if (args[1].equalsIgnoreCase("reset")) sendUsage(sender, "reset");
				if (args[1].equalsIgnoreCase("world")) sendUsage(sender, "world");
				if (args[1].equalsIgnoreCase("global")) sendUsage(sender, "global");
			} else {
				sendUsage(sender, "help");
			}
		} else {
			sendUsage(sender, "help");
		}

		return true;
	}

	/**
	 * Gets player whose name partially matches the input string
	 * @param sender - command sender
	 * @param player - input string
	 * @return player whose name partially matches input string 
	 */
	private Player getPlayer(CommandSender sender, String player, boolean notify) {
		
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
			if (notify) sender.sendMessage("Found " + found.size() + " online players matching that partial name:");
			for (Player p : found){
				if (notify) sender.sendMessage("- "+p.getDisplayName());
			}
		} else {
			// found no matches
			if (notify) PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.PLAYER_NOT_FOUND);
		};
		
		return (Player) null;
	}

	/**
	 * Prints target player's status to chat/console
	 * @param sender - command sender
	 * @param target - who to check for
	 * @param world - what world to check in
	 */
	private void getPlayerStatus(CommandSender sender, Player target, String world) {
		
		// check for permission to view own or other player's status
		if (!(((plugin.permissionsCheck(sender, "pvptoggle.self.status", true))&&(sender.getName().equalsIgnoreCase(target.getName()))) 
				|| (plugin.permissionsCheck(sender, "pvptoggle.other.status", true)) || (plugin.permissionsCheck(sender, "pvptoggle.admin", true)))){
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
			return;
		}
		
		if ((target != null)&&(world != null)){
			if (sender.getName().equalsIgnoreCase(target.getName())){
				// report personal PvP status
				if (plugin.permissionsCheck(target, "pvptoggle.pvp.force", false)){
					PvPLocalisation.display(sender, "", world, PvPLocalisation.Strings.PVP_FORCED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_STATUS);
				} else if (plugin.permissionsCheck(target, "pvptoggle.pvp.deny", false)){
					PvPLocalisation.display(sender, "", world, PvPLocalisation.Strings.PVP_DENIED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_STATUS);
				} else if (plugin.checkPlayerStatus(target, target.getWorld().getName())){
					PvPLocalisation.display(sender, "", world, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_STATUS);
				} else {
					PvPLocalisation.display(sender, "", world, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_STATUS);
				}
			} else {
				// report someone else's PvP status
				if (plugin.permissionsCheck(target, "pvptoggle.pvp.force", false)){
					PvPLocalisation.display(sender, target.getName(), world, PvPLocalisation.Strings.PVP_FORCED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_OTHER_STATUS);
				} else if (plugin.permissionsCheck(target, "pvptoggle.pvp.deny", false)){
					PvPLocalisation.display(sender, target.getName(), world, PvPLocalisation.Strings.PVP_DENIED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_OTHER_STATUS);
				} else if (plugin.checkPlayerStatus(target, world)){
					PvPLocalisation.display(sender, target.getName(), world, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_OTHER_STATUS);
				} else {
					PvPLocalisation.display(sender, target.getName(), world, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_OTHER_STATUS);
				}
			}
		}
	}
	
	/**
	 * Prints global PvP status to chat/console
	 * @param sender - command sender
	 */
	private void getGlobalStatus(CommandSender sender){
		if (plugin.permissionsCheck(sender, "pvptoggle.global.status", true)){
			
			if ((Boolean) plugin.getGlobalSetting("enabled")){
				PvPLocalisation.display(sender, "", "", PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_GLOBAL_STATUS);
			} else {
				PvPLocalisation.display(sender, "", "", PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_GLOBAL_STATUS);
			}
			
		} else {
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
		}
		
	}	
	
	/**
	 * Prints PvP status in specified world
	 * @param sender
	 * @param world
	 */
	private void getWorldStatus(CommandSender sender, String worldname) {
		if (plugin.permissionsCheck(sender, "pvptoggle.world.status", true)){
			
			if (worldname != null){
				if (plugin.getWorldStatus(worldname)){
					PvPLocalisation.display(sender, "", worldname, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_WORLD_STATUS);
				} else {
					PvPLocalisation.display(sender, "", worldname, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_WORLD_STATUS);
				}
			}
			
		} else {
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
		}

	}

	/**
	 * Toggle the PvP status of a player
	 * @param sender - command sender
	 * @param targetplayer - name of player to toggle
	 * @param newval - new PvP status of player
	 * @param worldname - world to toggle in
	 */
	private void togglePlayer(CommandSender sender, Player player, String worldname, boolean newval) {
		
		if ((player == null)||(worldname == null)){
			// target not found, return out
			return;
		}
				
		// check for permission to toggle own or other player's status
		if (!(((plugin.permissionsCheck(sender, "pvptoggle.self.toggle", true))&&(sender.getName().equalsIgnoreCase(player.getName()))) 
				|| (plugin.permissionsCheck(sender, "pvptoggle.other.toggle", true)))){
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
			return;	// no permission, return out
		}
		
		
		if (sender.getName().equalsIgnoreCase(player.getName())){
			// if sender is the target
			if (!WorldGuardRegionCheck(player, "this")) return;	// no permission to toggle in this region
			
			if (newval){
				// if enabling
				plugin.setPlayerStatus(player, player.getWorld().getName(), true);
				PvPLocalisation.display(sender, "", player.getWorld().getName(), PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PVP_PLAYER_SELF_TOGGLE);
				plugin.log.info("[PvPToggle] Player " + player.getDisplayName() + " enabled pvp");
				plugin.setLastAction(player, "toggle");
			} else if (!(newval)){
				// if disabling
				if (plugin.checkLastAction(player, "toggle", player.getWorld().getName())){
					plugin.setPlayerStatus(player, player.getWorld().getName(), false);
					PvPLocalisation.display(sender, "", player.getWorld().getName(), PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PVP_PLAYER_SELF_TOGGLE);
					plugin.log.info("[PvPToggle] Player " + player.getDisplayName() + " disabled pvp");
				} else {
					PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.PLAYER_COOLDOWN);
				}
			}
		} else {
			// if the target is another player
			if (!WorldGuardRegionCheck(player, player.getName()+"'s current ")) return;	// no permission to toggle in this region
			
			// set message details
			String message = null;
			if (newval){
				message = PvPLocalisation.Strings.PVP_ENABLED.toString();
			} else {
				message = PvPLocalisation.Strings.PVP_DISABLED.toString();
			}
			
			// toggle
			if (worldname.equalsIgnoreCase("*")){
				// if toggling across all worlds
				for (World world : plugin.getServer().getWorlds()){
					plugin.setPlayerStatus(player, world.getName(), newval);
				}
				PvPLocalisation.display(player, "", "", message, PvPLocalisation.Strings.PVP_PLAYER_GLOBAL_TOGGLE);
				PvPLocalisation.display(sender, player.getName(), "", message, PvPLocalisation.Strings.PVP_PLAYER_GLOBAL_TOGGLE_SENDER);
			} else {
				// if toggling specific world
				plugin.setPlayerStatus(player, worldname, newval);
				PvPLocalisation.display(player, "", worldname, message, PvPLocalisation.Strings.PVP_PLAYER_OTHER_TOGGLE);
				PvPLocalisation.display(sender, player.getName(), worldname, message, PvPLocalisation.Strings.PVP_PLAYER_OTHER_TOGGLE_SENDER);
			}
			plugin.setLastAction(player, "toggle");
		}
	}
	
	/**
	 * Checks if a player is currently in a WorldGuard region with the PVP flag set
	 * @param player - who to check for
	 * @return false if in region with flag set, otherwise true
	 */
	private boolean WorldGuardRegionCheck(Player player, String target) {
		if ((Boolean) plugin.getGlobalSetting("worldguard")){
			return plugin.regionListener.WorldGuardRegionCheck(player, target);
		}
		return true;
	}

	/**
	 * Toggles whether PvP is allowed or denied in a world
	 * @param sender
	 * @param targetworld
	 * @param newval
	 */
	private void toggleWorld(CommandSender sender, String targetworld, boolean newval){
		if (!((plugin.permissionsCheck(sender, "pvptoggle.world.toggle", true)) || (plugin.permissionsCheck(sender, "pvptoggle.admin", true)))){
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
			return;	// no permission, return out
		}
		
		if (targetworld != null){
			plugin.setWorldStatus(targetworld, newval);
			if (newval){
				PvPLocalisation.display(sender, "", targetworld, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PVP_WORLD_TOGGLE_SENDER);
			} else {
				PvPLocalisation.display(sender, "", targetworld, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PVP_WORLD_TOGGLE_SENDER);
			}
		}
	}
	
	/**
	 * Toggles whether PvP is allowed or denied globally
	 * @param sender - who sent the toggle request
	 * @param newval - what to toggle it to
	 */
	private void toggleGlobal(CommandSender sender, boolean newval) {
		if (!((plugin.permissionsCheck(sender, "pvptoggle.global.toggle", true)) || (plugin.permissionsCheck(sender, "pvptoggle.admin", true)))){
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
			return;	// no permission, return out
		}
		
		plugin.toggleGlobalStatus(newval);
		if (newval){
			PvPLocalisation.display(sender, "", "", PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PVP_GLOBAL_TOGGLE_SENDER);
		} else {
			PvPLocalisation.display(sender, "", "", PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PVP_GLOBAL_TOGGLE_SENDER);
		}
	}
	
	/**
	 * Resets a player's PvP status to login default in specified, or all worlds
	 * @param sender - who sent the command
	 * @param player - target player
	 * @param worldname - what world to reset in
	 */
	private void resetPlayer(CommandSender sender, Player player, String worldname) {
		if ((player == null)||(worldname == null)){
			// invalid player, exit
			return;
		}
		
		if (!(((plugin.permissionsCheck(sender, "pvptoggle.self.reset", true))&&(sender.getName().equalsIgnoreCase(player.getName()))) 
				|| (plugin.permissionsCheck(sender, "pvptoggle.other.reset", true)) || (plugin.permissionsCheck(sender, "pvptoggle.admin", true)))){
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
			return;	// no permission, return out
		}
		
		if (worldname.equalsIgnoreCase("*")){
			// if resetting in all worlds
			for (World world : plugin.getServer().getWorlds()){
				plugin.setPlayerStatus(player, world.getName(), plugin.getWorldDefault(world.getName()));
			}
			plugin.setLastAction(player, "toggle");
			PvPLocalisation.display(player, "", "", "", PvPLocalisation.Strings.PVP_RESET_PLAYER_GLOBAL);
			PvPLocalisation.display(sender, player.getName(), "", "", PvPLocalisation.Strings.PVP_RESET_PLAYER_GLOBAL_SENDER);
		} else {
			// if resetting in specific world
			plugin.setPlayerStatus(player, worldname, plugin.getWorldDefault(worldname));
			plugin.setLastAction(player, "toggle");
			PvPLocalisation.display(player, "", worldname, "", PvPLocalisation.Strings.PVP_RESET_PLAYER);
			PvPLocalisation.display(sender, player.getName(), worldname, "", PvPLocalisation.Strings.PVP_RESET_PLAYER_SENDER);
		}
		
	}
	
	/**
	 * Resets PvP status of every player in a world to specified value
	 * @param sender - who sent the command
	 * @param worldname - what world to reset in
	 * @param newval - what to reset it to
	 */
	private void resetWorld(CommandSender sender, String worldname, boolean newval) {
		
		if (!((plugin.permissionsCheck(sender, "pvptoggle.world.reset", true)) || (plugin.permissionsCheck(sender, "pvptoggle.admin", true)))){
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
			return;	// no permission, return out
		}
		
		Player players[] = plugin.getServer().getOnlinePlayers();	// get list of online players
		
		// if toggle all players in specific world
		for (Player p : players){
			plugin.setPlayerStatus(p, worldname, newval);
			plugin.setLastAction(p, "toggle");
			if (newval) PvPLocalisation.display(p, "", worldname, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PVP_RESET_WORLD);
			else PvPLocalisation.display(p, "", worldname, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PVP_RESET_WORLD);
		}
		if (newval) PvPLocalisation.display(sender, "", worldname, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PVP_RESET_WORLD_SENDER); 
		else  PvPLocalisation.display(sender, "", worldname, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PVP_RESET_WORLD_SENDER);
	}
	
	/**
	 * Resets PvP status of all players in all worlds to login default
	 * @param sender
	 */
	private void resetGlobal(CommandSender sender){
		if (!((plugin.permissionsCheck(sender, "pvptoggle.global.reset", true)) || (plugin.permissionsCheck(sender, "pvptoggle.admin", true)))){
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
			return;	// no permission, return out
		}
		
		Player players[] = plugin.getServer().getOnlinePlayers();	// get list of online players

		// toggle all players in all worlds
		for (Player p : players){
			for (World world : plugin.getServer().getWorlds()){
				plugin.setPlayerStatus(p, world.getName(), plugin.getWorldDefault(world.getName()));	
			}
			plugin.setLastAction(p, "toggle");
			PvPLocalisation.display(p, "", "", "", PvPLocalisation.Strings.PVP_RESET_GLOBAL);
		}	
		PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.PVP_RESET_GLOBAL_SENDER);

	}
	
	/**
	 * Print command usage to screen/chat
	 * @param sender - command sender
	 * @param string - which section was send for
	 */
	private void sendUsage(CommandSender sender, String section) {
		sender.sendMessage(ChatColor.RED + "-----------------[ "+ChatColor.GOLD+"PvPToggle v"+plugin.getDescription().getVersion()+ChatColor.RED+" ]-----------------");
		ChatColor messagecolour = ChatColor.GOLD;
		if (section.equalsIgnoreCase("help")){
			sender.sendMessage(messagecolour+"/pvp help toggle "+ChatColor.GRAY+"- Show usage information for toggling");
			sender.sendMessage(messagecolour+"/pvp help status "+ChatColor.GRAY+"- Show usage information for checking status");
			sender.sendMessage(messagecolour+"/pvp help reset "+ChatColor.GRAY+"- Show usage information for reset command");
			sender.sendMessage(messagecolour+"/pvp help world "+ChatColor.GRAY+"- Show usage information for world command");
			sender.sendMessage(messagecolour+"/pvp help global "+ChatColor.GRAY+"- Show usage information for global command");
		}
		if ((section.equalsIgnoreCase("toggle"))||(section.equalsIgnoreCase("*"))){
			if (plugin.permissionsCheck(sender, "pvptoggle.self.toggle", true)){
				sender.sendMessage(messagecolour+"/pvp "+ChatColor.GRAY+"- Toggles own PvP status");
				sender.sendMessage(messagecolour+"/pvp on "+ChatColor.GRAY+"- Sets own PvP status to on");
				sender.sendMessage(messagecolour+"/pvp off "+ChatColor.GRAY+"- Sets own PvP status to off");
			}
			if (plugin.permissionsCheck(sender, "pvptoggle.other.toggle", true)){
				sender.sendMessage(messagecolour+"/pvp on [player] "+ChatColor.GRAY+"- Sets player's PvP status to on in current world");
				sender.sendMessage(messagecolour+"/pvp on [player] [world] "+ChatColor.GRAY+"- Sets player's PvP status to on in specified world");
				sender.sendMessage(messagecolour+"/pvp off [player] "+ChatColor.GRAY+"- Sets player's PvP status to off in current world");
				sender.sendMessage(messagecolour+"/pvp off [player] [world] "+ChatColor.GRAY+"- Sets player's PvP status to off in specified world");
			}
		}
		if ((section.equalsIgnoreCase("reset"))||(section.equalsIgnoreCase("*"))){
			if (plugin.permissionsCheck(sender, "pvptoggle.other.reset", true)){
				sender.sendMessage(messagecolour+"/pvp reset [player] "+ChatColor.GRAY+"- Resets player's PvP status to default across all worlds");
				sender.sendMessage(messagecolour+"/pvp reset [player] [world] "+ChatColor.GRAY+"- Resets player's PvP status to default in specified world");
			}
		}
		if ((section.equalsIgnoreCase("status"))||(section.equalsIgnoreCase("*"))){
			if (plugin.permissionsCheck(sender, "pvptoggle.self.status", true)){
				sender.sendMessage(messagecolour+"/pvp status "+ChatColor.GRAY+"- Shows own PvP status in current world");
			}
			if (plugin.permissionsCheck(sender, "pvptoggle.other.status", true)){
				sender.sendMessage(messagecolour+"/pvp status [player] "+ChatColor.GRAY+"- Shows player's PvP status in current world");
				sender.sendMessage(messagecolour+"/pvp status [player] [world] "+ChatColor.GRAY+"- Shows player's PvP status in specified world");
			}
		} 
		if ((section.equalsIgnoreCase("world"))||(section.equalsIgnoreCase("*"))){
			if (plugin.permissionsCheck(sender, "pvptoggle.world.toggle", true)){
				sender.sendMessage(messagecolour+"/pvp w:[world] on "+ChatColor.GRAY+"- Sets PvP status of specified world to on");
				sender.sendMessage(messagecolour+"/pvp w:[world] off "+ChatColor.GRAY+"- Sets PvP status of specified world to off");
			}
			if (plugin.permissionsCheck(sender, "pvptoggle.world.reset", true)){
				sender.sendMessage(messagecolour+"/pvp w:[world] reset "+ChatColor.GRAY+"- Resets PvP status of all players in specified world");
			}
			if (plugin.permissionsCheck(sender, "pvptoggle.world.status", true)){
				sender.sendMessage(messagecolour+"/pvp w:[world] "+ChatColor.GRAY+"- Shows PvP status of specified world");
				sender.sendMessage(messagecolour+"/pvp w:[world] status "+ChatColor.GRAY+"- Shows PvP status of specified world");
			}
		}
		if ((section.equalsIgnoreCase("global"))||(section.equalsIgnoreCase("*"))){
			if (plugin.permissionsCheck(sender, "pvptoggle.global.toggle", true)){
				sender.sendMessage(messagecolour+"/pvp global on "+ChatColor.GRAY+"- Enables global PvP");
				sender.sendMessage(messagecolour+"/pvp global off "+ChatColor.GRAY+"- Disables global PvP");
			}
			if (plugin.permissionsCheck(sender, "pvptoggle.global.reset", true)){
				sender.sendMessage(messagecolour+"/pvp global reset "+ChatColor.GRAY+"- Resets PvP status of all players in all worlds to login default");
			}
			if (plugin.permissionsCheck(sender, "pvptoggle.global.reset", true)){
				sender.sendMessage(messagecolour+"/pvp global "+ChatColor.GRAY+"- Shows global PvP status");
				sender.sendMessage(messagecolour+"/pvp global status "+ChatColor.GRAY+"- Shows global PvP status");
			}
		}
		
	}

	/**
	 * Converts string enable/disable parameters to booleans
	 * @param string - string enable/disable input parameter
	 * @return boolean true for on, false for off
	 */
	public static Boolean checkNewValue(String string){
		Boolean enable = (Boolean) null;
		if ((string.equalsIgnoreCase("on"))||(string.equalsIgnoreCase("enable"))){
			enable = true;
		} else if ((string.equalsIgnoreCase("off"))||(string.equalsIgnoreCase("disable"))){
			enable = false;
		}
		return enable;
	}
	
	/**
	 * Check world name to make sure it's valid
	 * @param sender - who is checking
	 * @param worldname - what world name to check
	 * @param notify - notify on invalid world
	 * @return completed name of world, or null if no match
	 */
	private String isWorld(CommandSender sender, String worldname, boolean notify){
		String output = plugin.checkWorldName(worldname);
		if (output == null) if (notify) PvPLocalisation.display(sender, null, null, null, PvPLocalisation.Strings.WORLD_NOT_FOUND);
		return output;
	}
	
	private String isWorld(CommandSender sender, String worldname){
		return isWorld(sender, worldname, true);		
	}
	
}
