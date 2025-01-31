package com.timeSeriesApplication.timeSeriesApplication.Repository;


import com.influxdb.client.WriteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.timeSeriesApplication.timeSeriesApplication.Entity.MachineStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MachineStatusRepository {

    @Autowired
    private InfluxDBClient influxDBClient;

    public void saveMachineStatus(MachineStatus machineStatus) {
        System.out.println("----------------------Saved");
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writeMeasurement(WritePrecision.MS, machineStatus);
    }
}
