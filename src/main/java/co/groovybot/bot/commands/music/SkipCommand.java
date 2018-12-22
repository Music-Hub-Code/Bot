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

public class SkipCommand extends SameChannelCommand {
    public SkipCommand() {
        super(new String[]{"skip", "s", "next"}, CommandCategory.MUSIC, Permissions.djMode(), "Lets you skip the current/to a specific track", "[position]");
    }

    @Override
    public Result runCommand(String[] args, CommandEvent event, MusicPlayer player) {
        if (!player.isPlaying())
            return send(error(event.translate("phrases.notplaying.title"), event.translate("phrases.notplaying.description")));

        player.setPreviousTrack(player.getPlayer().getPlayingTrack());

        int skipTo;

        if (args.length == 0)
            skipTo = 1;
        else {
            try {
                skipTo = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                return send(error(event.translate("phrases.invalidnumber.title"), event.translate("phrases.invalidnumber.description")));
            }
        }

        player.skipTo(skipTo);

        if (args.length > 0)
            return send(success(event.translate("command.skip.success.title"), String.format(event.translate("command.skip.success.more.description"), skipTo)));

        return send(success(event.translate("command.skip.success.title"), (event.translate("command.skip.success.one.description"))));
    }
}
