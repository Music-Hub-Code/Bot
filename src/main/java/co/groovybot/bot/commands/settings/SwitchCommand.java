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

import co.groovybot.bot.core.audio.MusicPlayer;
import co.groovybot.bot.core.command.CommandCategory;
import co.groovybot.bot.core.command.CommandEvent;
import co.groovybot.bot.core.command.Result;
import co.groovybot.bot.core.command.permission.Permissions;
import co.groovybot.bot.core.command.voice.InChannelCommand;

public class SwitchCommand extends InChannelCommand {
    public SwitchCommand() {
        super(new String[]{"switch"}, CommandCategory.SETTINGS, Permissions.adminOnly(), "Lets you to switch the text-channel or/and the voicechannel of Groovy", "");
    }

    @Override
    public Result execute(String[] args, CommandEvent event, MusicPlayer player) {
        if (player.getChannel() == event.getChannel())
            return send(error(event.translate("command.switch.already.title"), event.translate("command.switch.already.description")));

        player.setChannel(event.getChannel());

        if (event.getMember().getVoiceState().getChannel() != event.getGuild().getSelfMember().getVoiceState().getChannel()) {
            player.connect(event.getMember().getVoiceState().getChannel());
            return send(success(event.translate("command.switch.voice.title"), event.translate("command.switch.voice.description")));
        }

        return send(success(event.translate("command.switch.title"), event.translate("command.switch.description")));
    }
}
