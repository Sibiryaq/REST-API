package ru.aston.repository.impl;

import ru.aston.repository.ConnectionBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionBuilderImpl implements ConnectionBuilder {

    private String config = "default";
    private String jdbcURL;
    private String jdbcUsername;
    private String jdbcPassword;
    private String jdbcDriver;

    public ConnectionBuilderImpl(String jdbcURL, String jdbcUsername, String jdbcPassword, String jdbcDriver) {
        this.jdbcURL = jdbcURL;
        this.jdbcUsername = jdbcUsername;
        this.jdbcPassword = jdbcPassword;
        this.jdbcDriver = jdbcDriver;
    }

    public ConnectionBuilderImpl() {
    }

    private static final String DB_CONFIG = "/resources/database.properties";

    @Override
    public Connection getConnection() throws SQLException, ClassNotFoundException {
        if (jdbcURL == null) {
            this.init();
        }
        Class.forName(jdbcDriver);
        return DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
    }

    private void init() throws SQLException {
        Properties properties = this.loadProperties(DB_CONFIG);
        this.jdbcURL = properties.getProperty(config + ".jdbc.url");
        this.jdbcDriver = properties.getProperty(config + ".jdbc.driver");
        this.jdbcUsername = properties.getProperty(config + ".jdbc.user");
        this.jdbcPassword = properties.getProperty(config + ".jdbc.pass");
    }

    private Properties loadProperties(String configPath) throws SQLException {
        Properties dbProps = new Properties();
        try {
            File file = new File(configPath);
            if (file.exists()) {
                try (FileInputStream inputStream = new FileInputStream(configPath)) {
                    dbProps.load(inputStream);
                }
            } else {
                try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("/database.properties")) {
                    dbProps.load(inputStream);
                }
            }
        } catch (Exception e) {
            throw new SQLException(e);
        }
        return dbProps;
    }
}
