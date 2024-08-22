package pm.model.task;

import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name="timelimit")
public class TimeLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String status;
	private LocalDateTime createdat;
	private LocalDateTime updatedat;
	private Boolean isdeletd;
	private LocalTime hour;
	
}
