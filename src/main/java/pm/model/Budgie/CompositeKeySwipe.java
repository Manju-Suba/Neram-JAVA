package pm.model.Budgie;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompositeKeySwipe implements Serializable {
private String employeeID;
    private Date date;
	private String signIn;
	private String signOut;


 

    // Getters, setters, equals, and hashCode methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositeKeySwipe that = (CompositeKeySwipe) o;
        return Objects.equals(employeeID, that.employeeID) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeID, date);
    }
}
