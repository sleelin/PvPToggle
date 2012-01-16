package com.sleelin.pvptoggle;

import java.util.GregorianCalendar;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

import net.citizensnpcs.api.CitizensManager;

public class PvPToggleEntityListener extends EntityListener {
	
	public static PvPToggle plugin;
	
	public PvPToggleEntityListener(PvPToggle instance) {
		plugin = instance;
	}
	
	public void onBlockBreak(){
		
	}
	
	public void onEntityDamage(EntityDamageEvent event){
		if (event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			if (event instanceof EntityDamageByEntityEvent){
				boolean proceed = false;
				Player damager = null;
				EntityDamageByEntityEvent edbye = (EntityDamageByEntityEvent) event;
				
				if (edbye.getDamager() instanceof Projectile){
					Projectile projectile = (Projectile) edbye.getDamager();
					if (projectile.getShooter() instanceof Player){
						damager = (Player) projectile.getShooter();
						proceed = true;
					}
				} else if (edbye.getDamager() instanceof Player){
					damager = (Player) edbye.getDamager();
					proceed = true;
				}
				
				if (PvPToggle.citizensEnabled){
					if (CitizensManager.isNPC(player)){
						proceed = false;
					}
				}
				
				if (proceed){
					boolean genabled = plugin.gpvpEnabled();
					if (!(PvPToggle.worldstatus.get(player.getWorld().getName())) || (!genabled)) {
						if (!(PvPToggle.worldstatus.get(player.getWorld().getName()))) {
							damager.sendMessage(ChatColor.RED + "PvP is disabled in world " + player.getWorld().getName() + "!");
						}
						if (!genabled) {
							damager.sendMessage(ChatColor.RED + "Global PvP is disabled!");
						}
						event.setCancelled(true);
						return;
					}
					if ((plugin.permissionsCheck(player, "pvptoggle.use", true))&&(!plugin.permissionsCheck(player, "pvptoggle.pvp.force", false))){							
						boolean targetenabled = plugin.pvpEnabled(player, player.getWorld().getName());
						boolean damagerenabled = plugin.pvpEnabled(damager, player.getWorld().getName());
						if ((plugin.permissionsCheck(damager, "pvptoggle.pvp.autoenable", false))&&(!damagerenabled)){
							plugin.pvpEnable(damager, damager.getWorld().getName());
							damagerenabled = true;
							damager.sendMessage(ChatColor.GOLD + "PvP automatically enabled due to combat!");
						}
						if ((!targetenabled)||(!damagerenabled)){
							String message = null;
							if (!targetenabled){
								message = ChatColor.RED + player.getDisplayName() + " has PvP disabled!";
							}
							if (!damagerenabled){
								message = ChatColor.RED + "You have PvP disabled!";
							}
							damager.sendMessage(message);
							event.setCancelled(true);
						} else {
							PvPToggle.lastpvp.put(damager, new GregorianCalendar().getTime().getTime());
							PvPToggle.lastpvp.put(player, new GregorianCalendar().getTime().getTime());
						}
					}
				}
			}			
			
		}
	}
	
	/*private boolean checkWarmup(Player player) {
		GregorianCalendar cal = new GregorianCalendar();
		Long difference = cal.getTime().getTime() - PvPToggle.lastpvp.get(player);
		int before = difference.compareTo(((long) PvPToggle.warmup) * 1000);
		if (before>=0){
			return true;
		}
		return false;
	}*/
}
