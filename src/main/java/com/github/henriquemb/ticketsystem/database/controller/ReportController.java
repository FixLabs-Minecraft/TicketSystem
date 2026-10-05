package com.github.henriquemb.ticketsystem.database.controller;

import com.github.henriquemb.ticketsystem.database.Database;
import com.github.henriquemb.ticketsystem.database.model.ReportModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ReportController {
    private static ReportModel map(ResultSet rs) throws SQLException {
        return new ReportModel(
                rs.getInt("id"),
                rs.getString("player"),
                rs.getString("reported"),
                rs.getString("reason"),
                rs.getString("evidence"),
                rs.getBoolean("verified"),
                rs.getString("verifiedBy"),
                rs.getTimestamp("verifiedAt"),
                rs.getInt("status"),
                rs.getTimestamp("timestamp")
        );
    }

    public int create(String player, String reported, String evidence, String reason) {
        return Database.insert("INSERT INTO report (player, reported, reason, evidence) VALUES (?, ?, ?, ?)", st -> {
            st.setString(1, player);
            st.setString(2, reported);
            st.setString(3, reason);
            st.setString(4, evidence);
        });
    }

    public void delete(int id) {
        Database.update("DELETE FROM report WHERE id = ?", st -> st.setInt(1, id));
    }

    public void update(ReportModel report) {
        Database.update("UPDATE report SET reason = ?, evidence = ?, verified = ?, verifiedBy = ?, verifiedAt = ?, status = ? WHERE id = ?", st -> {
            st.setString(1, report.getReason());
            st.setString(2, report.getEvidence());
            st.setBoolean(3, report.isVerified());
            st.setString(4, report.getVerifiedBy());
            st.setTimestamp(5, report.getVerifiedAt());
            st.setInt(6, report.getStatus());
            st.setInt(7, report.getId());
        });
    }

    public ReportModel fetchById(int id) {
        if (id <= 0) return null;
        return Database.queryOne("SELECT * FROM report WHERE id = ?", st -> st.setInt(1, id), ReportController::map);
    }

    public List<ReportModel> fetchAll() {
        return Database.query("SELECT * FROM report", ReportController::map);
    }

    /**
     * Нерассмотренные жалобы на игроков из списка (обычно — на тех, кто сейчас в сети).
     */
    public List<ReportModel> fetchNotVerified(List<String> players) {
        return fetchNotVerified().stream().filter(r -> players.contains(r.getReported())).toList();
    }

    public List<ReportModel> fetchNotVerified() {
        return Database.query("SELECT * FROM report WHERE NOT verified", ReportController::map);
    }

    public List<ReportModel> fetchByPlayer(String player) {
        return Database.query("SELECT * FROM report WHERE player = ?", st -> st.setString(1, player), ReportController::map);
    }

    public int countOpenByPlayer(String player) {
        return Database.count("SELECT COUNT(*) FROM report WHERE player = ? AND NOT verified", st -> st.setString(1, player));
    }
}
