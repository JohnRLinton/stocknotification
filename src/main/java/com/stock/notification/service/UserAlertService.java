package com.stock.notification.service;

public interface UserAlertService {
    void addAlert(int userId, String stockCode, int alertType, String alertContent, int alertFrequency);
}

