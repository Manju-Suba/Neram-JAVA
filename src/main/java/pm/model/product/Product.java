package pm.model.product;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Cascade;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pm.model.flow.Flow;
import pm.model.users.Users;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "flow")
  private Flow flow;

  @ManyToOne
  @JoinColumn(name = "prod_head")
  private Users prodHead;

  @ManyToOne
  @JoinColumn(name = "tech_head")
  private Users techHead;

  @ManyToOne
  @JoinColumn(name = "data_head")
  private Users dataHead;


  @ManyToOne
  @JoinColumn(name = "how_head")
  private Users howHead;

  private String name;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private BussinessCategory category;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  private String summary;

  @Pattern(regexp = ".*\\.(jpg|jpeg|png|pdf|docx)$")
  private String file;

  // @Pattern(regexp = "^[0-9]+(\\.[0-9]+)?$", message = "Budget must be a valid
  // numeric value")
  private String budget;

  // @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid
  // three-letter code (e.g., USD)")

  private String currency;

  @Column(name = "prod_owner")
  private String prodOwner;

  private int createdBy;

  private String techOwner;
  private String dataOwner;
  @Column(name = "how_owner")
  private String howOwner;
  @Enumerated(EnumType.STRING)
  private ProductStatus status;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "is_deleted")
  private Boolean isDeleted;

}
