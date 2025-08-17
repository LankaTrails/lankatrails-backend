package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.model.Transport;

import java.util.List;
import java.util.Set;

public interface Tabs {
    void addTabs(List<TabSectionRequest> tabsReq, Service lastServiceAdded);
    List<TabSectionRequest> getAllTabs(Long Id);
    Set<TabsSection> updateTabs(Set<TabsSection> tabs, List<TabSectionRequest> reqTabs, Transport transport);
    void updateTabs(List<TabSectionRequest> tabsReq, Service lastServiceAdded);
    Boolean addTabsToTransport(List<TabSectionRequest> tabsReq,Transport transport);
    void deleteTabs(List<TabSectionRequest> tabsReq);
}
