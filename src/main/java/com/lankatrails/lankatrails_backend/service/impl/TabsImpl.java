package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.repositories.TabsSectionRepository;
import com.lankatrails.lankatrails_backend.service.Tabs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TabsImpl implements Tabs {
    @Autowired
    TabsSectionRepository tabsSectionRepository;

    @Override
    public Boolean addTabs(List<TabSectionRequest> tabsReq, ActivityService lastServiceAdded){
        if (tabsReq!=null){
            for (TabSectionRequest tab : tabsReq){
                TabsSection tabsSection=new TabsSection();
                tabsSection.setHeading(tab.getHeading());
                tabsSection.setContent(tab.getContent());
                tabsSection.setService(lastServiceAdded);
                tabsSectionRepository.save(tabsSection);
            }
            return true;
        }else {
            return false;
        }

    }
}
