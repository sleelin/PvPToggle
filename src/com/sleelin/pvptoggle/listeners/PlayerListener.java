package com.sleelin.pvptoggle.listeners;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;

import static com.sk89q.worldguard.protection.flags.StateFlag.State;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.ApplicableRegionSet;

import static com.sk89q.worldguard.bukkit.BukkitUtil.*;

import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;

public class PlayerListener implements Listener {

	public static PvPToggle plugin;
	
	public PlayerListener(PvPToggle instance) {
		plugin = instance;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = (Player) event.getPlayer();
		for (World world : plugin.getServer().getWorlds()){
			plugin.checkPlayerStatus(player, world.getName());
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event){
		if ((Boolean) plugin.getGlobalSetting("worldguard")){
			Player player = event.getPlayer();
			WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
			ApplicableRegionSet set = worldGuard.getRegionManager(player.getWorld()).getApplicableRegions(toVector(player.getLocation().getBlock()));
			for (ProtectedRegion region : set){
				for (Flag<?> flag : region.getFlags().keySet()){
					if (flag.getName().equals("pvp")){
						if (region.getFlag(flag).equals(State.ALLOW)){
							if (!(plugin.checkPlayerStatus(player, player.getWorld().getName()))){
								plugin.setPlayerStatus(player, player.getWorld().getName(), true);
								PvPLocalisation.display(player, null, null, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.WORLDGUARD_REGION_ENTERED);
							}
						} else if (region.getFlag(flag).equals(State.DENY)){
							if (plugin.checkPlayerStatus(player, player.getWorld().getName())){
								plugin.setPlayerStatus(player, player.getWorld().getName(), false);
								PvPLocalisation.display(player, null, null, PvPLocalisation.Strings.PVP_DENIED.toString(), PvPLocalisation.Strings.WORLDGUARD_REGION_ENTERED);
							}
						}
					}
				}
			}
		}
	}

}
