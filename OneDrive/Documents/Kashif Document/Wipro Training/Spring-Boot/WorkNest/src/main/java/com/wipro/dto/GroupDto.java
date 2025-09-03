package com.wipro.dto;

import com.wipro.entity.Group;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupDto {
    private Integer id;
    private String name;
    private Set<UserDto> members;

    public GroupDto(Group group) {
        this.id = group.getId();
        this.name = group.getName();
        this.members = group.getMembers().stream()
                .map(user -> new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole()))
                .collect(Collectors.toSet());
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<UserDto> getMembers() { return members; }
    public void setMembers(Set<UserDto> members) { this.members = members; }
}
