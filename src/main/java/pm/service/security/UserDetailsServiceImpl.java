package pm.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import pm.model.users.Users;
import pm.repository.UsersRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired
	UsersRepository usersRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Users users= usersRepository.findByUsername(username)
				.orElseThrow(()->new UsernameNotFoundException("User Not Found with username"+username));
		
		return UserDetailsImpl.build(users);
	}

}
