package com.mykhaliev.metrics.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class MetricServerConfig {

    private static final String HOSTNAME = "METRIC_CLIENT_LOCALHOST";

//    @Bean
//    public DataSource mysqlDataSource() {
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
//        dataSource.setUrl("jdbc:mysql://localhost:3306/springjdbc");
//        dataSource.setUsername("guest_user");
//        dataSource.setPassword("guest_password");
//        return dataSource;
//    }

}
