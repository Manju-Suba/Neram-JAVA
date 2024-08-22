package pm.utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import pm.dto.UserDTO;
import pm.response.OwnerDetails;

@Component
public class CommonFunct {

    @Value("${fileBasePath}")
	private  String fileBasePath;
    
    public List<UserDTO> commonFunction1(List<UserDTO> userDTOs) {
        List<UserDTO> userDTOsall = new ArrayList();
        for (UserDTO userDTO : userDTOs) {
			String profilePicPath = fileBasePath + userDTO.getProfile_pic(); // Assuming profilePic is the file name
			String profilegetPath = userDTO.getProfile_pic(); // direct file name passed here...
			Path filePath = Paths.get(profilePicPath);
			
			// Check if the profile picture exists
			if (Files.exists(filePath)) {
				userDTO.setProfile_pic(profilegetPath);
			} else {
				userDTO.setProfile_pic(null);

			}
            userDTOsall.add(userDTO);
		}
        return userDTOsall;
        // Implement your common functionality here
    }

	public  List<OwnerDetails> commonFunctionForProduct(List<OwnerDetails> userDTOs) {
		List<OwnerDetails> userDTOsall = new ArrayList();

		for (OwnerDetails userDTO : userDTOs) {

			String profilePicPath = fileBasePath + userDTO.getProfilePic(); // Assuming profilePic is the file name
			String profilegetPath = userDTO.getProfilePic(); // direct file name passed here...
			Path filePath = Paths.get(profilePicPath);

			// Check if the profile picture exists
			if (Files.exists(filePath)) {
				userDTO.setProfilePic(profilegetPath);
			} else {
				userDTO.setProfilePic(null);
			}
			userDTOsall.add(userDTO);
		}
		return userDTOsall;
		// Implement your common functionality here
	}

}
