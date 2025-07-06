package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.model.Transport;

import java.util.List;
import java.util.Set;

public interface Tabs {
    Boolean addTabs(List<TabSectionRequest> tabsReq, ActivityService lastServiceAdded);
    List<TabSectionRequest> getAllTabs(Long Id);
    Set<TabsSection> updateTabs(Set<TabsSection> tabs, List<TabSectionRequest> reqTabs, Transport transport);
    Boolean addTabsToTransport(List<TabSectionRequest> tabsReq,Transport transport);
}
