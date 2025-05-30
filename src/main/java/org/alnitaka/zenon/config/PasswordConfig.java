package org.alnitaka.zenon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {
	
	/**
	 * Configure un encodeur Argon2 avec des paramètres raisonnables par défaut
	 * pour Spring Security 5.8+.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		// Constructeur par défaut adapté à Spring Security v5.8 :
		// saltLength=16, hashLength=32, parallelism=1, memory=1<<12 KB (4 096 KB), iterations=3
		return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
		
		// Si vous voulez personnaliser les paramètres, utilisez :
		// return new Argon2PasswordEncoder(
		//     saltLength,    // longueur du sel en octets (ex.16)
		//     hashLength,    // taille du hash en octets (ex.32)
		//     parallelism,   // nombre de threads (ex.1)
		//     memory,        // mémoire en KB (ex.1<<12 = 4096 KB)
		//     iterations     // nombre d’itérations (ex.3)
		// );
	}
}
