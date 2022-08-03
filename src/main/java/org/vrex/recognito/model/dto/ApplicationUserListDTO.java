package org.vrex.recognito.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.vrex.recognito.entity.User;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApplicationUserListDTO implements Serializable {

    private String app;
    private List<UserDTO> users;
    private int size;

    public ApplicationUserListDTO(String appIdentifier, List<User> users) {
        this.app = appIdentifier;
        this.users = ObjectUtils.isEmpty(users) ?
                new LinkedList<>() :
                users.stream().map(user -> new UserDTO(user)).collect(Collectors.toList());
        this.size = this.users.size();
    }
}
