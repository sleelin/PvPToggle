package com.sleelin.pvptoggle;

import java.util.GregorianCalendar;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

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
//					if (PvPToggle.citizensEnabled){
//						if (NPCManager.isNPC(player)){
//							proceed = false;
//						}
//					}
					if (proceed){
						Player damager = (Player) edbye.getDamager();
						if (!(PvPToggle.forcepvpworld.get(player.getWorld().getName())))
						{
							if (plugin.permissionsCheck(player, "pvptoggle.use")){
								boolean targetenabled = plugin.pvpEnabled(player, player.getWorld().getName());
								boolean genabled = plugin.gpvpEnabled();
								boolean damagerenabled = plugin.pvpEnabled(damager, player.getWorld().getName());
								if ((!targetenabled)||(!damagerenabled)||(!genabled)||(!(PvPToggle.worldstatus.get(player.getWorld().getName())))){
									String message = null;
									if (!targetenabled){
										message = ChatColor.RED + player.getDisplayName() + " has PvP disabled!";
									}
									if (!damagerenabled){
										message = ChatColor.RED + "You have PvP disabled!";
									}
									if (!(PvPToggle.worldstatus.get(player.getWorld().getName()))){
										message = ChatColor.RED + "PvP is disabled in world " + player.getWorld().getName() + "!";
									}
									if (!genabled){
										message = ChatColor.RED + "Global PvP is disabled!";
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
