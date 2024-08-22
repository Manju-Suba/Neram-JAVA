package pm.model.flow;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "poduct_flows")
public class Flow {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String name;
	private List<Integer> access_to;
	private List<Integer> approval_by;
	private int created_by;
	private Set<EStatus> status;
	private LocalDateTime created_at;
	private LocalDateTime updated_at;
	private boolean is_deleted;
//	private String access;
    public void setStatus(EStatus pending) {
    }
    

}
