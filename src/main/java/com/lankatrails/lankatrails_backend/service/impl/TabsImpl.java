package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.model.Transport;
import com.lankatrails.lankatrails_backend.repositories.TabsSectionRepository;
import com.lankatrails.lankatrails_backend.service.Tabs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TabsImpl implements Tabs {
    @Autowired
    TabsSectionRepository tabsSectionRepository;

    @Override
    public void addTabs(List<TabSectionRequest> tabsReq, ActivityService lastServiceAdded){
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

}
