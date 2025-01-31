package com.timeSeriesApplication.timeSeriesApplication.Entity;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;

import java.time.Instant;

@Measurement(name = "machine_status")
@Data
public class MachineStatus {

    @Column(tag = true)
    private String machineName;

    @Column
    private Double cpuUsage;

    @Column
    private Double memoryUsage;

    @Column(timestamp = true)
    private Instant timestamp;


}
