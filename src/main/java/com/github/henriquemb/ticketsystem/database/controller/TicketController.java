package com.github.henriquemb.ticketsystem.database.controller;

import com.github.henriquemb.ticketsystem.database.Database;
import com.github.henriquemb.ticketsystem.database.model.TicketModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TicketController {
    private static TicketModel map(ResultSet rs) throws SQLException {
        return new TicketModel(
                rs.getInt("id"),
                rs.getString("player"),
                rs.getString("request"),
                rs.getString("response"),
                rs.getString("respondedBy"),
                rs.getTimestamp("respondedAt"),
                rs.getDouble("rating"),
                rs.getBoolean("send"),
                rs.getTimestamp("timestamp")
        );
    }

    private List<TicketModel> searchByPlayer(String sql, String player) {
        return Database.query(sql, st -> st.setString(1, player), TicketController::map);
    }

    public int create(String player, String request) {
        return Database.insert("INSERT INTO ticket (player, request) VALUES (?, ?)", st -> {
            st.setString(1, player);
            st.setString(2, request);
        });
    }

    public void delete(int id) {
        Database.update("DELETE FROM ticket WHERE id = ?", st -> st.setInt(1, id));
    }

    public void update(TicketModel ticket) {
        Database.update("UPDATE ticket SET response = ?, respondedBy = ?, respondedAt = ?, rating = ?, send = ? WHERE id = ?", st -> {
            st.setString(1, ticket.getResponse());
            st.setString(2, ticket.getRespondedBy());
            st.setTimestamp(3, ticket.getRespondedAt());
            st.setDouble(4, ticket.getRating());
            st.setBoolean(5, ticket.getSend());
            st.setInt(6, ticket.getId());
        });
    }

    public TicketModel fetchById(int id) {
        if (id <= 0) return null;
        return Database.queryOne("SELECT * FROM ticket WHERE id = ?", st -> st.setInt(1, id), TicketController::map);
    }

    public List<TicketModel> fetchAll() {
        return Database.query("SELECT * FROM ticket", TicketController::map);
    }

    public List<TicketModel> fetchNotAnswered() {
        return Database.query("SELECT * FROM ticket WHERE response IS NULL", TicketController::map);
    }

    public List<TicketModel> fetchAllAnswered() {
        return Database.query("SELECT * FROM ticket WHERE response IS NOT NULL", TicketController::map);
    }

    public List<TicketModel> fetchAnsweredBy(String player) {
        return searchByPlayer("SELECT * FROM ticket WHERE respondedBy = ?", player);
    }

    public List<TicketModel> fetchNotSendToPlayer(String player) {
        return searchByPlayer("SELECT * FROM ticket WHERE player = ? AND NOT send AND response IS NOT NULL", player);
    }

    /**
     * Все тикеты игрока, новые сверху.
     */
    public List<TicketModel> fetchByPlayer(String player) {
        return searchByPlayer("SELECT * FROM ticket WHERE player = ? ORDER BY id DESC", player);
    }

    public int countNotAnswered() {
        return Database.count("SELECT COUNT(*) FROM ticket WHERE response IS NULL", st -> { });
    }

    public int countOpenByPlayer(String player) {
        return Database.count("SELECT COUNT(*) FROM ticket WHERE player = ? AND response IS NULL", st -> st.setString(1, player));
    }
}
