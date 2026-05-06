package com.ctf.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestionnaire de connexion à la base de données via HikariCP.
 * Remplace le Singleton simple qui causait "This connection has been closed"
 * avec Supabase (connexions inactives fermées côté serveur).
 */
public class DBConnection {

    private static HikariDataSource dataSource = null;

    // Constructeur privé — classe utilitaire
    private DBConnection() {}

    // Initialisation du pool au premier appel
    static {
        try {
            // 1. Charger db.properties
            Properties props = new Properties();
            InputStream is = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("db.properties");

            if (is == null) {
                throw new RuntimeException("Le fichier db.properties est introuvable !");
            }
            props.load(is);

            // 2. Configurer HikariCP
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setDriverClassName(props.getProperty("db.driver"));

            // Pool size
            config.setMaximumPoolSize(
                Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
            config.setMinimumIdle(
                Integer.parseInt(props.getProperty("db.pool.minimumIdle", "2")));

            // Timeouts
            config.setConnectionTimeout(
                Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
            config.setIdleTimeout(
                Long.parseLong(props.getProperty("db.pool.idleTimeout", "600000")));
            config.setMaxLifetime(
                Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));

            // ✅ Fix Supabase : garder les connexions vivantes
            config.setKeepaliveTime(60000);          // ping toutes les 60s
            config.setConnectionTestQuery("SELECT 1"); // tester avant utilisation

            config.setPoolName("CTF-HikariPool");

            // 3. Créer le pool
            dataSource = new HikariDataSource(config);

            System.out.println("✅ HikariCP pool initialisé avec succès !");

        } catch (Exception e) {
            System.err.println("❌ Erreur fatale d'initialisation du pool HikariCP !");
            e.printStackTrace();
            throw new RuntimeException("Impossible d'initialiser le pool de connexions", e);
        }
    }

    /**
     * Retourne une connexion depuis le pool.
     * HikariCP gère automatiquement la reconnexion si la connexion est fermée.
     * Utiliser dans un try-with-resources pour la refermer automatiquement.
     *
     * Exemple :
     *   try (Connection conn = DBConnection.getConnection()) {
     *       ...
     *   }
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
