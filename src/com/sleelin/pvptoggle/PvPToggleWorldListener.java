package com.sleelin.pvptoggle;

import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;

public class PvPToggleWorldListener extends WorldListener {

	public static PvPToggle plugin;
	
	public PvPToggleWorldListener(PvPToggle instance) {
		plugin = instance;
	}
	
	public void onWorldLoad(WorldLoadEvent event){
		plugin.loadWorld(event.getWorld());
	}
	
}
