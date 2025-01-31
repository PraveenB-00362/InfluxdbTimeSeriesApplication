package com.timeSeriesApplication.timeSeriesApplication.Service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.timeSeriesApplication.timeSeriesApplication.Entity.MachineStatus;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.influxdb.InfluxDBTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MachineStatusQueryService {

    @Autowired
    private InfluxDBClient influxDBClient;

    public List<FluxTable> queryMachineStatus() {
        String fluxQuery = "from(bucket: \"mybucket\") |> range(start: -1h) |> filter(fn: (r) => r._measurement == \"machine_status\")";

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery);

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                System.out.println(record.getValueByKey ("_field")+ ": " + record.getValueByKey("_value") + ":  "+record.getTime() );
            }
        }
        return tables;
    }



    public double getAverageCpuUsage(String machineName) {
        String query = String.format("from(bucket: \"%s\") " +
                "|> range(start: 0) " +
                "|> filter(fn: (r) => r._measurement == \"machine_status\" and r.machineName == \"%s\" and r._field == \"cpuUsage\") " +
                "|> mean()", "mybucket", machineName);
        return executeSingleValueQuery(query);
    }

    public double getAverageMemoryUsage(String machineName) {
        String query = String.format("from(bucket: \"%s\") " +
                "|> range(start: 0) " +
                "|> filter(fn: (r) => r._measurement == \"machine_status\" and r.machineName == \"%s\" and r._field == \"memoryUsage\") " +
                "|> mean()", "mybucket", machineName);
        return executeSingleValueQuery(query);
    }

    public double getMaxCpuUsage(String machineName) {
        String query = String.format("from(bucket: \"%s\") " +
                "|> range(start: 0) " +
                "|> filter(fn: (r) => r._measurement == \"machine_status\" and r.machineName == \"%s\" and r._field == \"cpuUsage\") " +
                "|> max()", "mybucket", machineName);
        return executeSingleValueQuery(query);
    }

    public double getMaxMemoryUsage(String machineName) {
        String query = String.format("from(bucket: \"%s\") " +
                "|> range(start: 0) " +
                "|> filter(fn: (r) => r._measurement == \"machine_status\" and r.machineName == \"%s\" and r._field == \"memoryUsage\") " +
                "|> max()", "mybucket", machineName);
        return executeSingleValueQuery(query);
    }

    public double getCpuUsageAtTime(String machineName, Instant time) {
        String query = String.format("from(bucket: \"%s\") " +
                "|> range(start: %s, stop: %s) " +
                "|> filter(fn: (r) => r._measurement == \"machine_status\" and r.machineName == \"%s\" and r._field == \"cpuUsage\") " +
                "|> last()", "mybucket", time.minusSeconds(1).toString(), time.plusSeconds(1).toString(), machineName);
        return executeSingleValueQuery(query);
    }

    public double getMemoryUsageAtTime(String machineName, Instant time) {
        String query = String.format("from(bucket: \"%s\") " +
                "|> range(start: time(v: %s), stop: time(v: %s))" +
                "|> filter(fn: (r) => r._measurement == \"machine_status\" and r.machineName == \"%s\" and r._field == \"memoryUsage\") " +
                "|> last()", "mybucket", time.toString(), "2025-01-29T15:01:00Z", machineName);
        return executeSingleValueQuery(query);
    }



    private double executeSingleValueQuery(String query) {
        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(query, "myorg");
        if (!tables.isEmpty() && !tables.get(0).getRecords().isEmpty()) {
            return (double) tables.get(0).getRecords().get(0).getValue();
        }
        return 0.0;
    }

    public Map<String, List<Map<String, Object>>> getUsageWithTimeRange(
            String machineName, String timeRange, String startDate, String endDate) {

        Instant startInstant;
        Instant endInstant = Instant.now();

        if ("custom".equalsIgnoreCase(timeRange)) {
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("For custom time range, startDate and endDate must be provided.");
            }
            startInstant = Instant.parse(startDate);
            endInstant = Instant.parse(endDate);
        } else {
            // Parse the timeRange value (e.g., "1h", "24h", "30d", "50h", etc.)
            Pattern pattern = Pattern.compile("^(\\d+)([hHdD])$");
            Matcher matcher = pattern.matcher(timeRange);

            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid timeRange format. Expected format: <number><h|d> (e.g., 1h, 24h, 30d).");
            }

            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();

            switch (unit) {
                case "h":
                    startInstant = endInstant.minus(value, ChronoUnit.HOURS);
                    break;
                case "d":
                    startInstant = endInstant.minus(value, ChronoUnit.DAYS);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid unit in timeRange. Supported units: h (hours), d (days).");
            }
        }

        return getUsageBetweenDates(machineName, startInstant, endInstant);
    }


    public Map<String, List<Map<String, Object>>> getUsageBetweenDates(String machineName, Instant startDate, Instant endDate) {
        String query = String.format("from(bucket: \"%s\") " +
                "|> range(start: %s, stop: %s) " +
                "|> filter(fn: (r) => r._measurement == \"machine_status\" and r.machineName == \"%s\") " +
                "|> filter(fn: (r) => r._field == \"cpuUsage\" or r._field == \"memoryUsage\")", "mybucket", startDate.toString(), endDate.toString(), machineName);

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(query, "myorg");

        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        List<Map<String, Object>> cpuUsageList = new ArrayList<>();
        List<Map<String, Object>> memoryUsageList = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("time", record.getTime());
                dataPoint.put("value", record.getValue());

                if ("cpuUsage".equals(record.getField())) {
                    cpuUsageList.add(dataPoint);
                } else if ("memoryUsage".equals(record.getField())) {
                    memoryUsageList.add(dataPoint);
                }
            }
        }

        result.put("cpuUsage", cpuUsageList);
        result.put("memoryUsage", memoryUsageList);

        return result;
    }

    public Map<String, Double> getAverageUsage(
            String machineName, String startDate, String endDate, Set<String> fields) {

        Instant startInstant;
        Instant endInstant;

        if (endDate == null) {
            LocalDate localDate = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
            startInstant = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            endInstant = localDate.atTime(23, 59, 59, 999_999_999).atZone(ZoneOffset.UTC).toInstant();
        } else {
            startInstant = Instant.parse(startDate);
            endInstant = Instant.parse(endDate);
        }

        Map<String, Double> averages = new HashMap<>();

        for (String field : fields) {
            String query = String.format("from(bucket: \"%s\") " +
                    "|> range(start: %s, stop: %s) " +
                    "|> filter(fn: (r) => r._measurement == \"machine_status\" and r.machineName == \"%s\" and r._field == \"%s\") " +
                    "|> mean()", "mybucket", startInstant.toString(), endInstant.toString(), machineName, field);

            double average = executeSingleValueQuery(query);
            averages.put(field, average);
        }

        return averages;
    }

    //
    public Map<String, Double> getMaxUsage(
            String machineName, String startDate, String endDate, Set<String> fields) {

        Instant startInstant;
        Instant endInstant;

        if (endDate == null) {
            LocalDate localDate = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
            startInstant = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            endInstant = localDate.atTime(23, 59, 59, 999_999_999).atZone(ZoneOffset.UTC).toInstant();
        } else {
            startInstant = Instant.parse(startDate);
            endInstant = Instant.parse(endDate);
        }

        Map<String, Double> max = new HashMap<>();

        for (String field : fields) {
            String query = String.format("from(bucket: \"%s\") " +
                    "|> range(start: %s, stop: %s) " +
                    "|> filter(fn: (r) => r._measurement == \"machine_status\" and r.machineName == \"%s\" and r._field == \"%s\") " +
                    "|> max()", "mybucket", startInstant.toString(), endInstant.toString(), machineName, field);

            double maxi = executeSingleValueQuery(query);
            max.put(field, maxi);
        }

        return max;
    }
}

