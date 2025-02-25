package com.upsilon.TCMP.service;

import com.upsilon.TCMP.dto.ActivityDTO;
import com.upsilon.TCMP.dto.SystemLogDTO;
import com.upsilon.TCMP.dto.SystemStatusDTO;

import java.util.List;

public interface SystemMonitorService {
    SystemStatusDTO getCurrentStatus();
    
    List<ActivityDTO> getRecentActivity(int limit);
    
    List<ActivityDTO> getAllActivity();
    
    List<SystemLogDTO> getSystemLogs();
    
    void logActivity(String type, String description, String username);
    
    void logSystemError(String message, String source, String stackTrace);
}