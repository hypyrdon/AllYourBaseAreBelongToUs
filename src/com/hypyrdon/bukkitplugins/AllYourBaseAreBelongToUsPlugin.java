package com.hypyrdon.bukkitplugins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

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
	private final String marksFilename = "marks.yml";
	private File marksFile = null;
	private FileConfiguration cachedMarks;

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
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
			return onCommandFromConsole(sender, cmd, commandLabel, args);
		}

		Player p = (Player) sender;

		//TODO: use sub-commands.   || args[0].equalsIgnoreCase("MIGRATEALL")
		if (cmd.getName().equalsIgnoreCase("urbase")) {
			if (!p.hasPermission("urbase.use")) {
				sender.sendMessage(CHATPREMSG + "You don't have permission to use the AllYourBaseAreBelongToUs plugin");
				return false;
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("urbmark")) {
			cmdMark(p,cmd,commandLabel,args);
			return true;
		} else if (cmd.getName().equalsIgnoreCase("aybabtu")) {
			sender.sendMessage(CHATPREWARN + "Watch your language!");
			return true;
		} else if (cmd.getName().equalsIgnoreCase("urbcleanup")) {
			sender.sendMessage(CHATPREWARN + "You can't do that here.");
			return true;
		} else {
			sender.sendMessage(CHATPREWARN + "unknown command: " + ChatColor.GOLD + cmd.getName());
		}

		return false;
	}

	private boolean onCommandFromConsole(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		//console can run migrateall and cleanall, but no others.
		if (cmd.getName().equalsIgnoreCase("aybabtu")) {
			Bukkit.getServer().getLogger().severe("Gesundheit...");
			cmdMigrateall(sender,cmd,commandLabel,args);
			return true;
		} else if (cmd.getName().equalsIgnoreCase("urbcleanup")) {
			Bukkit.getServer().getLogger().severe("Gesundheit...");
			cmdClearCache(sender,cmd,commandLabel,args);
			return true;
		} else {
			sender.sendMessage(CHATPREERR + "cannot do that in console.");
		}

		return false;
	}

	private boolean cmdMark(Player player, Command cmd, String commandLabel, String[] args) {
		//TODO: check perms (src.worlds, dst.worlds, mark)  if (!p.hasPermission("urbase.allowed.srcworlds"))
		//TODO: check player is in a valid src or dst world
		//TODO: get marks.yml "config"
		//TODO: if "mark" is the only param
		if (player.hasPermission("urbase.cmd.mark." + player.getWorld() )) {

			//is player in src world?
			if (player.hasPermission("urbase.allowed.srcworlds." + player.getWorld() )) {
				//if config.ayb.<player>.src.p2 set
					//reset p2 and p1              LATER: set a 15 second confirmyes callback to reset p2 and set new p1
					//set config.ayb.<player>.src.p1 = player.position
				//is config.ayb.<player>.src.p1 set?
					//is p2 within 64 of p1 in each dimension?
						//set config.ayb.<player>.src.p2 = player.position
					//else warn player of out of range p2
				//else
					//set config.ayb.<player>.src.p1 = player.position
			//is player in dst world?
			} else if (player.hasPermission("urbase.allowed.srcworlds." + player.getWorld() )) {
				//is config.ayb.<player>.dst.p1 set?
					//is p2 the same offset from p1 as it is in src world?
						//set config.ayb.<player>.dst.p2 = player.position
					//else warn player of misaligned p2
				//else set config.ayb.<player>.dst.p1 = player.position
			//else player in other world?
		//TODO: if 2 args: "mark" and "p1", then check_and_set_p1
		//TODO: if 2 args: "mark" and "p2", then check_and_set_p2
		//TODO: if 3 args: "mark" and p1(x,y,z) and p2(x,y,z), then check_and_set_p1 and check_and_set_p2
			//is player in src world?
				//is p2 within 64 of p1 in each dimension?
					//set config.ayb.<player>.src.p2 = player.position
					//set config.ayb.<player>.src.p1 = player.position
				//else warn player of out of range p2
			} else {
				player.sendMessage(CHATPREWARN + "you do NOT have access to use markers in world[" + player.getWorld() + "]");
			}
		} //has permisson:mark
		return false;
	}

	private boolean cmdMigrateall(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Bukkit.getServer().getLogger().warning("Not Implemented, yet.");
		return false;
	}

	private boolean cmdClearCache(CommandSender sender, Command cmd, String commandLabel, String[] args) {
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
