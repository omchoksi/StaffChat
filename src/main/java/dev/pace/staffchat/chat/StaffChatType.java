package dev.pace.staffchat.chat;

import dev.pace.staffchat.StaffChat;
import dev.pace.staffchat.utils.DiscordWebhook;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;

/**
 * Created by Pace
 * https://www.spigotmc.org/resources/1-7-1-17-staff-chat.92585/
 */

public interface StaffChatType {

    String getCommand();
    String getToggleCommand();
    String getLockCommand();
    String getPrefix();
    String getPermission();
    String getType();

    default boolean sendChatMessage(final Player player, final String message) {
        // Fix messages sending even if no permission.
        if (player.hasPermission("staff.staffchat") || player.hasPermission("staff.developerchat") || player.hasPermission("staff.adminchat")) {
            sendWebhook(getPrefix(), player.getName(), message);
        }
        if (!player.hasPermission(getPermission()) && !player.isOp()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', StaffChat.getInstance().getConfig().getString(getPrefix() + ".error")));
            return false;
        }

        if (message.equals("")) {
            player.sendMessage("§cUsage:§7 /" + getPrefix() + " <message>");
            return false;
        }

        if (!StaffChat.getInstance().isChatEnabled(player, this)) {
            player.sendMessage("§7Do /" + getType() + "chatdisable to talk in " + getType() + " chat!");
            return true;
        }

        final boolean isPapi = StaffChat.getInstance().getPapiEnabled().get();
        final String header = StaffChat.getInstance().getConfig().getString(getPrefix() + ".header");
        final String placeholder = StaffChat.getInstance().getConfig().getString(getPrefix() + ".placeholder.name");

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission(getPermission())) {
                if (!StaffChat.getInstance().toggleTable.contains(staff.getUniqueId(), getType()))
                    StaffChat.getInstance().toggleTable.put(staff.getUniqueId(), getType(), true);
                if (StaffChat.getInstance().toggleTable.get(staff.getUniqueId(), getType())) {
                    String sendMessage = ChatColor.translateAlternateColorCodes('&', header) +
                            (isPapi ? placeholder : player.getName())
                            + ": "
                            + message;

                    if (isPapi) {
                        staff.sendMessage(PlaceholderAPI.setPlaceholders(player, sendMessage));
                    } else {
                        staff.sendMessage(sendMessage);
                    }
                }
            }
        }
        return true;
    }

    // Discord send webhook.
     static void sendWebhook(String prefix, String name, String message) {
        if (!StaffChat.getInstance().getConfig().getBoolean(prefix + ".discordwebhook.enabled")) return;

        DiscordWebhook discordWebhook = new DiscordWebhook(StaffChat.getInstance().getConfig().getString(prefix + ".discordwebhook.webhook"));
        discordWebhook.setUsername(StaffChat.getInstance().getConfig().getString(prefix + ".discordwebhook.webhookusername"));
        discordWebhook.addEmbed(new DiscordWebhook.EmbedObject().setDescription(name + ": " + message).setColor(Color.RED).setFooter(StaffChat.getInstance().getConfig().getString(prefix + ".discordwebhook.footer"), StaffChat.getInstance().getConfig().getString(prefix + ".discordwebhook.footericon")));
        try {
            discordWebhook.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
