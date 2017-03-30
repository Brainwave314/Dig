package me.brainwave;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class DigListener implements Listener{

	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		Player player = event.getPlayer();
		if (player.isSneaking()) {
			return;
		}
		
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}
		
		if (bPlayer.canBend(CoreAbility.getAbility("Dig"))) {
			new Dig(player);
		}
	}
}
