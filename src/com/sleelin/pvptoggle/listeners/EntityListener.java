package com.sleelin.pvptoggle.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import com.sleelin.pvptoggle.PvPToggle;

import net.citizensnpcs.api.CitizensManager;

public class EntityListener implements Listener {
	
	public static PvPToggle plugin;
	
	public EntityListener(PvPToggle instance) {
		plugin = instance;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent event){
		if (event.isCancelled()) return;
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
				
				if ((Boolean) plugin.getGlobalSetting("citizens")){
					if (CitizensManager.isNPC(player)){
						proceed = false;
					}
				}
				
				if (proceed){
					eventMagic(player, damager, event, "EntityDamage", true);
				}
			}			
			
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void PotionHit(PotionSplashEvent event){
		if (event.isCancelled()) return;
		boolean firstdamage = true;
		ThrownPotion potion = event.getPotion();
		if (potion.getShooter() instanceof Player){
			Player damager = (Player) potion.getShooter();
			for(LivingEntity entity : event.getAffectedEntities()){
				if (entity instanceof Player){
					if (!((Player) entity).equals(damager)){
						eventMagic((Player) entity, damager, event, "PotionSplash", firstdamage);
						firstdamage = false;
					}
				}
			}
		}
	}
	
	private void eventMagic(Player player, Player damager, Event event, String type, boolean first){
		boolean cancellable = false;
		boolean genabled = (Boolean) plugin.getGlobalSetting("enabled");
		if (!(plugin.getWorldStatus(player.getWorld().getName())) || (!genabled)) {
			if (!(plugin.getWorldStatus(player.getWorld().getName()))) {
				if (first) damager.sendMessage(ChatColor.RED + "PvP is disabled in world " + player.getWorld().getName() + "!");
			}
			if (!genabled) {
				if (first) damager.sendMessage(ChatColor.RED + "Global PvP is disabled!");
			}
			cancellable = true;
		}
		if ((plugin.permissionsCheck(player, "pvptoggle.use", true))&&(!plugin.permissionsCheck(player, "pvptoggle.pvp.force", false))){							
			boolean targetenabled = plugin.checkPlayerStatus(player, player.getWorld().getName());
			boolean damagerenabled = plugin.checkPlayerStatus(damager, player.getWorld().getName());
			if ((!plugin.checkLastAction(damager, "combat", damager.getWorld().getName()))&&(!(plugin.permissionsCheck(damager, "pvptoggle.pvp.bypasswarmup", false)))){
				if (first) damager.sendMessage(ChatColor.RED + "You only just enabled PvP!");
				cancellable = true;
			}
			if ((plugin.permissionsCheck(damager, "pvptoggle.pvp.autoenable", false))&&(!damagerenabled)){
				plugin.setPlayerStatus(damager, damager.getWorld().getName(), true);
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
				if (first) damager.sendMessage(message);
				cancellable = true;
			} else {
				plugin.setLastAction(damager, "combat");
				plugin.setLastAction(player, "combat");
			}
		}
		if (cancellable){
			if (type.equalsIgnoreCase("EntityDamage")){
				((EntityDamageEvent) event).setCancelled(true);
			} else if (type.equalsIgnoreCase("PotionSplash")) {
				((PotionSplashEvent) event).setCancelled(true);
			}
		}
	}
	
	
}
