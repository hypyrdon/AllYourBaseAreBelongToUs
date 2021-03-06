package com.hypyrdon.bukkitplugins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * NAME: AllYourBaseAreBelongToUsPlugin
 * DESC: player base mover/migrator
 * Created by hypyrdon on 1/25/14.
 *
 * may have to add MultiverseCore jar (and other Mv jars)
 * may have to use MultiverseException for Exceptions
 * more
 */

public class AllYourBaseAreBelongToUsPlugin extends JavaPlugin {

	private final String CHATPREMSG = ChatColor.BLUE + "[AllUrBase]" + ChatColor.WHITE + " ";
	private final String CHATPREWARN = ChatColor.YELLOW + "[AllUrBase] * WARNING * ";
	private final String CHATPREERR = ChatColor.RED + "[AllUrBase] * ERROR * ";
	private File markerFile = null;
	private FileConfiguration cachedMarks;
	final String markerFilename = "markers.yml";

	@Override
	public void onEnable() {
		this.getLogger().setLevel(Level.ALL);  // This does not work!!  neither does setting java.util.logging.config.file!
		this.getLogger().finest("This is a finest");
		this.getLogger().finer("This is a finer");
		this.getLogger().fine("This is a fine");
		this.getLogger().info("This is an info");
		this.getLogger().warning("This is a warning");
		this.getLogger().severe("This is a severe");
		getLogger().info("AllYourBaseAreBelongToUs plugin enabled");
		cachedMarks = getCachedMarks();
		saveCachedMarks();
		saveDefaultConfig(); //just in case it is not there.
	}

	@Override
	public void onDisable() {
		getLogger().info("AllYourBaseAreBelongToUs plugin disabled");
		getLogger().setLevel(Level.INFO);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		getLogger().info("finest:AllYourBaseAreBelongToUs received command" + commandLabel);
		// command was run from console
		if ( !(sender instanceof Player)) {
			getLogger().info("finest:non-player sender detected. assuming server console.");
			return onCommandFromConsole(sender, cmd, args);
		}

		Player p = (Player) sender;

		//TODO: use sub-commands.   || args[0].equalsIgnoreCase("MIGRATEALL")
		if (cmd.getName().equalsIgnoreCase("urbase")) {
			if (!p.hasPermission("urbase.use")) {
				sender.sendMessage(CHATPREMSG + "You don't have permission to use the AllYourBaseAreBelongToUs plugin");
				getLogger().warning("Player [" + p.getName() + "] attempted to use AYBABTU module, but does not have rights to so.");
				return true;  //returning true will silence the "usage" dump
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("urbmark")) {
			return processCmdMark(p, cmd, args);
		} else if (cmd.getName().equalsIgnoreCase("aybabtu")) { //aybabtu is only avail via server console
			sender.sendMessage(CHATPREWARN + "Watch your language!");
			return false;
		} else if (cmd.getName().equalsIgnoreCase("urbcleanup")) { //urbcleanup is only avail via server console
			sender.sendMessage(CHATPREERR + "You can't do that here.");
			return false;
		} else {
			sender.sendMessage(CHATPREERR + "unknown command: " + ChatColor.GOLD + cmd.getName());
		}

		return false;
	}

	private boolean onCommandFromConsole(CommandSender sender, Command cmd, String[] args) {
		//console can run migrateall and cleanall, but no others.
		if (cmd.getName().equalsIgnoreCase("aybabtu")) {
			getLogger().severe("Gesundheit..."); //TODO: "aybabtu" Not implemented yet
			return cmdMigrateAll(sender, cmd, args);
		} else if (cmd.getName().equalsIgnoreCase("urbcleanup")) {
			getLogger().severe("Gesundheit..."); //TODO: "urbcleanup" Not implemented yet
			return cmdClearCache(sender,cmd,args);
		} else {
			sender.sendMessage(CHATPREERR + "cannot do that in console.");
		}
		return false;
	}

	private Location ymlLocBukkitLoc(World world, List<Double> coords) throws IllegalArgumentException {
		//String[] coords = coordslist.toArray();
		getLogger().info("finest:ymlLocBukkitLoc: entered.");
		getLogger().info("finest:coords.size() = " + coords.size());
		if ( coords.size() == 3 ) {
			//format: x, y, z
			return new Location( world, coords.get(0), coords.get(1), coords.get(2) );
		//} else  if ( coords.size() == 5 ) {
		//	//TODO: format: x, y, z, yaw, pitch
		} else {
			getLogger().info("finer: a location must be x,y,z or x,y,z,yaw,pitch.");
			//throw new IllegalArgumentException("a location must be x,y,z or x,y,z,yaw,pitch.");
		}
		return null;
	}

	private boolean processCmdMark(Player player, Command cmd, String[] args) {
		//TODO: check player is in a valid src or dst world
		// get any pre-existing locs for player from markers.yml datafile
		Location src_p1 = ymlLocBukkitLoc( player.getWorld(), cachedMarks.getDoubleList("urbase." + player.getName() + ".marker.src." + player.getWorld().getName() + ".p1"));
		Location src_p2 = ymlLocBukkitLoc( player.getWorld(), cachedMarks.getDoubleList("urbase." + player.getName() + ".marker.src." + player.getWorld().getName() + ".p2"));
		Location dst_p1 = ymlLocBukkitLoc( player.getWorld(), cachedMarks.getDoubleList("urbase." + player.getName() + ".marker.dst." + player.getWorld().getName() + ".p1"));
		Location dst_p2 = ymlLocBukkitLoc( player.getWorld(), cachedMarks.getDoubleList("urbase." + player.getName() + ".marker.dst." + player.getWorld().getName() + ".p2"));

		if (player.hasPermission("urbase.cmd.marker." + player.getWorld().getName() )) {

			//is player in valid source world?
			if (player.hasPermission("urbase.allowed.srcworlds." + player.getWorld().getName() )) {
				player.sendMessage(CHATPREMSG + "you are a valid source world[" + player.getWorld().getName() + "]"); //TODO: DEBUG
				switch (args.length) {
					case 0:
						//if 0 args: then check_for_p1_isCached then set p2 to player pos, else reset p2, and set p1 to player pos.
						if (src_p1 != null) {
							src_p2 = player.getLocation();
						} else {
							//TODO: LATER: set a 15 second confirmyes callback to reset p2 and set new p1
							src_p1 = player.getLocation();
							src_p2 = null;
						}
						break;
					case 1:
						//if 1 arg: then set p1 or p2 (as passed)
						if (args[0].equalsIgnoreCase("p1") ) {
							src_p1 = player.getLocation();
						} else if (args[0].equalsIgnoreCase("p2") ) {
							src_p2 = player.getLocation();
						} else {
							player.sendMessage(CHATPREERR + "invalid usage of " + cmd.getName() + " command.");
							return false;
						}
						break;
					case 2:
						//TODO: if 2 args: p1(x,y,z) and p2(x,y,z), then check_and_set_p1 and check_and_set_p2
						//TODO: decide how a point/location is to be stored in the yaml. (a string <"00x00y00z"> or 3 Ints<[ 00 00 00]>, or ???)
						player.sendMessage(CHATPREWARN + cmd.getName() + " does not support setting both points simultaneously, yet.");
						return false;
					default:
						player.sendMessage(CHATPREERR + "too many arguments for " + cmd.getName() + " command.");
						break;
				}
				//TODO: is p2 within 64 of p1 in each dimension?
				cachedMarks.set("urbase." + player.getName() +".marker.src." + player.getWorld().getName() + ".p1", src_p1);
				cachedMarks.set("urbase." + player.getName() +".marker.src." + player.getWorld().getName() + ".p2", src_p2);
				this.saveCachedMarks();
				return true;

			// is player in a valid destination world
			} else if (player.hasPermission("urbase.allowed.dstworlds." + player.getWorld().getName() )) {
				player.sendMessage(CHATPREMSG + "you are a valid destination world[" + player.getWorld().getName() + "]"); //TODO: DEBUG
				switch (args.length) {
					case 0:
						//if 0 args: then check_for_p1_isCached then set p2 to player pos, else reset p2, and set p1 to player pos.
						if (dst_p1 != null) {
							dst_p2 = player.getLocation();
						} else {
							//TODO: LATER: set a 15 second ConfirmYes callback to reset p2 and set new p1
							dst_p1 = player.getLocation();
							dst_p2 = null;
						}
						break;
					case 1:
						if (args[0].equalsIgnoreCase("p1") ) {
							dst_p1 = player.getLocation();
						} else if (args[0].equalsIgnoreCase("p2") ) {
							dst_p2 = player.getLocation();
						} else {
							player.sendMessage(CHATPREERR + "invalid usage of " + cmd.getName() + " command.");
							return false;
						}
						break;
					case 2:
						//TODO: if 2 args: p1(x,y,z) and p2(x,y,z), then check_and_set_p1 and check_and_set_p2
						player.sendMessage(CHATPREWARN + cmd.getName() + " does not support setting both points simultaneously, yet.");
						return false;
					default:
						player.sendMessage(CHATPREERR + "too many arguments for " + cmd.getName() + " command.");
						break;
				}
				//TODO: is p2 within 64 of p1 in each dimension?
				cachedMarks.set("urbase." + player.getName() +".marker.dst." + player.getWorld().getName() + ".p1", dst_p1);
				cachedMarks.set("urbase." + player.getName() +".marker.dst." + player.getWorld().getName() + ".p2", dst_p2);
				this.saveCachedMarks();
				return true;

			} else {
				getLogger().warning("Player [" + player.getName() + "] attempted to use AYBABTU module, but does not have rights to use in world [" + player.getWorld().getName() + "].");
				player.sendMessage(CHATPREERR + "you do NOT have access to use markers in world[" + player.getWorld().getName() + "]");
			}
		} //has permission:marker
		return false;
	}

	private boolean cmdMigrateAll(CommandSender sender, Command cmd, String[] args) {
		sender.sendMessage("[" + cmd.getName() + "] is not implemented, yet.");
		getLogger().warning("[" + cmd.getName() + "] Not Implemented."); //TODO: "aybabtu" Not implemented yet
		return false;
	}

	private boolean cmdClearCache(CommandSender sender, Command cmd, String[] args) {
		sender.sendMessage("[" + cmd.getName() + "] is not implemented, yet.");
		getLogger().warning("[" + cmd.getName() + "] Not Implemented."); //TODO: "urbcleanup" Not implemented yet
		return false;
	}

	private void loadCachedMarks(){
		if (markerFile == null) {
			markerFile = new File(getDataFolder() + markerFilename);
		}
		cachedMarks = YamlConfiguration.loadConfiguration(markerFile);
	}

	private FileConfiguration getCachedMarks() {
		if ( cachedMarks == null ) {
			loadCachedMarks();
		}
		getLogger().info("finest:AllYourBaseAreBelongToUs loaded (" + cachedMarks.getKeys(true).size() + ") items from marker file");
		return cachedMarks;
	}

	private void saveCachedMarks() {
		if (cachedMarks == null || markerFile == null) {
			return;
		}
		try {
			cachedMarks.save(markerFile);
		} catch ( IOException e) {
			getLogger().severe("could not save marker data file " + markerFile );
		}
	}

}
