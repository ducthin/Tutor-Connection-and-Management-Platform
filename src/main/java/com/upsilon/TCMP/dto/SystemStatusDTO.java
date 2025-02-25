package com.upsilon.TCMP.dto;

public class SystemStatusDTO {
    private int diskUsagePercent;
    private int memoryUsagePercent;
    private boolean databaseConnected;
    private boolean emailServiceWorking;
    private boolean paymentGatewayWorking;
    private boolean fileStorageWorking;

    public int getDiskUsagePercent() {
        return diskUsagePercent;
    }

    public void setDiskUsagePercent(int diskUsagePercent) {
        this.diskUsagePercent = diskUsagePercent;
    }

    public int getMemoryUsagePercent() {
        return memoryUsagePercent;
    }

    public void setMemoryUsagePercent(int memoryUsagePercent) {
        this.memoryUsagePercent = memoryUsagePercent;
    }

    public boolean isDatabaseConnected() {
        return databaseConnected;
    }

    public void setDatabaseConnected(boolean databaseConnected) {
        this.databaseConnected = databaseConnected;
    }

    public boolean isEmailServiceWorking() {
        return emailServiceWorking;
    }

    public void setEmailServiceWorking(boolean emailServiceWorking) {
        this.emailServiceWorking = emailServiceWorking;
    }

    public boolean isPaymentGatewayWorking() {
        return paymentGatewayWorking;
    }

    public void setPaymentGatewayWorking(boolean paymentGatewayWorking) {
        this.paymentGatewayWorking = paymentGatewayWorking;
    }

    public boolean isFileStorageWorking() {
        return fileStorageWorking;
    }

    public void setFileStorageWorking(boolean fileStorageWorking) {
        this.fileStorageWorking = fileStorageWorking;
    }
}