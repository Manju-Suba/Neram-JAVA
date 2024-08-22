package pm.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlowDTO {
    public FlowDTO(int id2, String name2) {
    }

    public FlowDTO() {
    }

    private int id;
    private String name;
    private List<UserDTO> approvals;
    private List<UserDTO> access;
}
