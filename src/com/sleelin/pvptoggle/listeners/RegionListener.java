package com.sleelin.pvptoggle.listeners;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.bukkit.BukkitUtil;

import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;

public class RegionListener implements Listener {

	public static PvPToggle plugin;
	
	public RegionListener(PvPToggle instance) {
		plugin = instance;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event){
		if ((Boolean) plugin.getGlobalSetting("worldguard")){
			Player player = event.getPlayer();
			WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
			
			ApplicableRegionSet set = worldGuard.getRegionManager(player.getWorld()).getApplicableRegions(BukkitUtil.toVector(player.getLocation().getBlock()));
			for (ProtectedRegion region : set){
				for (Flag<?> flag : region.getFlags().keySet()){
					if (flag.getName().equals("pvp")){
						if (region.getFlag(flag).equals(StateFlag.State.ALLOW)){
							if (!(plugin.checkPlayerStatus(player, player.getWorld().getName()))){
								plugin.setPlayerStatus(player, player.getWorld().getName(), true);
								PvPLocalisation.display(player, null, null, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.WORLDGUARD_REGION_ENTERED);
							}
						} else if (region.getFlag(flag).equals(StateFlag.State.DENY)){
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
	
	/**
	 * Checks if a player is currently in a WorldGuard region with the PVP flag set
	 * @param player - who to check for
	 * @return false if in region with flag set, otherwise true
	 */
	public boolean WorldGuardRegionCheck(Player player, String target) {
		if ((Boolean) plugin.getGlobalSetting("worldguard")){
			WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
			ApplicableRegionSet set = worldGuard.getRegionManager(player.getWorld()).getApplicableRegions(toVector(player.getLocation().getBlock()));
			for (ProtectedRegion region : set){
				for (Flag<?> flag : region.getFlags().keySet()){
					if (flag.getName().equals("pvp")){
						if (region.getFlag(flag).equals(State.ALLOW)){
							PvPLocalisation.display(player, target, null, PvPLocalisation.Strings.PVP_FORCED.toString(), PvPLocalisation.Strings.WORLDGUARD_TOGGLE_DENIED);
						} else if (region.getFlag(flag).equals(State.DENY)){
							PvPLocalisation.display(player, target, null, PvPLocalisation.Strings.PVP_DENIED.toString(), PvPLocalisation.Strings.WORLDGUARD_TOGGLE_DENIED);
						}
						return false;
					}
				}
			}
		}
		return true;
	}
}
