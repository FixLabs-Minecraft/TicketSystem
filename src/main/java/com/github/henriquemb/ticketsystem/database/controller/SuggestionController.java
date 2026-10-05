package com.github.henriquemb.ticketsystem.database.controller;

import com.github.henriquemb.ticketsystem.database.Database;
import com.github.henriquemb.ticketsystem.database.model.SuggestionModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SuggestionController {
    private static SuggestionModel map(ResultSet rs) throws SQLException {
        return new SuggestionModel(
                rs.getInt("id"),
                rs.getString("player"),
                rs.getString("suggestion"),
                rs.getString("response"),
                rs.getString("respondedBy"),
                rs.getTimestamp("respondedAt"),
                rs.getBoolean("send"),
                rs.getTimestamp("timestamp")
        );
    }

    private List<SuggestionModel> searchByPlayer(String sql, String player) {
        return Database.query(sql, st -> st.setString(1, player), SuggestionController::map);
    }

    public void create(String player, String suggestion) {
        Database.insert("INSERT INTO suggestion (player, suggestion) VALUES (?, ?)", st -> {
            st.setString(1, player);
            st.setString(2, suggestion);
        });
    }

    public void delete(int id) {
        Database.update("DELETE FROM suggestion WHERE id = ?", st -> st.setInt(1, id));
    }

    public void update(SuggestionModel suggestion) {
        Database.update("UPDATE suggestion SET response = ?, respondedBy = ?, respondedAt = ?, send = ? WHERE id = ?", st -> {
            st.setString(1, suggestion.getResponse());
            st.setString(2, suggestion.getRespondedBy());
            st.setTimestamp(3, suggestion.getRespondedAt());
            st.setBoolean(4, suggestion.getSend());
            st.setInt(5, suggestion.getId());
        });
    }

    public SuggestionModel fetchById(int id) {
        if (id <= 0) return null;
        return Database.queryOne("SELECT * FROM suggestion WHERE id = ?", st -> st.setInt(1, id), SuggestionController::map);
    }

    public List<SuggestionModel> fetchAll() {
        return Database.query("SELECT * FROM suggestion", SuggestionController::map);
    }

    public List<SuggestionModel> fetchNotAnswered() {
        return Database.query("SELECT * FROM suggestion WHERE response IS NULL", SuggestionController::map);
    }

    public List<SuggestionModel> fetchAnsweredBy(String player) {
        return searchByPlayer("SELECT * FROM suggestion WHERE respondedBy = ?", player);
    }

    public List<SuggestionModel> fetchNotSendToPlayer(String player) {
        return searchByPlayer("SELECT * FROM suggestion WHERE player = ? AND NOT send AND response IS NOT NULL", player);
    }

    public int countOpenByPlayer(String player) {
        return Database.count("SELECT COUNT(*) FROM suggestion WHERE player = ? AND response IS NULL", st -> st.setString(1, player));
    }
}
