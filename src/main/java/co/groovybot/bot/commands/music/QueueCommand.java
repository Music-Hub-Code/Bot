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
import co.groovybot.bot.core.command.Command;
import co.groovybot.bot.core.command.CommandCategory;
import co.groovybot.bot.core.command.CommandEvent;
import co.groovybot.bot.core.command.Result;
import co.groovybot.bot.core.command.interaction.InteractableMessage;
import co.groovybot.bot.core.command.permission.Permissions;
import co.groovybot.bot.util.Colors;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueCommand extends Command {

    private final int PAGE_SIZE = 10;

    public QueueCommand() {
        super(new String[]{"queue", "q"}, CommandCategory.MUSIC, Permissions.everyone(), "Shows you a list of all queued songs", "");
    }

    public static EmbedBuilder formatQueue(List<AudioTrack> tracks, CommandEvent event, int startNumber, AudioTrack currentTrack, int currentPage, int totalPages) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("🎶 " + String.format(event.translate("command.queue.title"), event.getBot().getMusicPlayerManager().getPlayer(event.getGuild(), event.getChannel()).getTrackQueue().size()))
                .setDescription(generateQueueDescription(tracks, startNumber, currentTrack))
                .setColor(Colors.DARK_BUT_NOT_BLACK);
        if (currentPage != 0 && totalPages != 0)
            builder.setFooter(String.format("%s %s", event.translate("phrases.text.site"), String.format("%s/%s", currentPage, totalPages)), null);
        return builder;
    }

    private static String generateQueueDescription(List<AudioTrack> tracks, int startNumber, AudioTrack currentTrack) {
        StringBuilder queueMessage = new StringBuilder();
        AtomicInteger trackCount = new AtomicInteger(startNumber);
        if (currentTrack != null)
            queueMessage.append(String.format("**[Now]** [%s](%s) - %s\n\n", currentTrack.getInfo().title, currentTrack.getInfo().uri, currentTrack.getInfo().author));
        tracks.forEach(track -> queueMessage.append(String.format("▫ `%s.` [%s](%s) - %s\n", trackCount.addAndGet(1), track.getInfo().title != null ? track.getInfo().title : "none", track.getInfo().uri != null ? track.getInfo().uri : "none", track.getInfo().author != null ? track.getInfo().author : "none")));
        return queueMessage.toString();
    }

    @Override
    public Result run(String[] args, CommandEvent event) {
        MusicPlayer player = event.getBot().getMusicPlayerManager().getPlayer(event.getGuild(), event.getChannel());
        if (!player.isPlaying())
            return send(error(event.translate("phrases.notplaying.title"), event.translate("phrases.notplaying.description")));
        if (player.getQueueSize() <= PAGE_SIZE)
            return new Result(formatQueue((LinkedList<AudioTrack>) player.getTrackQueue(), event, 0, player.getPlayer().getPlayingTrack(), 1, 1));
        if (!event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_MANAGE))
            return send(error(event.translate("phrases.nopermission.title"), event.translate("phrases.nopermission.manage")));
        Message infoMessage = sendMessageBlocking(event.getChannel(), noTitle(event.translate("phrases.loading")));
        new QueueMessage(infoMessage, event.getChannel(), event.getMember(), player.getTrackQueue(), event, player.getPlayer().getPlayingTrack());
        return null;
    }

    private class QueueMessage extends InteractableMessage {

        private final Queue<AudioTrack> queue;
        private final int pages;
        private final CommandEvent commandEvent;
        private final AudioTrack currentTrack;
        private int currentPage = 1;

        private QueueMessage(Message infoMessage, TextChannel channel, Member author, Queue<AudioTrack> queue, CommandEvent event, AudioTrack currentTrack) {
            super(infoMessage, channel, author, infoMessage.getIdLong());
            this.queue = queue;
            this.pages = queue.size() >= PAGE_SIZE ? queue.size() / PAGE_SIZE : 1;
            this.commandEvent = event;
            this.currentTrack = currentTrack;
            updateEmotes(true);
            updateMessage();
        }

        @Override
        protected void handleReaction(GuildMessageReactionAddEvent event) {
            switch (event.getReaction().getReactionEmote().getName()) {
                case "➡":
                    currentPage++;
                    break;
                case "⬅":
                    currentPage--;
                    break;
                default:
                    break;
            }
            updateEmotes(false);
            updateMessage();
            update();
        }

        private void updateMessage() {
            List<AudioTrack> subQueue = ((LinkedList<AudioTrack>) queue).subList((currentPage - 1) * PAGE_SIZE, ((currentPage - 1) * PAGE_SIZE + PAGE_SIZE) > queue.size() ? queue.size() : (currentPage - 1) * PAGE_SIZE + PAGE_SIZE);
            editMessage(getInfoMessage(), formatQueue(subQueue, commandEvent, (currentPage * PAGE_SIZE - 10), currentPage == 1 ? currentTrack : null, currentPage, pages + 1));
        }

        private void updateEmotes(boolean first) {
            if (!first && currentPage == 1)
                getChannel().removeReactionById(getInfoMessage().getIdLong(), "⬅").queue();
            if (currentPage > pages)
                getChannel().removeReactionById(getInfoMessage().getIdLong(), "➡").queue();
            if (currentPage > 1)
                getInfoMessage().addReaction("⬅").queue();
            if (currentPage <= pages) {
                getInfoMessage().addReaction("➡").queue();
            }
        }
    }
}
