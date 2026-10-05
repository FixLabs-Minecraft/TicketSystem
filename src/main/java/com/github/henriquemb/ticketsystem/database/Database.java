package com.github.henriquemb.ticketsystem.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Подключение к SQLite (файл database.db) или MySQL (пул HikariCP) и простые обёртки для запросов.
 */
public final class Database {
    @FunctionalInterface
    public interface Binder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    public interface Mapper<T> {
        T map(ResultSet resultSet) throws SQLException;
    }

    private static final Binder NO_PARAMS = statement -> { };

    private static Logger logger;
    private static HikariDataSource dataSource;
    private static String sqliteUrl;
    private static Properties sqliteProperties;

    private Database() {
    }

    public static void connect(ConfigurationSection config, File dataFolder, Logger log) {
        close();
        logger = log;

        String type = config.getString("type", "sqlite");
        if ("mysql".equalsIgnoreCase(type)) {
            HikariConfig hikari = new HikariConfig();
            hikari.setPoolName("TicketSystem");
            hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");
            hikari.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?%s",
                    config.getString("mysql.host", "localhost"),
                    config.getInt("mysql.port", 3306),
                    config.getString("mysql.database", "ticketsystem"),
                    config.getString("mysql.parameters", "useSSL=false&characterEncoding=utf8")));
            hikari.setUsername(config.getString("mysql.username", "root"));
            hikari.setPassword(config.getString("mysql.password", ""));
            hikari.setMaximumPoolSize(Math.max(1, config.getInt("mysql.pool-size", 5)));
            hikari.setConnectionTimeout(10_000);

            dataSource = new HikariDataSource(hikari);
            logger.info("Подключено к MySQL");
        }
        else {
            if (!"sqlite".equalsIgnoreCase(type))
                logger.warning("Неизвестный тип базы данных '" + type + "', используется sqlite");

            sqliteUrl = "jdbc:sqlite:" + new File(dataFolder, "database.db").getAbsolutePath();
            SQLiteConfig sqLiteConfig = new SQLiteConfig();
            sqLiteConfig.setBusyTimeout(5000);
            sqliteProperties = sqLiteConfig.toProperties();
            sqliteProperties.setProperty(SQLiteConfig.Pragma.DATE_STRING_FORMAT.pragmaName, "yyyy-MM-dd HH:mm:ss");
        }

        createTables();
    }

    public static void close() {
        if (dataSource != null) dataSource.close();
        dataSource = null;
        sqliteUrl = null;
    }

    public static boolean isMySQL() {
        return dataSource != null;
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource != null) return dataSource.getConnection();
        if (sqliteUrl == null) throw new SQLException("База данных не подключена");
        return DriverManager.getConnection(sqliteUrl, sqliteProperties);
    }

    public static <T> List<T> query(String sql, Mapper<T> mapper) {
        return query(sql, NO_PARAMS, mapper);
    }

    public static <T> List<T> query(String sql, Binder binder, Mapper<T> mapper) {
        List<T> result = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) result.add(mapper.map(rs));
            }
        }
        catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка запроса к базе данных: " + sql, e);
        }
        return result;
    }

    public static <T> T queryOne(String sql, Binder binder, Mapper<T> mapper) {
        List<T> result = query(sql, binder, mapper);
        return result.isEmpty() ? null : result.getFirst();
    }

    public static int count(String sql, Binder binder) {
        Integer count = queryOne(sql, binder, rs -> rs.getInt(1));
        return count == null ? 0 : count;
    }

    /**
     * Выполняет INSERT и возвращает сгенерированный ID (0 при ошибке).
     */
    public static int insert(String sql, Binder binder) {
        try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            binder.bind(statement);
            statement.executeUpdate();
            try (ResultSet rs = statement.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
        catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка записи в базу данных: " + sql, e);
            return 0;
        }
    }

    public static void update(String sql, Binder binder) {
        try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            binder.bind(statement);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка записи в базу данных: " + sql, e);
        }
    }

    private static void createTables() {
        String id = isMySQL() ? "INT PRIMARY KEY AUTO_INCREMENT" : "INTEGER PRIMARY KEY AUTOINCREMENT";
        String suffix = isMySQL() ? " DEFAULT CHARSET=utf8mb4" : "";

        String[] tables = {
                "CREATE TABLE IF NOT EXISTS ticket (id " + id + ", player VARCHAR(30) NOT NULL, request TEXT NOT NULL, response TEXT, respondedBy VARCHAR(30), respondedAt DATETIME, rating DOUBLE DEFAULT 0, send BOOLEAN DEFAULT FALSE, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)" + suffix,
                "CREATE TABLE IF NOT EXISTS report (id " + id + ", player VARCHAR(30) NOT NULL, reported VARCHAR(30) NOT NULL, reason TEXT, evidence TEXT, verified BOOLEAN DEFAULT FALSE, verifiedBy VARCHAR(30), verifiedAt DATETIME, status INTEGER DEFAULT 0, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)" + suffix,
                "CREATE TABLE IF NOT EXISTS suggestion (id " + id + ", player VARCHAR(30) NOT NULL, suggestion TEXT NOT NULL, response TEXT, respondedBy VARCHAR(30), respondedAt DATETIME, send BOOLEAN DEFAULT FALSE, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)" + suffix
        };

        try (Connection conn = getConnection(); Statement statement = conn.createStatement()) {
            for (String table : tables) statement.executeUpdate(table);
            logger.info("База данных успешно инициализирована");
        }
        catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при создании таблиц", e);
        }
    }
}
