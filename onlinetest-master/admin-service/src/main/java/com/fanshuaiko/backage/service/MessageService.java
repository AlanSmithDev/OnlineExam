package com.fanshuaiko.backage.service;

import com.fanshuaiko.backage.entity.ImMessage;

import java.util.List;

public interface MessageService {
    void save(ImMessage message);

    List<ImMessage> latest(int limit);
}
