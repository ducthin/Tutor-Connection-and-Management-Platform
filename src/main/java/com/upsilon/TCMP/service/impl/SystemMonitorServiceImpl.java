package com.upsilon.TCMP.service.impl;

import com.upsilon.TCMP.dto.ActivityDTO;
import com.upsilon.TCMP.dto.SystemLogDTO;
import com.upsilon.TCMP.dto.SystemStatusDTO;
import com.upsilon.TCMP.service.SystemMonitorService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SystemMonitorServiceImpl implements SystemMonitorService {

    private List<ActivityDTO> activityLog = new ArrayList<>();
    private List<SystemLogDTO> systemLog = new ArrayList<>();

    @Override
    public SystemStatusDTO getCurrentStatus() {
        SystemStatusDTO status = new SystemStatusDTO();
        
        // Get disk usage
        File root = new File("/");
        long totalSpace = root.getTotalSpace();
        long usableSpace = root.getUsableSpace();
        int diskUsage = (int) ((totalSpace - usableSpace) * 100 / totalSpace);
        status.setDiskUsagePercent(diskUsage);

        // Get memory usage
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedHeap = memoryBean.getHeapMemoryUsage().getUsed();
        long maxHeap = memoryBean.getHeapMemoryUsage().getMax();
        int memoryUsage = (int) (usedHeap * 100 / maxHeap);
        status.setMemoryUsagePercent(memoryUsage);

        // Set service statuses (in production, these would check actual services)
        status.setDatabaseConnected(true);
        status.setEmailServiceWorking(true);
        status.setPaymentGatewayWorking(true);
        status.setFileStorageWorking(true);

        return status;
    }

    @Override
    public List<ActivityDTO> getRecentActivity(int limit) {
        if (activityLog.size() <= limit) {
            return new ArrayList<>(activityLog);
        }
        return activityLog.subList(0, limit);
    }

    @Override
    public List<ActivityDTO> getAllActivity() {
        return new ArrayList<>(activityLog);
    }

    @Override
    public List<SystemLogDTO> getSystemLogs() {
        return new ArrayList<>(systemLog);
    }

    @Override
    public void logActivity(String type, String description, String username) {
        ActivityDTO activity = new ActivityDTO();
        activity.setType(type);
        activity.setDescription(description);
        activity.setUser(username);
        activity.setTime(LocalDateTime.now());
        
        activityLog.add(0, activity); // Add to beginning of list
        
        // Keep log size manageable
        if (activityLog.size() > 1000) {
            activityLog = activityLog.subList(0, 1000);
        }
    }

    @Override
    public void logSystemError(String message, String source, String stackTrace) {
        SystemLogDTO log = new SystemLogDTO();
        log.setLevel("ERROR");
        log.setMessage(message);
        log.setSource(source);
        log.setStackTrace(stackTrace);
        log.setTimestamp(LocalDateTime.now());
        
        systemLog.add(0, log); // Add to beginning of list
        
        // Keep log size manageable
        if (systemLog.size() > 1000) {
            systemLog = systemLog.subList(0, 1000);
        }
    }
}