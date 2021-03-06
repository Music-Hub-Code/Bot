/*
 * Groovy Bot - The core component of the Groovy Discord music bot
 *
 * Copyright (C) 2018  Oskar Lang & Michael Rittmeister & Sergeij Herdt & Yannick Seeger & Justus Kliem & Leon Kappes
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

package co.groovybot.bot.commands.music;

import co.groovybot.bot.core.audio.MusicPlayer;
import co.groovybot.bot.core.command.CommandCategory;
import co.groovybot.bot.core.command.CommandEvent;
import co.groovybot.bot.core.command.Result;
import co.groovybot.bot.core.command.permission.Permissions;
import co.groovybot.bot.core.command.voice.SameChannelCommand;

public class LeaveCommand extends SameChannelCommand {

    public LeaveCommand() {
        super(new String[]{"leave", "l"}, CommandCategory.MUSIC, Permissions.djMode(), "Lets Groovy disconnect from your channel", "");
    }

    @Override
    public Result runCommand(String[] args, CommandEvent event, MusicPlayer player) {
        if (player.getGuild().getId().equals("403882830225997825") && !Permissions.ownerOnly().isCovered(event.getPermissions(), event))
            return send(error(event.translate("phrases.nopermission"), "You are not allowed to let Groovy disconnect from this channel!"));
        player.setPreviousTrack(player.getPlayer().getPlayingTrack());
        player.leave();
        return send(success(event.translate("phrases.success"), String.format(event.translate("command.leave"), event.getMember().getVoiceState().getChannel().getName())));
    }
}
