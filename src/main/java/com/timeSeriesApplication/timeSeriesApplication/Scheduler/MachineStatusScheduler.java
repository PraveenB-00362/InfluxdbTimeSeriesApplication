package com.timeSeriesApplication.timeSeriesApplication.Scheduler;


import com.timeSeriesApplication.timeSeriesApplication.Entity.MachineStatus;
import com.timeSeriesApplication.timeSeriesApplication.Repository.MachineStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MachineStatusScheduler {

    @Autowired
    private MachineStatusRepository machineStatusRepository;

    @Scheduled(fixedRate = 60000)
    public void logMachineStatus() {

        MachineStatus machineStatus = new MachineStatus();
        machineStatus.setMachineName("Machine-1");
        machineStatus.setCpuUsage(getCpuUsage());
        machineStatus.setMemoryUsage(getMemoryUsage());
        machineStatus.setTimestamp(Instant.now());


        machineStatusRepository.saveMachineStatus(machineStatus);
    }

    private Double getCpuUsage() {

        return Math.random() * 100;
    }

    private Double getMemoryUsage() {

        return Math.random() * 100;
    }
}
