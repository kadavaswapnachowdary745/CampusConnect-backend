package com.example.CampusConnectMP.config;

import com.example.CampusConnectMP.model.Category;
import com.example.CampusConnectMP.model.Product;
import com.example.CampusConnectMP.model.User;
import com.example.CampusConnectMP.repository.ProductRepository;
import com.example.CampusConnectMP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("admin@college.edu")) {
            User admin = User.builder()
                    .name("Admin")
                    .email("admin@college.edu")
                    .password(passwordEncoder.encode("Admin@123"))
                    .blocked(false)
                    .build();
            userRepository.save(admin);
            System.out.println("Created default admin user: admin@college.edu / Admin@123");
        }

        // Seed sample data if empty (so analytics has content)
        if (userRepository.count() < 3) {
            User user1 = User.builder()
                    .name("Alice")
                    .email("alice@college.edu")
                    .password(passwordEncoder.encode("Password1!"))
                    .blocked(false)
                    .build();
            User user2 = User.builder()
                    .name("Bob")
                    .email("bob@college.edu")
                    .password(passwordEncoder.encode("Password2!"))
                    .blocked(false)
                    .build();
            userRepository.saveAll(List.of(user1, user2));
            System.out.println("Created sample user accounts");
        }

        if (productRepository.count() == 0) {
            User seller = userRepository.findByEmail("alice@college.edu").orElse(userRepository.findByEmail("admin@college.edu").orElse(null));
            if (seller != null) {
                productRepository.saveAll(List.of(
                        Product.builder().title("Physics Textbook").description("Intro physics").price(49.99).category(Category.BOOKS).sold(false).seller(seller).build(),
                        Product.builder().title("Lab Microscope").description("Basic optical microscope").price(125.00).category(Category.LAB_EQUIPMENT).sold(true).seller(seller).build(),
                        Product.builder().title("Calculator").description("Graphing calculator").price(70.00).category(Category.ELECTRONICS).sold(false).seller(seller).build(),
                        Product.builder().title("Used Laptop").description("Core i5 with 8GB RAM").price(280.00).category(Category.ELECTRONICS).sold(true).seller(seller).build(),
                        Product.builder().title("Project Journal").description("Engineering project report") .price(15.00).category(Category.PROJECT_MATERIALS).sold(false).seller(seller).build()
                ));
                System.out.println("Seeded sample products for analytics");
            }
        }
    }
}
