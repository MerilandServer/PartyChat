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
import lol.hyper.partychat.tools.UUIDLookup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CommandParty implements TabExecutor {

    private final PartyChat partyChat;

    public CommandParty(PartyChat partyChat) {
        this.partyChat = partyChat;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatUtils.colorize(PartyChat.MESSAGE_PREFIX + "Versión de PartyChat "
                    + partyChat.getDescription().getVersion() + ". Por hyperdefined y Abortos."));
            sender.sendMessage(ChatUtils.colorize(PartyChat.MESSAGE_PREFIX + "Usa /party help para obtener ayuda."));
            return true;
        }

        Player player = (Player) sender;

        if(!player.hasPermission("party.use")) {
            ChatUtils.sendErrorMessage(player, "No tienes permisos para ejecutar este comando");
            return true;
        }

        switch (args[0]) {
            case "help":
                ChatUtils.sendInfoMessage(player, "--------------------------------------------");
                ChatUtils.sendInfoMessage(player, "/party help &7Muestra este menú.");
                ChatUtils.sendInfoMessage(player, "/party create &7Crea una party.");
                ChatUtils.sendInfoMessage(player, "/party invite <nick> &7Invita a un miembro a la party. Solo para líder.");
                ChatUtils.sendInfoMessage(player, "/party accept/deny &7Acepta o deniega una invitación.");
                ChatUtils.sendInfoMessage(player, "/party kick <nick> &7Expulsa a un miembro de la party. Solo para líder.");
                ChatUtils.sendInfoMessage(player, "/party leave &7Abandona la party.");
                ChatUtils.sendInfoMessage(player, "/party disband &7Elimina la party. Solo para líder.");
                ChatUtils.sendInfoMessage(player, "/party info &7Información sobre la party.");
                ChatUtils.sendInfoMessage(player, "/party transfer <nick> &7Cambia el líder de la party. Solo para líder.");
                ChatUtils.sendInfoMessage(player, "/party trust <nick> &7Permite a un miembro a invitar y expulsar otros miembros.");
                ChatUtils.sendInfoMessage(player, "/party untrust <nick> &7Elimina permisos a un miembro de la party.");
                ChatUtils.sendInfoMessage(player, "/pc <on/off> &7Activa o desactiva el chat de la party.");
                ChatUtils.sendInfoMessage(player, "--------------------------------------------");
                break;
            case "invite": {
                if (args.length == 1 || args.length > 2) {
                    ChatUtils.sendErrorMessage(player, "Error: Usa /party invite <nick>");
                    return true;
                }
                if (partyChat.partyManagement.lookupParty(player.getUniqueId()) == null) {
                    ChatUtils.sendErrorMessage(player, "No formas parte de ninguna party. Usa /party create para crear una.");
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(player.getUniqueId())
                        || partyChat.partyManagement.checkTrusted(player.getUniqueId())) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        ChatUtils.sendErrorMessage(player, "No se ha podido encontrar a &4" + args[1]);
                        return true;
                    }
                    Player playerToInvite = Bukkit.getPlayerExact(args[1]);
                    if (partyChat.partyManagement.pendingInvites.containsKey(playerToInvite.getUniqueId())) {
                        ChatUtils.sendErrorMessage(player, "&4" + args[1] +  " &cya tiene una invitación pendiente.");
                        return true;
                    }
                    if (partyChat.partyManagement.lookupParty(playerToInvite.getUniqueId()) != null) {
                        ChatUtils.sendErrorMessage(player, "&4" + args[1] +  " &cya forma parte de una party.");
                        return true;
                    }
                    String partyID = partyChat.partyManagement.lookupParty(player.getUniqueId());
                    partyChat.partyManagement.invitePlayer(playerToInvite.getUniqueId(), player.getUniqueId(), partyID);
                    return true;
                }
                ChatUtils.sendErrorMessage(player, "No puedes invitar a miembros a la party. Solo pueden el líder y miembros autorizados.");
                return true;
            }
            case "create": {
                if (partyChat.partyManagement.lookupParty(player.getUniqueId()) == null) {
                    partyChat.partyManagement.createParty(player.getUniqueId());
                    ChatUtils.sendInfoMessage(player, "Party creada correctamente.");
                } else {
                    ChatUtils.sendErrorMessage(player, "Ya formas parte de una party.");
                }
                return true;
            }
            case "accept": {
                if (partyChat.partyManagement.pendingInvites.containsKey(player.getUniqueId())) {
                    partyChat.partyManagement.removeInvite(player.getUniqueId(), true);
                } else {
                    ChatUtils.sendErrorMessage(player, "No tienes ninguna invitación pendiente.");
                }
                return true;
            }
            case "deny": {
                if (partyChat.partyManagement.pendingInvites.containsKey(player.getUniqueId())) {
                    partyChat.partyManagement.removeInvite(player.getUniqueId(), false);
                } else {
                    ChatUtils.sendErrorMessage(player, "No tienes ninguna invitación pendiente.");
                }
                return true;
            }
            case "leave": {
                if (partyChat.partyManagement.lookupParty(player.getUniqueId()) == null) {
                    ChatUtils.sendErrorMessage(player, "No formas parte de ninguna party. Usa /party create para crear una.");
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(player.getUniqueId())) {
                    ChatUtils.sendErrorMessage(player, "El líder de la party no puede abandonarla. Puedes eliminarla con &4/party disband&c." +
                            " También puedes transferir el liderazgo con &4/party transfer <nick>");
                    return true;
                }
                Player playerLeaving = (Player) sender;
                String partyID = partyChat.partyManagement.lookupParty(playerLeaving.getUniqueId());
                partyChat.partyManagement.sendPartyMessage(playerLeaving.getName() + " ha abandonado la party.", partyID);
                partyChat.partyManagement.removePlayerFromParty(player.getUniqueId(), partyID);
                return true;
            }
            case "disband": {
                if (partyChat.partyManagement.lookupParty(player.getUniqueId()) == null) {
                    ChatUtils.sendErrorMessage(player, "No formas parte de ninguna party. Usa /party create para crear una.");
                    return true;
                }
                if (!partyChat.partyManagement.isPlayerOwner(player.getUniqueId())) {
                    ChatUtils.sendErrorMessage(player, "No eres el líder de la party. Usa &4/party leave&c si quieres abandonarla.");
                    return true;
                }
                Player playerLeaving = (Player) sender;
                String partyID = partyChat.partyManagement.lookupParty(playerLeaving.getUniqueId());
                partyChat.partyManagement.sendPartyMessage("La party ha sido eliminada.", partyID);
                partyChat.partyManagement.deleteParty(partyID);
                return true;
            }
            case "kick": {
                if (args.length == 1 || args.length > 2) {
                    ChatUtils.sendErrorMessage(player, "Error. Usa: /party kick <nick>");
                    return true;
                }
                if (partyChat.partyManagement.lookupParty(player.getUniqueId()) == null) {
                    ChatUtils.sendErrorMessage(player, "No formas parte de ninguna party. Usa /party create para crear una.");
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(player.getUniqueId())
                        || partyChat.partyManagement.checkTrusted(player.getUniqueId())) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        ChatUtils.sendErrorMessage(player, "No se ha podido encontrar a &4" + args[1]);
                        return true;
                    }
                    Player playerToKick = Bukkit.getPlayerExact(args[1]);
                    String partyIDKickingPlayer = partyChat.partyManagement.lookupParty(playerToKick.getUniqueId());
                    String partyID = partyChat.partyManagement.lookupParty(player.getUniqueId());
                    if (!partyID.equals(partyIDKickingPlayer)) {
                        ChatUtils.sendErrorMessage(player, args[1] + " no forma parte de tu party.");
                        return true;
                    }
                    if (partyChat.partyManagement.isPlayerOwner(playerToKick.getUniqueId())) {
                        ChatUtils.sendErrorMessage(player, "No puedes expulsar al líder de la party");
                        return true;
                    }
                    if (player.getUniqueId().equals(playerToKick.getUniqueId())) {
                        ChatUtils.sendErrorMessage(player, "¿Qué intentas? No puedes kickearte a ti mismo, erudito.");
                        return true;
                    }
                    partyChat.partyManagement.sendPartyMessage(
                            playerToKick.getDisplayName() + " ha sido expulsado de la party por " + Bukkit.getPlayer(player.getUniqueId()).getDisplayName() + ".", partyID);
                    partyChat.partyManagement.removePlayerFromParty(playerToKick.getUniqueId(), partyID);
                    return true;
                }

                ChatUtils.sendErrorMessage(player, "No tienes permisos para expulsar miembros de la party.");
                return true;
            }
            case "transfer": {
                if (args.length == 1 || args.length > 2) {
                    ChatUtils.sendErrorMessage(player, "Error. Usa: /party transfer <nick>");
                    return true;
                }
                if (partyChat.partyManagement.lookupParty(player.getUniqueId()) == null) {
                    ChatUtils.sendErrorMessage(player, "No formas parte de ninguna party. Usa /party create para crear una.");
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(player.getUniqueId())) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        ChatUtils.sendErrorMessage(player, "No se ha podido encontrar a &4" + args[1]);
                        return true;
                    }
                    Player newOwner = Bukkit.getPlayerExact(args[1]);
                    String partyID = partyChat.partyManagement.lookupParty(player.getUniqueId());
                    partyChat.partyManagement.sendPartyMessage(
                            newOwner.getName() + " es el nuevo líder de la party.", partyID);
                    partyChat.partyManagement.updatePartyOwner(newOwner.getUniqueId(), partyID);
                    return true;
                }
                ChatUtils.sendErrorMessage(player, "Solo el líder de la party tiene permisos para ejecutar este comando.");
                return true;
            }
            case "info": {
                if (partyChat.partyManagement.lookupParty(player.getUniqueId()) == null) {
                    ChatUtils.sendErrorMessage(player, "No formas parte de ninguna party. Usa /party create para crear una.");
                    return true;
                }
                Bukkit.getPlayer(player.getUniqueId())
                        .sendMessage(ChatColor.GOLD + "--------------------------------------------");
                Bukkit.getPlayer(player.getUniqueId())
                        .sendMessage(ChatColor.DARK_AQUA + "Miembros: " + ChatColor.DARK_AQUA
                                + partyChat
                                        .partyManagement
                                        .listPartyMembers(partyChat.partyManagement.lookupParty(player.getUniqueId()))
                                        .size()
                                + " - ID: " + partyChat.partyManagement.lookupParty(player.getUniqueId()));
                ArrayList<UUID> players = partyChat.partyManagement.listPartyMembers(
                        partyChat.partyManagement.lookupParty(player.getUniqueId()));
                ArrayList<String> convertedPlayerNames = new ArrayList<>();
                UUID partyOwner =
                        partyChat.partyManagement.lookupOwner(partyChat.partyManagement.lookupParty(player.getUniqueId()));
                Bukkit.getScheduler().runTaskAsynchronously(partyChat, () -> {
                    for (UUID tempPlayer : players) {
                        if (tempPlayer.equals(partyOwner)) {
                            convertedPlayerNames.add(UUIDLookup.getName(tempPlayer) + " (Líder)");
                        } else if (partyChat.partyManagement.checkTrusted(tempPlayer)) {
                            convertedPlayerNames.add(UUIDLookup.getName(tempPlayer) + " (Autorizado)");
                        } else {
                            convertedPlayerNames.add(UUIDLookup.getName(tempPlayer));
                        }
                    }
                    for (String tempPlayer : convertedPlayerNames) {
                        Bukkit.getPlayer(player.getUniqueId()).sendMessage(ChatColor.DARK_AQUA + tempPlayer);
                    }
                    Bukkit.getPlayer(player.getUniqueId())
                            .sendMessage(ChatColor.GOLD + "--------------------------------------------");
                });
                return true;
            }
            case "trust": {
                if (args.length == 1 || args.length > 2) {
                    ChatUtils.sendErrorMessage(player, "Error. Usa: /party trust <nick>");
                    return true;
                }
                if (partyChat.partyManagement.lookupParty(player.getUniqueId()) == null) {
                    ChatUtils.sendErrorMessage(player, "No formas parte de ninguna party. Usa /party create para crear una.");
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(player.getUniqueId())) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        ChatUtils.sendErrorMessage(player, "No se ha podido encontrar a &4" + args[1]);
                        return true;
                    }
                    Player memberToTrust = Bukkit.getPlayerExact(args[1]);
                    String partyID = partyChat.partyManagement.lookupParty(player.getUniqueId());
                    String partyIDTrusted = partyChat.partyManagement.lookupParty(memberToTrust.getUniqueId());
                    if (!partyID.equals(partyIDTrusted)) {
                        ChatUtils.sendErrorMessage(player, args[1] + " no forma parte de tu party.");
                        return true;
                    }
                    if (player.getUniqueId().equals(memberToTrust.getUniqueId())) {
                        ChatUtils.sendErrorMessage(player, "No puedes darte permisos, ya eres el líder de la party.");
                        return true;
                    }
                    if (partyChat.partyManagement.checkTrusted(memberToTrust.getUniqueId())) {
                        ChatUtils.sendMessage(player, "El jugador &4" + args[1] + "&c ya está autorizado.");
                        return true;
                    }
                    partyChat.partyManagement.trustPlayer(memberToTrust.getUniqueId());
                    return true;
                }

                ChatUtils.sendErrorMessage(player, "Solo el líder de la party tiene permisos para ejecutar este comando.");
                return true;
            }
            case "untrust": {
                if (args.length == 1 || args.length > 2) {
                    ChatUtils.sendErrorMessage(player, "Error. Usa: /party trust <nick>");
                    return true;
                }
                if (partyChat.partyManagement.lookupParty(player.getUniqueId()) == null) {
                    ChatUtils.sendErrorMessage(player, "No formas parte de ninguna party. Usa /party create para crear una.");
                    return true;
                }
                if (partyChat.partyManagement.isPlayerOwner(player.getUniqueId())) {
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        ChatUtils.sendErrorMessage(player, "No se ha podido encontrar a &4" + args[1]);
                        return true;
                    }
                    Player memberToTrust = Bukkit.getPlayerExact(args[1]);
                    String partyID = partyChat.partyManagement.lookupParty(player.getUniqueId());
                    String partyIDTrusted = partyChat.partyManagement.lookupParty(memberToTrust.getUniqueId());
                    if (!partyID.equals(partyIDTrusted)) {
                        ChatUtils.sendErrorMessage(player, args[1] + " no forma parte de tu party.");
                        return true;
                    }
                    if (player.getUniqueId().equals(memberToTrust.getUniqueId())) {
                        ChatUtils.sendErrorMessage(player, "No puedes quitarte permisos, ya eres el líder de la party.");
                        return true;
                    }
                    if (!partyChat.partyManagement.checkTrusted(memberToTrust.getUniqueId())) {
                        ChatUtils.sendMessage(player, "El jugador &4" + args[1] + "&c ya está no autorizado.");
                        return true;
                    }
                    partyChat.partyManagement.removeTrustedPlayer(memberToTrust.getUniqueId());
                    return true;
                }
                ChatUtils.sendErrorMessage(player, "Solo el líder de la party tiene permisos para ejecutar este comando.");
                return true;
            }
            default: {
                ChatUtils.sendErrorMessage(player, "Comando inválido. Usa /party help para obtener ayuda.");
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList(
                    "create",
                    "invite",
                    "accept",
                    "deny",
                    "kick",
                    "leave",
                    "disband",
                    "info",
                    "transfer",
                    "help",
                    "trust",
                    "untrust");
        } else {
            return null;
        }
    }
}
