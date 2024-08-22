package pm.controller.users;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class sampleController {
	
	@GetMapping("/login1")
	public String loginPage() {
		return "login";
	}
}
