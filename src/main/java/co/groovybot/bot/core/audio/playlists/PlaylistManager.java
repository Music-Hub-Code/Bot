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

package co.groovybot.bot.core.audio.playlists;

import co.groovybot.bot.core.entity.Playlist;
import com.relops.snowflake.Snowflake;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class PlaylistManager {

    private final HikariDataSource dataSource;
    private final Snowflake generator = new Snowflake(1);

    public Playlist createPlaylist(String name, Long ownerId, List<AudioTrack> tracks) {
        return new Playlist(name, generator.next(), ownerId, tracks);
    }

    public Map<String, Playlist> getPlaylist(Long authorId) {
        Map<String, Playlist> playlists = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM playlists WHERE author_id = ?");
            ps.setLong(1, authorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Playlist playlist = new Playlist(rs);
                playlists.put(playlist.getName().toLowerCase(), playlist);
            }
        } catch (SQLException e) {
            log.error("[Playlist] Error while retrieving playlist", e);
        }
        return playlists;
    }

    public Playlist getPlaylistById(Long id) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM playlists WHERE id = ?");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Playlist(rs);
        } catch (SQLException e) {
            log.error("[Playlist] Error while retrieving playlist", e);
        }
        return null;
    }

    public void deletePlaylist(String name, Long authorId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM playlists WHERE author_id = ? AND name = ?");
            ps.setLong(1, authorId);
            ps.setString(2, name);
            ps.execute();
        } catch (SQLException e) {
            log.error("[Playlist] Error while deleting playlist", e);
        }
    }

    public Map<Integer, Playlist> getTopPlaylists() {
        Map<Integer, Playlist> topPlaylists = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM playlists WHERE public = TRUE ORDER BY count DESC LIMIT 3");
            ResultSet rs = ps.executeQuery();
            int rank = 1;
            while (rs.next()) {
                Playlist playlist = new Playlist(rs);
                topPlaylists.put(rank, playlist);
                rank++;
            }
        } catch (SQLException e) {
            log.error("[Playlist] Error while retrieving playlist", e);
        }
        return topPlaylists;
    }
}
