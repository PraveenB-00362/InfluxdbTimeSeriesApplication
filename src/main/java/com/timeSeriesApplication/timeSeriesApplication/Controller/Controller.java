package com.timeSeriesApplication.timeSeriesApplication.Controller;

import com.influxdb.query.FluxTable;
import com.timeSeriesApplication.timeSeriesApplication.Scheduler.MachineStatusScheduler;
import com.timeSeriesApplication.timeSeriesApplication.Service.MachineStatusQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class Controller {
    @Autowired
    private MachineStatusScheduler machineStatusScheduler;
    @Autowired
    private MachineStatusQueryService machineStatusQueryService;

    @PostMapping ("/save")
    public String  logMachineStatus() {
        machineStatusScheduler.logMachineStatus();
        return "success";
    }

    @GetMapping("/get")
    public List<FluxTable> getAllData(){
        return machineStatusQueryService.queryMachineStatus();
    }

        @GetMapping("/avg-cpu-usage")
    public double getAverageCpuUsage(@RequestParam String machineName) {
        return machineStatusQueryService.getAverageCpuUsage(machineName);
    }

    @GetMapping("/avg-memory-usage")
    public double getAverageMemoryUsage(@RequestParam String machineName) {
        return machineStatusQueryService.getAverageMemoryUsage(machineName);
    }

    @GetMapping("/max-cpu-usage")
    public double getMaxCpuUsage(@RequestParam String machineName) {
        return machineStatusQueryService.getMaxCpuUsage(machineName);
    }

    @GetMapping("/max-memory-usage")
    public double getMaxMemoryUsage(@RequestParam String machineName) {
        return machineStatusQueryService.getMaxMemoryUsage(machineName);
    }

    @GetMapping("/cpu-usage-at-time")
    public double getCpuUsageAtTime(@RequestParam String machineName, @RequestParam String time) {
        Instant instant = Instant.parse(time);
        return machineStatusQueryService.getCpuUsageAtTime(machineName, instant);
    }

    @GetMapping("/memory-usage-at-time")
    public double getMemoryUsageAtTime(@RequestParam String machineName, @RequestParam String time) {
        Instant instant = Instant.parse(time);
        return machineStatusQueryService.getMemoryUsageAtTime(machineName, instant);
    }


    @GetMapping("/usage-with-time-range")
    public Map<String, List<Map<String, Object>>> getUsageWithTimeRange(
            @RequestParam String machineName,
            @RequestParam String timeRange,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return machineStatusQueryService.getUsageWithTimeRange(machineName, timeRange, startDate, endDate);
    }


    @GetMapping("/avg-usage")
    public Map<String, Double> getAverageUsage(
            @RequestParam String machineName,
            @RequestParam String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam Set<String> fields) {
        return machineStatusQueryService.getAverageUsage(machineName, startDate, endDate, fields);
    }

    @GetMapping("/max-usage")
    public Map<String, Double> getmaxUsage(
            @RequestParam String machineName,
            @RequestParam String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam Set<String> fields) {
        return machineStatusQueryService.getMaxUsage(machineName, startDate, endDate, fields);
    }


}
