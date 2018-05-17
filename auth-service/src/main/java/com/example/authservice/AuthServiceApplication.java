package com.example.authservice;

import java.security.Principal;
import java.util.Optional;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@EnableResourceServer
@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}

@RestController
class PrincipalRestController {
	
	@RequestMapping("/user")
	Principal principal(Principal principal){
		return principal;
	}
}

@Configuration
@EnableAuthorizationServer
class OAuthConfiguration extends AuthorizationServerConfigurerAdapter{
	private final AuthenticationManager authenticationManager;
	@Autowired
	public OAuthConfiguration(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}
	
//	@Override 
//	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
//		security
//        .tokenKeyAccess("permitAll()")
//        .checkTokenAccess("isAuthenticated()")
//        .allowFormAuthenticationForClients();
//	}
	
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.authenticationManager(this.authenticationManager);
	}
	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients
			.inMemory()
			.withClient("html5")
			.secret("secret")
			.authorizedGrantTypes("password")
			.scopes("openid");
	}
}


@Component
class AccountCreator implements CommandLineRunner{
	@Override
	public void run(String... arg0) throws Exception {
		Stream.of("vikash,password", "hetal,vikash", "mintoo,vidhi", "vidhi,kindle")
		.map(x -> x.split(","))
		.forEach( tuple -> {
			this.accountRepository.save(new Account(
					tuple[0],
					tuple[1],
					true
					));
		});
		this.accountRepository.findAll().forEach(account -> {
			System.out.println(account.toString());
		});
		
	}
	
	private final AccountRepository accountRepository;
	
	@Autowired
	public AccountCreator(AccountRepository accRepository) {
		this.accountRepository = accRepository;
	}
}

@Service
class AccountUserDetailsService implements UserDetailsService {
	private final AccountRepository accountRepository;
	
	@Autowired
	public AccountUserDetailsService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("Trying to find user...."+username +"\n"+ accountRepository.findByUsername(username));
		return accountRepository.findByUsername(username)
		.map(account -> new User(account.getUsername(),
				account.getPassword(),
				account.isActive(),
				account.isActive(), 
				account.isActive(), 
				account.isActive(), 
				AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER")
				))
		.orElseThrow(() -> new UsernameNotFoundException("user("+username+") not found!"));
	}
	
}

interface AccountRepository extends JpaRepository<Account, Long> {
	Optional<Account> findByUsername(String username);
}


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
class Account {
	@Id
	@GeneratedValue
	private Long id;
	private String username, password;
	private boolean isActive;
	
	public Account(String username, String password, boolean isActive) {
		this.username = username;
		this.password = password;
		this.isActive = isActive;
	}
	
}