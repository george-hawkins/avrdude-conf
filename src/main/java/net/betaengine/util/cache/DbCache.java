package net.betaengine.util.cache;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbCache implements Cache {
    private final static String INSERT = "INSERT INTO cache (uuid, cachekey, cachevalue) VALUES(?,?, ?)";
    private final static String SELECT = "SELECT cachevalue FROM cache WHERE uuid = ? AND cachekey = ?";
    
    private String uuid;
    
    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String get(String key, ValueCreator creator) {
        try (Connection connection = getConnection()) {
            String value = select(connection, uuid, key);
            
            if (value == null) {
                value = creator.create();
                insert(connection, uuid, key, value);
            }
            
            return value;
        } catch (SQLException e) {
            throw new CacheException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            URI dbUri = new URI(System.getenv("DATABASE_URL"));
    
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();
            
            // If accessing a Heroku DB remotely the URI should end in
            // "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory".
            if (dbUri.getQuery() != null) {
                dbUrl += "?" + dbUri.getQuery();
            }
    
            return DriverManager.getConnection(dbUrl, username, password);
        } catch (URISyntaxException e) {
            throw new CacheException(e);
        }
    }
    
    public void insert(Connection connection, String uuid, String key, String value)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
            // Note: initially I tried using a CLOB for "cachevalue" but the PostgreSQL JDBC driver doesn't
            // support creating them, you can query and modify them, so there are workarounds - e.g.:
            // http://www.xinotes.net/notes/note/943/
            // However treating the "cachevalue" as a string works for non-huge values.
            
            statement.setString(1, uuid);
            statement.setString(2, key);
            statement.setString(3, value);
            statement.executeUpdate();
        }
    }
    
    public String select(Connection connection, String uuid, String key)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT)) {
            statement.setString(1, uuid);
            statement.setString(2, key);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getString("cachevalue");
            } else {
                return null;
            }
        }
    }
}