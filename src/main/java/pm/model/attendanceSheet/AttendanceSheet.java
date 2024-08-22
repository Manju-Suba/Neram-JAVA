package pm.model.attendanceSheet;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "attendance_table")

public class AttendanceSheet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "user_id")
    private int userid;
    private String status;
    private LocalDate appliedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AttendanceSheet(int userid, String status, LocalDate appliedDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userid = userid;
        this.status = status;
        this.appliedDate = appliedDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
