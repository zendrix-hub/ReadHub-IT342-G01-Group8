package com.readhub.bookmanagement.config;

import com.readhub.bookmanagement.model.Book;
import com.readhub.bookmanagement.model.Category;
import com.readhub.bookmanagement.model.Role;
import com.readhub.bookmanagement.model.User;
import com.readhub.bookmanagement.repository.BookRepository;
import com.readhub.bookmanagement.repository.CategoryRepository;
import com.readhub.bookmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Categories
        Category tech = seedCategory("Technology");
        Category science = seedCategory("Science");
        seedCategory("Fiction");
        seedCategory("Self-help");
        seedCategory("Other");

        // 2. Seed Admin
        if (!userRepository.existsByEmail("admin@readhub.com")) {
            User admin = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email("admin@readhub.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Admin seeded: admin@readhub.com");
        }

        // 3. Seed Student (Uno) - NOW PERSISTENT!
        if (!userRepository.existsByEmail("uno@readhub.com")) {
            User student = User.builder()
                    .firstName("Uno")
                    .lastName("Student")
                    .email("uno@readhub.com")
                    .passwordHash(passwordEncoder.encode("student123"))
                    .role(Role.STUDENT)
                    .build();
            userRepository.save(student);
            log.info("Student seeded: uno@readhub.com");
        }

        // 4. Seed a Sample Book (Optional, for demo)
        if (bookRepository.count() == 0) {
            Book b1 = Book.builder()
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .isbn("978-0132350884")
                    .publicationYear(2008)
                    .totalCopies(5)
                    .availableCopies(5)
                    .category(tech)
                    .build();
            bookRepository.save(b1);
            log.info("Sample Book seeded");
        }
    }

    private Category seedCategory(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName(name);
                    return categoryRepository.save(c);
                });
    }
}