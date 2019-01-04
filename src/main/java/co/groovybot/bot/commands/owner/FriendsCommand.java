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

package co.groovybot.bot.commands.owner;

import co.groovybot.bot.core.command.*;
import co.groovybot.bot.core.command.permission.Permissions;
import co.groovybot.bot.core.entity.User;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Log4j2
public class FriendsCommand extends Command {
    public FriendsCommand() {
        super(new String[]{"friends", "friend", "f"}, CommandCategory.DEVELOPER, Permissions.ownerOnly(), "Lets you add some friends!", "");
        registerSubCommand(new AddCommand());
        registerSubCommand(new RemoveCommand());
    }

    @Override
    public Result run(String[] args, CommandEvent event) {
        try {
            Connection connection = event.getBot().getPostgreSQL().getDataSource().getConnection();

            PreparedStatement friendsStatement = connection.prepareStatement("SELECT user_id FROM users WHERE friend = TRUE");
            ResultSet friendsResult = friendsStatement.executeQuery();

            StringBuilder friendsNames = new StringBuilder();

            while (friendsResult.next()) {
                net.dv8tion.jda.core.entities.User friend = event.getBot().getShardManager().getUserById(friendsResult.getLong("user_id"));
                friendsNames.append(friend.getAsMention()).append(", ");
            }

            if (friendsNames.toString().equals(""))
                return send(error(event.translate("command.friends.nofriends.title"), event.translate("command.friends.nofriends.description")));

            friendsNames.replace(friendsNames.lastIndexOf(", "), friendsNames.lastIndexOf(", ") + 1, "");
            return send(info(event.translate("command.friends.list.title"), friendsNames.toString()));
        } catch (SQLException e) {
            log.error("[FriendsCommand] Error while querying all friends!", e);
            return send(error(event));
        }
    }

    private class AddCommand extends SubCommand {

        public AddCommand() {
            super(new String[]{"add"}, Permissions.ownerOnly(), "Lets you add an user to your friends!", "<@user>");
        }

        @Override
        public Result run(String[] args, CommandEvent event) {
            if (event.getMessage().getMentionedMembers().size() == 0)
                return send(error(event.translate("command.friends.nomention.title"), event.translate("command.friends.nomention.description")));
            User user = event.getBot().getUserCache().get(event.getMessage().getMentionedMembers().get(0).getUser().getIdLong());
            if (!user.isFriend()) {
                user.setFriend(true);
                return send(success(event.translate("command.friends.added.title"), String.format(event.translate("command.friends.added.description"), event.getMessage().getMentionedMembers().get(0).getAsMention())));
            }
            return send(error(event.translate("command.friends.already.title"), String.format(event.translate("command.friends.already.description"), event.getMessage().getMentionedMembers().get(0).getAsMention())));
        }
    }

    private class RemoveCommand extends SubCommand {

        public RemoveCommand() {
            super(new String[]{"remove", "rm", "delete"}, Permissions.ownerOnly(), "Lets you remove an user from your friends!", "<@user>");
        }

        @Override
        public Result run(String[] args, CommandEvent event) {
            if (event.getMessage().getMentionedMembers().size() == 0)
                return send(error(event.translate("command.friends.nomention.title"), event.translate("command.friends.nomention.description")));
            User user = event.getBot().getUserCache().get(event.getMessage().getMentionedMembers().get(0).getUser().getIdLong());
            if (user.isFriend()) {
                user.setFriend(false);
                return send(success(event.translate("command.friends.removed.title"), String.format(event.translate("command.friends.removed.description"), event.getMessage().getMentionedMembers().get(0).getAsMention())));
            }
            return send(error(event.translate("command.friends.not.title"), String.format(event.translate("command.friends.not.description"), event.getMessage().getMentionedMembers().get(0).getAsMention())));
        }
    }
}