package com.sleelin.pvptoggle;

import java.util.GregorianCalendar;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

import com.citizens.npcs.NPCManager;

public class PvPToggleEntityListener extends EntityListener {
	
	public static PvPToggle plugin;
	
	public PvPToggleEntityListener(PvPToggle instance) {
		plugin = instance;
	}
	
	public void onEntityDamage(EntityDamageEvent event){
		if (event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			if (event instanceof EntityDamageByEntityEvent){
				EntityDamageByEntityEvent edbye = (EntityDamageByEntityEvent) event;
				if (edbye.getDamager() instanceof Player){
					boolean proceed = true;
					if (PvPToggle.citizensEnabled){
						if (NPCManager.isNPC(player)){
							proceed = false;
						}
					}
					if (proceed){
						Player damager = (Player) edbye.getDamager();
						boolean genabled = plugin.gpvpEnabled();
						if (!(PvPToggle.worldstatus.get(player.getWorld().getName())) || (!genabled)) {
							if (!(PvPToggle.worldstatus.get(player.getWorld().getName()))) {
								damager.sendMessage(ChatColor.RED + "Global PvP is disabled!");
							}
							if (!genabled) {
								damager.sendMessage(ChatColor.RED + "PvP is disabled in world " + player.getWorld().getName() + "!");
							}
							event.setCancelled(true);
							return;
						}
						if (!(PvPToggle.forcepvpworld.get(player.getWorld().getName()))) {
							if (plugin.permissionsCheck(player, "pvptoggle.use")){
								boolean targetenabled = plugin.pvpEnabled(player, player.getWorld().getName());
								boolean damagerenabled = plugin.pvpEnabled(damager, player.getWorld().getName());
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
									PvPToggle.lasttoggle.put(damager, new GregorianCalendar().getTime().getTime());
									PvPToggle.lasttoggle.put(player, new GregorianCalendar().getTime().getTime());
								}
							}
						}
					}
				}
			}
		}
	}
}
