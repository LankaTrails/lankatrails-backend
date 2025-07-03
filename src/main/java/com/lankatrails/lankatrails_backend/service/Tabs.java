package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.model.ActivityService;

import java.util.List;

public interface Tabs {
    Boolean addTabs(List<TabSectionRequest> tabsReq, ActivityService lastServiceAdded);
}
