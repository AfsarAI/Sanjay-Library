package com.digitallibrary.core.config;

import com.digitallibrary.modules.library.Library;
import com.digitallibrary.modules.library.LibraryRepository;
import com.digitallibrary.modules.library.LibrarySettings;
import com.digitallibrary.modules.library.LibrarySettingsRepository;
import com.digitallibrary.modules.user.User;
import com.digitallibrary.modules.user.UserRepository;
import com.digitallibrary.modules.user.UserRole;
import com.digitallibrary.modules.user.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final LibrarySettingsRepository librarySettingsRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository,
                          LibraryRepository libraryRepository,
                          LibrarySettingsRepository librarySettingsRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.libraryRepository = libraryRepository;
        this.librarySettingsRepository = librarySettingsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Ensure Library 1 exists
        Library library = libraryRepository.findById(1L).orElseGet(() -> {
            Library lib = new Library(
                    "Apex Self-Study Digital Library",
                    "Main Market Road, Near City Center, Civil Lines",
                    "+91-9876543210",
                    LocalTime.of(6, 0),
                    LocalTime.of(23, 0),
                    50
            );
            return libraryRepository.save(lib);
        });

        // Ensure Library Settings exist
        librarySettingsRepository.findByLibraryId(library.getId()).orElseGet(() -> {
            LibrarySettings settings = new LibrarySettings(
                    library.getId(),
                    new BigDecimal("700.00"),
                    7,
                    8,
                    15,
                    10,
                    true
            );
            return librarySettingsRepository.save(settings);
        });

        // Ensure Admin exists: Phone 9876543210, Password Admin@123
        if (!userRepository.existsByPhoneNumber("9876543210")) {
            User admin = new User(
                    library.getId(),
                    "9876543210",
                    "owner@digitallibrary.local",
                    passwordEncoder.encode("Admin@123"),
                    "Rajesh Sharma (Owner)",
                    UserRole.ROLE_ADMIN
            );
            admin.setStatus(UserStatus.ACTIVE);
            userRepository.save(admin);
            log.info("Seeded default administrator account: 9876543210 / Admin@123");
        }

        // Ensure Demo Student exists: Phone 9123456780, Password Student@123
        if (!userRepository.existsByPhoneNumber("9123456780")) {
            User student = new User(
                    library.getId(),
                    "9123456780",
                    "rahul.kumar@example.com",
                    passwordEncoder.encode("Student@123"),
                    "Rahul Kumar",
                    UserRole.ROLE_STUDENT
            );
            student.setStatus(UserStatus.ACTIVE);
            userRepository.save(student);
            log.info("Seeded default demo student account: 9123456780 / Student@123");
        }
    }
}
