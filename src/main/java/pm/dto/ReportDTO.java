package pm.dto;


import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReportDTO {
	
	private String name;
	private String email;
	private LocalDateTime date_of_joining;

	
	@Data
	public class ProductDetailDTO{
		private LocalDate date;
		private String day;
		private String attendance_status;
		private String product;
		private String task;
		private String description;
		private String task_status;
		private String task_work_hour;
		private String total_work_hours;
		
	
    
	}
   
    
    

}


