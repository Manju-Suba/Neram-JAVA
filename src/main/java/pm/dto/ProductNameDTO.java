package pm.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductNameDTO {
    private Integer id;
    private String name;
    private String profilepic;
    private List<String> role;
	public ProductNameDTO(Integer id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
    
    
    
}
