package com.hypyrdon.bukkitplugins;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * NAME: AybChatOut
 * Created by hypyrdon on 1/28/14.
 */
public class AybChatOut {
	public void chatMessage(CommandSender sender, String message) {
		sender.sendMessage(
				ChatColor.BLUE+ "[AllUrBase] " + ChatColor.WHITE
				+ message );
	}
	public void chatError(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.RED + "[AllUrBase] " + ChatColor.WHITE + message );
	}
}
