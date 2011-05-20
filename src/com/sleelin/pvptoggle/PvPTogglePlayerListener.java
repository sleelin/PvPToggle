package com.sleelin.pvptoggle;

import org.bukkit.event.player.PlayerListener;



public class PvPTogglePlayerListener extends PlayerListener {

	public static PvPToggle plugin;
	
	public PvPTogglePlayerListener(PvPToggle instance) {
		plugin = instance;
	}

}
