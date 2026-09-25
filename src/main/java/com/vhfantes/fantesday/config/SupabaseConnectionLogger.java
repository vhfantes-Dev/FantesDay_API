package com.vhfantes.fantesday.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component
public class SupabaseConnectionLogger implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseConnectionLogger.class);

    private final DataSource dataSource;

    public SupabaseConnectionLogger(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            logger.info("✅ Conexão com o Supabase estabelecida com sucesso!");
            logger.info("URL: {}", metaData.getURL());
            logger.info("Usuário: {}", metaData.getUserName());
            logger.info("Banco de dados: {} {}", metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
        } catch (Exception e) {
            logger.error("❌ Falha ao conectar com o Supabase: {}", e.getMessage(), e);
        }
    }
}