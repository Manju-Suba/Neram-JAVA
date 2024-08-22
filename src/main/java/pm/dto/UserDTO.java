package pm.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pm.model.product.EProductApproStatus;
import pm.model.users.Roles;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    public UserDTO(int id2, String name2, String roleNames, String profile_pic, String branch) {
        this.id = id2;
        this.name = name2;
        this.role = roleNames;
        this.profile_pic = profile_pic;
        this.branch = branch;
    }

    public UserDTO(int id, String name, String role, String email, String profile_pic, String branch) {
        super();
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.profile_pic = profile_pic;
        this.branch = branch;
    }

    public UserDTO(int id, String name) {
        super();
        this.id = id;
        this.name = name;
    }
    private int id;
    private String name;
    private String userName;
    private String role;
    private String email;
    private String rolesid;
    private String profile_pic;
    private LocalDateTime updated_At;
    private EProductApproStatus approStatus;
    private String remarks;
    private String employeeId;
    private String branch;
    private String designation;
    private String supervisor;
    private String supervisorId;
    private String roleType;
    private int approvalFinal;
    private String approvalFinalId;
    private String approvalFinalName;
    private boolean userStatus;
    private LocalDate doj;

}
