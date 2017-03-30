package me.brainwave;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.configuration.Config;

public class Dig extends EarthAbility implements AddonAbility{
	
	private static Config config;
	
	private Location loc;
	private int range;
	private int radius = 1;
	private int maxRadius;
	private int delay;
	private long lastCheck = 0;
	private Random r = new Random();
	private Set<Material> transparent;

	@SuppressWarnings("deprecation")
	public Dig(Player player) {
		super(player);
		
		range = config.get().getInt("ExtraAbilities.Dig.Range");
		maxRadius = config.get().getInt("ExtraAbilities.Dig.MaxRadius");
		delay = config.get().getInt("ExtraAbilities.Dig.Delay");
		transparent = new HashSet<>();
		for (int id : getTransparentMaterial()) {
			transparent.add(Material.getMaterial(id));
		}
		
		loc = player.getTargetBlock(transparent, range).getLocation();
		
		start();
	}

	@Override
	public long getCooldown() {
		return config.get().getLong("ExtraAbilities.Dig.Cooldown");
	}

	@Override
	public Location getLocation() {
		return loc;
	}

	@Override
	public String getName() {
		return "Dig";
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public void progress() {
		if (player == null) {
			remove();
			return;
		}
		
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (!player.isSneaking()) {
			remove();
			return;
		}
		
		if (radius > maxRadius) {
			remove();
			return;
		}
		
		resetDigLocation(player.getTargetBlock(transparent, range).getLocation());
		dig();
	}
	
	public void resetDigLocation(Location newLoc) {
		if (loc == null) {
			loc = newLoc;
		} else if (loc != newLoc) {
			loc = newLoc;
			radius = 1;
		}
	}
	
	public void dig() {
		if (System.currentTimeMillis() < lastCheck + delay) {
			return;
		}
		
		List<Block> earth = new ArrayList<>();
		for (Location l : GeneralMethods.getCircle(loc, radius, radius, true, true, 0)) {
			if (l.getBlock() == null) {
				continue;
			}
			
			if (isEarth(l.getBlock())) {
				earth.add(l.getBlock());
			}
		}
		
		lastCheck = System.currentTimeMillis();
		if (earth.size() == 0) {
			radius++;
			return;
		}
		
		Block b = earth.get(r.nextInt(earth.size()));
		Material m = b.getType();
		b.setType(Material.AIR);
		
		ItemStack is = new ItemStack(m);
		b.getWorld().dropItem(player.getLocation().add(0.5, 0.5, 0.5), is);
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
	}

	@Override
	public String getAuthor() {
		return "Brainwave";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void load() {
		config = new Config(new File("ExtraAbilities.yml"));
		FileConfiguration c = config.get();
		
		c.addDefault("ExtraAbilities.Dig.Cooldown", 4000);
		c.addDefault("ExtraAbilities.Dig.Range", 9);
		c.addDefault("ExtraAbilities.Dig.MaxRadius", 4);
		c.addDefault("ExtraAbilities.Dig.Delay", 125);
		
		config.save();
		
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new DigListener(), ProjectKorra.plugin);
		if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.dig") == null) {
			Permission perm = new Permission("bending.ability.dig");
			perm.setDefault(PermissionDefault.TRUE);
			ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
		}
	}

	@Override
	public void stop() {
	}
	
	@Override
	public String getDescription() {
		return "A utility ability for earthbenders that allows them to dig up earthbendable blocks!";
	}
	
	@Override
	public String getInstructions() {
		return "Hold sneak and look at earthbendable blocks";
	}
}
