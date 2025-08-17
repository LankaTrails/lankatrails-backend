package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.model.Transport;
import com.lankatrails.lankatrails_backend.repositories.TabsSectionRepository;
import com.lankatrails.lankatrails_backend.service.Tabs;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Slf4j
public class TabsImpl implements Tabs {
    @Autowired
    TabsSectionRepository tabsSectionRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public void addTabs(List<TabSectionRequest> tabsReq, Service lastServiceAdded){
        if (!tabsReq.isEmpty()){
            for (TabSectionRequest tab : tabsReq){
                if (StringUtils.hasText(tab.getHeading()) && StringUtils.hasText(tab.getContent())){
                    System.out.println("testingTabOutput"+tab.getContent()+"  "+tab.getContent());
                    TabsSection tabsSection=new TabsSection();
                    tabsSection.setHeading(tab.getHeading());
                    tabsSection.setContent(tab.getContent());
                    tabsSection.setService(lastServiceAdded);
                    tabsSectionRepository.save(tabsSection);
                }

            }

        }
    }

    @Override
    public List<TabSectionRequest> getAllTabs(Long Id) {

        List<TabsSection> tabsSection=tabsSectionRepository.findByService_ServiceId(Id);
        List<TabSectionRequest> tabs=new ArrayList<>();

        for (TabsSection tab :tabsSection){
            TabSectionRequest tabReq = new TabSectionRequest();
            tabReq.setId(tab.getId());
            tabReq.setHeading(tab.getHeading());
            tabReq.setContent(tab.getContent());
            tabs.add(tabReq);
        }
        return tabs;
    }

    @Override
    public void updateTabs(List<TabSectionRequest> tabsReq, Service lastServiceAdded){
        if (!tabsReq.isEmpty()){
            for (TabSectionRequest tab : tabsReq){
                if (tab.getId() != null) {
                    // If the tab has an ID, update the existing tab
                    TabsSection existingTab = tabsSectionRepository.findById(tab.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Tab not found with ID: " + tab.getId()));
                    existingTab.setHeading(tab.getHeading());
                    existingTab.setContent(tab.getContent());
                    tabsSectionRepository.save(existingTab);
                } else {
                    // If no ID, create a new tab
                    TabsSection newTab = new TabsSection();
                    newTab.setHeading(tab.getHeading());
                    newTab.setContent(tab.getContent());
                    newTab.setService(lastServiceAdded);
                    tabsSectionRepository.save(newTab);
                }
            }

        }
    }

    @Override
    public Set<TabsSection> updateTabs(Set<TabsSection> tabs, List<TabSectionRequest> reqTabs, Transport transport) {
        //create a map of existing tabs by ID for quick lookup
        Map<Long,TabsSection> savedTabMap=tabs.stream()
                .collect(Collectors.toMap(TabsSection::getId, Function.identity()));

        //create a set to track updated or newly added tabs
        Set<TabsSection> updatedTabs=new HashSet<>();

        for (TabSectionRequest req:reqTabs){
            TabsSection tab;
            if (req.getId()!=null && savedTabMap.containsKey(req.getId())){
                //update the existing tab
                tab=savedTabMap.get(req.getId());
                tab.setHeading(req.getHeading());
                tab.setContent(req.getContent());
            }else{
                //create new tab
                tab=new TabsSection();
                tab.setHeading(req.getHeading());
                tab.setContent(req.getContent());
                tab.setService(transport);
            }
            updatedTabs.add(tab);
        }

        return updatedTabs;
    }



    @Override
    public Boolean addTabsToTransport(List<TabSectionRequest> tabsReq, Transport lastTransportAdded) {
        if (tabsReq!=null){
            for (TabSectionRequest tab : tabsReq){
                TabsSection tabsSection=new TabsSection();
                tabsSection.setHeading(tab.getHeading());
                tabsSection.setContent(tab.getContent());
                tabsSection.setService(lastTransportAdded);
                tabsSectionRepository.save(tabsSection);
            }
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void deleteTabs(List<TabSectionRequest> tabsReq) {
        log.debug("Deleting tabs: {}", tabsReq);
        if (tabsReq != null && !tabsReq.isEmpty()) {
            for (TabSectionRequest tab : tabsReq) {
                if (tab.getId() != null) {
                    log.debug("Deleting tab with ID: {}", tab.getId());
                    tabsSectionRepository.deleteById(tab.getId());
                }
            }
        }
    }

}
