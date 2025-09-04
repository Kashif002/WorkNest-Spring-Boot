package com.wipro.service;

import com.wipro.dto.GroupDto;
import com.wipro.entity.Group;
import com.wipro.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    public List<GroupDto> findAll() {
        return groupRepository.findAll()
                .stream()
                .map(GroupDto::new)
                .collect(Collectors.toList());
    }
    
    public List<Group> findAllGroups() {
        return groupRepository.findAll();
    }
    
    public Group findById(Integer id) {
        return groupRepository.findById(id).orElse(null);
    }
    
    public Group findGroupById(Integer id) {
        return groupRepository.findById(id).orElse(null);
    }

    public Group save(Group group) {
        return groupRepository.save(group);
    }

    public void deleteById(Integer id) {
        groupRepository.deleteById(id);
    }
}