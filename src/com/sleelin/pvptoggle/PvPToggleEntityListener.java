package com.sleelin.pvptoggle;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class PvPToggleEntityListener extends EntityListener {
	
	public static PvPToggle plugin;
	private EntityDamageByEntityEvent edentitye;
	
	public PvPToggleEntityListener(PvPToggle instance) {
		plugin = instance;
	}
	
	public void onEntityDamage(EntityDamageEvent event){
		if (event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			boolean haspermissions = false;

			if (PvPToggle.permissionHandler != null){
				if (PvPToggle.permissionHandler.has(player, "pvptoggle")){
					haspermissions = true;
				}
			} else if (player.isOp()){
				haspermissions = true;
			}
			
			if (haspermissions){
				boolean isenabled = plugin.pvpEnabled(player);
				if (!isenabled){
					Player defender = (Player) edentitye.getDamager();
					String message = ChatColor.RED + player.getDisplayName() + "has PvP disabled!";
					defender.sendMessage(message);
					event.setCancelled(true);
				}
				
			}
		}
	}
	
	

}
