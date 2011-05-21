package com.sleelin.pvptoggle;

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
					Player damager = (Player) edbye.getDamager();
					boolean haspermissions = false;
					
					if (PvPToggle.permissionHandler != null){
						if (PvPToggle.permissionHandler.has(player, "pvptoggle.use")){
							haspermissions = true;
						}						
					} 
					if (player.isOp()){
						haspermissions = true;
					}					
					
					if (haspermissions){
						boolean isenabled = plugin.pvpEnabled(player, player.getWorld().toString());
						boolean genabled = plugin.gpvpEnabled();
						if ((!isenabled)||(!genabled)){
							String message = null;
							if (!isenabled){
								message = ChatColor.RED + player.getDisplayName() + " has PvP disabled!";
							}
							if (!genabled){
								message = ChatColor.RED + "Global PvP is disabled!";
							}
							damager.sendMessage(message);
							event.setCancelled(true);
						}
					}
				}
			}
		}
	}
	
	

}
