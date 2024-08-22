package pm.model.Budgie;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name = "swipeinout")
@IdClass(CompositeKeySwipe.class)
public class BudgieApi {
	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private int id;
	@Column(name = "empid")
	private String employeeID;
	@Column(name = "logdate")
	private Date date;
	@Column(name = "intime")
	private String signIn;
	@Column(name = "outtime")
	private String signOut;

}
