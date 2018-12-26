/*
 * Groovy Bot - The core component of the Groovy Discord music bot
 *
 * Copyright (C) 2018  Oskar Lang & Michael Rittmeister & Sergej Herdt & Yannick Seeger & Justus Kliem & Leon Kappes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package co.groovybot.bot.commands.settings;

import co.groovybot.bot.core.command.*;
import co.groovybot.bot.core.command.permission.Permissions;
import co.groovybot.bot.core.entity.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

public class BlackListCommand extends Command {

    public BlackListCommand() {
        super(new String[]{"blacklist", "bl"}, CommandCategory.SETTINGS, Permissions.adminOnly(), "Lets you block specific textchannels", "");
        registerSubCommand(new AddCommand());
        registerSubCommand(new RemoveCommand());
    }

    @Override
    public Result run(String[] args, CommandEvent event) {
        if (args.length == 0)
            if (event.getGroovyGuild().hasBlacklistedChannels()) {
                Guild guild = event.getBot().getGuildCache().get(event.getGuild().getIdLong());

                if (!guild.hasBlacklistedChannels())
                    return send(error(event.translate("command.blacklist.nochannels.title"), event.translate("command.blacklist.nochannels.description")));

                StringBuilder channelNames = new StringBuilder();
                guild.getBlacklistedChannels().forEach(channelObject -> {
                    long channelId = Long.valueOf(channelObject.toString());
                    TextChannel channel = event.getGuild().getTextChannelById(channelId);

                    if (channel == null) {
                        guild.unBlacklistChannel(channelId);
                        return;
                    }

                    channelNames.append(channel.getAsMention()).append(", ");
                });

                if (channelNames.toString().equals(""))
                    return send(error(event.translate("command.blacklist.nochannels.title"), event.translate("command.blacklist.nochannels.description")));

                channelNames.replace(channelNames.lastIndexOf(", "), channelNames.lastIndexOf(", ") + 1, "");
                return send(info(event.translate("command.blacklist.list.title"), String.format(event.translate("command.blacklist.list.description"), channelNames.toString())));
            }
        return sendHelp();
    }

    private class AddCommand extends SubCommand {

        public AddCommand() {
            super(new String[]{"add"}, Permissions.adminOnly(), "Lets you add a textchannel to the blacklist", "<#channel>");
        }

        @Override
        public Result run(String[] args, CommandEvent event) {
            final List<TextChannel> mentionedChannels = event.getMessage().getMentionedChannels();
            if (mentionedChannels.isEmpty())
                return sendHelp();
            TextChannel target = mentionedChannels.get(0);
            Guild guild = event.getBot().getGuildCache().get(event.getGuild().getIdLong());
            if (guild.isChannelBlacklisted(target.getIdLong()))
                return send(error(event.translate("command.blacklist.alreadyblacklisted.title"), event.translate("command.blacklist.alreadyblacklisted.description")));
            if (guild.getBotChannel() == target.getIdLong())
                return send(error(event.translate("command.blacklist.isbotchannel.title"), event.translate("command.blacklist.isbotchannel.description")));
            guild.blacklistChannel(target.getIdLong());
            return send(success(event.translate("command.blacklist.added.title"), String.format(event.translate("command.blacklist.added.description"), target.getAsMention())));
        }
    }

    private class RemoveCommand extends SubCommand {

        public RemoveCommand() {
            super(new String[]{"remove", "rm"}, Permissions.adminOnly(), "Lets you remove a textchannel to the blacklist", "<#channel>");
        }

        @Override
        public Result run(String[] args, CommandEvent event) {
            final List<TextChannel> mentionedChannels = event.getMessage().getMentionedChannels();
            if (mentionedChannels.isEmpty())
                return sendHelp();
            TextChannel target = mentionedChannels.get(0);
            Guild guild = event.getBot().getGuildCache().get(event.getGuild().getIdLong());
            if (!guild.isChannelBlacklisted(target.getIdLong()))
                return send(error(event.translate("command.blacklist.notblacklisted.title"), event.translate("command.blacklist.notblacklisted.description")));
            guild.unBlacklistChannel(target.getIdLong());
            return send(success(event.translate("command.blacklist.removed.title"), String.format(event.translate("command.blacklist.removed.description"), target.getAsMention())));
        }
    }
}
