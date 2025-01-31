package com.timeSeriesApplication.timeSeriesApplication.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.influxdb.InfluxDBTemplate;

@Configuration
public class InfluxDBConfig {

    @Value("${spring.influx.url}")
    private String influxUrl;

    @Value("${spring.influx.token}")
    private String influxToken;

    @Value("${spring.influx.org}")
    private String influxOrg;

    @Value("${spring.influx.bucket}")
    private String influxBucket;

    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(influxUrl, influxToken.toCharArray(), influxOrg, influxBucket);
    }


}
