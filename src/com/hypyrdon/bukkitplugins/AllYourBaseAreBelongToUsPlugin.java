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

/**
 * NAME: AllYourBaseAreBelongToUsPlugin
 * DESC: player base mover/migrator
 * Created by hypyrdon on 1/25/14.
 *
 * may have to add MultiverseCore jar (and other Mv jars)
 * may have to use MultiverseException for Exceptions
 */

public class AllYourBaseAreBelongToUsPlugin extends JavaPlugin {

	private final String CHATPREMSG = ChatColor.BLUE + "[AllUrBase]" + ChatColor.WHITE + " ";
	private final String CHATPREWARN = ChatColor.YELLOW + "[AllUrBase] * WARNING * ";
	private final String CHATPREERR = ChatColor.RED + "[AllUrBase] * ERROR * ";
	private File marksFile = null;
	private FileConfiguration cachedMarks;
	final String marksFilename = "marks.yml";

	@Override
	public void onEnable() {
		//PluginManager pm = getServer().getPluginManager();
		Bukkit.getServer().getLogger().info("AllYourBaseAreBelongToUs plugin enabled");
		cachedMarks = getCachedMarks();
		this.saveDefaultConfig(); //just in case it is not there.
	}

	@Override
	public void onDisable() {
		Bukkit.getServer().getLogger().info("AllYourBaseAreBelongToUs plugin disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		// command was run from console
		if ( !(sender instanceof Player)) {
			return onCommandFromConsole(sender, cmd, args);
		}

		Player p = (Player) sender;
		Bukkit.getServer().getLogger().info("AllYourBaseAreBelongToUs received command" + commandLabel);

		//TODO: use sub-commands.   || args[0].equalsIgnoreCase("MIGRATEALL")
		if (cmd.getName().equalsIgnoreCase("urbase")) {
			if (!p.hasPermission("urbase.use")) {
				sender.sendMessage(CHATPREMSG + "You don't have permission to use the AllYourBaseAreBelongToUs plugin");
				return false;
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("urbmark")) {
			return processCmdMark(p, cmd, args);
		} else if (cmd.getName().equalsIgnoreCase("aybabtu")) {
			sender.sendMessage(CHATPREWARN + "Watch your language!");
			return false;
		} else if (cmd.getName().equalsIgnoreCase("urbcleanup")) {
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
			Bukkit.getServer().getLogger().severe("Gesundheit...");
			return cmdMigrateAll(sender,cmd,args);
		} else if (cmd.getName().equalsIgnoreCase("urbcleanup")) {
			Bukkit.getServer().getLogger().severe("Gesundheit...");
			return cmdClearCache(sender,cmd,args);
		} else {
			sender.sendMessage(CHATPREERR + "cannot do that in console.");
		}
		return false;
	}

	private Location ymlLocBukkitLoc(World world, List<Double> coords) throws IllegalArgumentException {
		//String[] coords = coordslist.toArray();
		if ( coords.size() == 3 ) {
			//format: x, y, z
			return new Location( world, coords.get(0), coords.get(1), coords.get(2) );
		//} else  if ( coords.size() == 5 ) {
		//	//TODO: format: x, y, z, yaw, pitch
		} else {
			throw new IllegalArgumentException("a location must be x,y,z or x,y,z,yaw,pitch.");
		}
		//return null;
	}

	private boolean processCmdMark(Player player, Command cmd, String[] args) {
		//TODO: check perms (src.worlds, dst.worlds, mark)  if (!p.hasPermission("urbase.allowed.srcworlds"))
		//TODO: check player is in a valid src or dst world
		// get pre-existing locs for player from marks.yml "config/save/cache/data" file
		Location src_p1 = ymlLocBukkitLoc( player.getWorld(), cachedMarks.getDoubleList("urbase." + player.getName() + ".mark.src." + player.getWorld() + ".p1"));
		Location src_p2 = ymlLocBukkitLoc( player.getWorld(), cachedMarks.getDoubleList("urbase." + player.getName() + ".mark.src." + player.getWorld() + ".p2"));
		Location dst_p1 = ymlLocBukkitLoc( player.getWorld(), cachedMarks.getDoubleList("urbase." + player.getName() + ".mark.dst." + player.getWorld() + ".p1"));
		Location dst_p2 = ymlLocBukkitLoc( player.getWorld(), cachedMarks.getDoubleList("urbase." + player.getName() + ".mark.dst." + player.getWorld() + ".p2"));

		if (player.hasPermission("urbase.cmd.mark." + player.getWorld() )) {

			//is player in source world?
			if (player.hasPermission("urbase.allowed.srcworlds." + player.getWorld() )) {
				player.sendMessage(CHATPREMSG + "you are in the source world[" + player.getWorld() + "]");
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
						return true;
					case 1:
						//if 1 arg: then set p1 or p2 (as passed)
						if (args[0] == "p1") {
							src_p1 = player.getLocation();
						} else if (args[0] == "p2") {
							src_p2 = player.getLocation();
						} else {
							player.sendMessage(CHATPREERR + "invalid usage of command.");
							return false;
						}
						return true;
					case 2:
						//TODO: if 2 args: p1(x,y,z) and p2(x,y,z), then check_and_set_p1 and check_and_set_p2
						//TODO: decide how a point/location is to be stored in the yaml. (a string <"00x00y00z"> or 3 Ints<[ 00 00 00]>, or ???)
						return false;
					default:
						player.sendMessage(CHATPREERR + "too many arguments for command.");
						break;
				}
				//TODO: is p2 within 64 of p1 in each dimension?
				cachedMarks.set("urbase." + player.getName() +".mark.src." + player.getWorld() + ".p1", src_p1);
				cachedMarks.set("urbase." + player.getName() +".mark.src." + player.getWorld() + ".p2", src_p2);

			// is player in destination world
			} else if (player.hasPermission("urbase.allowed.dstworlds." + player.getWorld() )) {
				player.sendMessage(CHATPREMSG + "you are in the destination world[" + player.getWorld() + "]");
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
						return true;
					case 1:
						if (args[0] == "p1") {
							dst_p1 = player.getLocation();
						} else if (args[0] == "p2") {
							dst_p2 = player.getLocation();
						} else {
							player.sendMessage(CHATPREERR + "invalid usage of command.");
							return false;
						}
						return true;
					case 2:
						//TODO: if 2 args: p1(x,y,z) and p2(x,y,z), then check_and_set_p1 and check_and_set_p2
						return false;
					default:
						player.sendMessage(CHATPREERR + "too many arguments for command.");
						break;
				}
				//TODO: is p2 within 64 of p1 in each dimension?
				cachedMarks.set("urbase." + player.getName() +".mark.dst." + player.getWorld() + ".p1", dst_p1);
				cachedMarks.set("urbase." + player.getName() +".mark.dst." + player.getWorld() + ".p2", dst_p2);

			} else {
				player.sendMessage(CHATPREERR + "you do NOT have access to use markers in world[" + player.getWorld() + "]");
			}
		} //has permission:mark
		return false;
	}

	private boolean cmdMigrateAll(CommandSender sender, Command cmd, String[] args) {
		Bukkit.getServer().getLogger().warning("Not Implemented, yet.");
		return false;
	}

	private boolean cmdClearCache(CommandSender sender, Command cmd, String[] args) {
		Bukkit.getServer().getLogger().warning("Not Implemented, yet.");
		return false;
	}

	private void loadCachedMarks(){
		if (marksFile == null) {
			marksFile = new File(getDataFolder() + marksFilename);
		}
		cachedMarks = YamlConfiguration.loadConfiguration(marksFile);
	}

	private FileConfiguration getCachedMarks() {
		if ( cachedMarks == null ) {
			loadCachedMarks();
		}
		return cachedMarks;
	}

	private void saveCachedMarks() {
		if (cachedMarks == null || marksFile == null) {
			return;
		}
		try {
			cachedMarks.save(marksFile);
		} catch ( IOException e) {
			getLogger().severe("could not save marks cache " + marksFile );
		}
	}

}
