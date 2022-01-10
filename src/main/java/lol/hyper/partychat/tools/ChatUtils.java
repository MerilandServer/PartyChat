package lol.hyper.partychat.tools;

import lol.hyper.partychat.PartyChat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatUtils {

    public static void sendMessage(Player player, String msg) {
        player.sendMessage(colorize(PartyChat.MESSAGE_PREFIX + msg));
    }

    public static void sendInfoMessage(Player player, String msg) {
        sendMessage(player, "&9" + msg);
    }

    public static void sendErrorMessage(Player player, String msg) {
        sendMessage(player, "&c" + msg);
    }

    public static String colorize(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
