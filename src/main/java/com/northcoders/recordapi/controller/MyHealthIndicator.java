package com.northcoders.recordapi.controller;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.MemoryMXBean;

@Component
public class MyHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // Perform checks for various resources
        boolean diskSpaceHealthy = checkDiskSpace("/");
        double cpuLoad = getCpuLoad();
        double memoryUsage = getMemoryUsage();

        // Assess health status
        if (!diskSpaceHealthy || cpuLoad > 90.0 || memoryUsage > 90.0) {
            return Health.down()
                    .withDetail("DiskSpace", diskSpaceHealthy ? "Healthy" : "Low disk space")
                    .withDetail("CpuLoad", cpuLoad + "%")
                    .withDetail("MemoryUsage", memoryUsage + "%")
                    .withDetail("Error", "One or more resources are under pressure")
                    .build();
        }

        return Health.up()
                .withDetail("DiskSpace", "Healthy")
                .withDetail("CpuLoad", cpuLoad + "%")
                .withDetail("MemoryUsage", memoryUsage + "%")
                .build();
    }

    private boolean checkDiskSpace(String path) {
        // Check available disk space on the specified path
        File drive = new File(path);
        long freeSpace = drive.getFreeSpace(); // in bytes
        long totalSpace = drive.getTotalSpace();
        long threshold = 5L * 1024 * 1024 * 1024; // 5 GB threshold

        System.out.println("Disk Space: " + freeSpace + " bytes available out of " + totalSpace + " bytes");
        return freeSpace > threshold;
    }

    private double getCpuLoad() {
        // Get the system CPU load as a percentage
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            double systemLoad = ((com.sun.management.OperatingSystemMXBean) osBean).getSystemCpuLoad() * 100;
            System.out.println("CPU Load: " + systemLoad + "%");
            return systemLoad;
        }
        return -1; // Unknown CPU load
    }

    private double getMemoryUsage() {
        // Get memory usage as a percentage
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryMXBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryMXBean.getHeapMemoryUsage().getMax();

        double memoryUsagePercentage = ((double) usedMemory / maxMemory) * 100;
        System.out.println("Memory Usage: " + memoryUsagePercentage + "%");
        return memoryUsagePercentage;
    }
}
