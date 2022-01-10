/*
 * This file is part of PartyChat.
 *
 * PartyChat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PartyChat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PartyChat.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * This file is part of PartyChat.
 *
 * PartyChat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PartyChat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PartyChat.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.partychat.commands;

import lol.hyper.partychat.PartyChat;
import lol.hyper.partychat.tools.ChatUtils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CommandPartyChatMessage implements TabExecutor {

    private final PartyChat partyChat;
    // anyone on this list has party chat enabled
    public final ArrayList<UUID> partyChatEnabled = new ArrayList<>();

    public CommandPartyChatMessage(PartyChat partyChat) {
        this.partyChat = partyChat;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(PartyChat.MESSAGE_PREFIX + "You must be a player for this command.");
            return true;
        }

        Player player = (Player) sender;

        if(!player.hasPermission("party.use")) {
            ChatUtils.sendErrorMessage(player, "No tienes permisos para ejecutar este comando");
            return true;
        }

        if (args.length < 1) {
            ChatUtils.sendErrorMessage(player, "Error: Usa /pc on/off");
            return true;
        }
        if (partyChat.partyManagement.lookupParty(player.getUniqueId()) == null) {
            ChatUtils.sendErrorMessage(player, "No formas parte de ninguna Conversación. Usa /party create para crear una.");
            return true;
        }
        String arg = args[0];
        if (arg.equalsIgnoreCase("on") || arg.equalsIgnoreCase("off")) {
            if (arg.equalsIgnoreCase("on")) {
                partyChatEnabled.add(player.getUniqueId());
                ChatUtils.sendInfoMessage(player, "Conversación privada activada. Los mensajes que envíes a continuación solo los leerán los miembros de la Conversación.");
            }
            if (arg.equalsIgnoreCase("off")) {
                partyChatEnabled.remove(player.getUniqueId());
                ChatUtils.sendInfoMessage(player, "Conversación privada desactivada. Los mensajes que envíes a continuación serán públicos.");
            }
        } else {
            ChatUtils.sendErrorMessage(player, "Error: Usa /pc on/off");
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length > 0) {
            return Arrays.asList("on", "off");
        } else {
            return null;
        }
    }
}
