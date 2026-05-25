// package com.academy.eventhub;

// import com.academy.eventhub.entity.User;
// import com.academy.eventhub.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Component;

// @Component
// @RequiredArgsConstructor
// CommandLineRunner viene eseguito dopo che l'applicazione è partita e il database è pronto.
// public class DataInitializer implements CommandLineRunner {


//     private final UserRepository userRepository;
//     private final PasswordEncoder passwordEncoder;

//     @Override
//     public void run(String... args) {
//         createUserIfNotExists("user@eventhub.com", User.Role.USER);
//         createUserIfNotExists("organizer@eventhub.com", User.Role.ORGANIZER);
//         createUserIfNotExists("admin@eventhub.com", User.Role.ADMIN);
//     }

//     private void createUserIfNotExists(String email, User.Role role) {
//         if (userRepository.existsByEmail(email)) 
//             return; // evito che vengano ricreati ogni volta, se ci sono già: return.

//         User user = new User();
//         user.setEmail(email);
//         user.setPassword(passwordEncoder.encode("123"));
//         user.setRole(role);
//         user.setStatus(User.Status.ACTIVE);

//         userRepository.save(user);
//         System.out.println("Utente creato: " + email + " [" + role + "]");
//     }
// }