package com.leoshop.config;

import com.leoshop.model.Product;
import com.leoshop.model.User;
import com.leoshop.repository.ProductRepository;
import com.leoshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Admin user
        if (!userRepository.existsByEmail("admin@leoshop.com")) {
            userRepository.save(User.builder()
                    .name("Admin").email("admin@leoshop.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN).build());
            log.info("Default admin user created: admin@leoshop.com");
        }

        // Mock users
        if (!userRepository.existsByEmail("user1@leoshop.com")) {
            userRepository.save(User.builder()
                    .name("王小明").email("user1@leoshop.com")
                    .password(passwordEncoder.encode("user123"))
                    .phone("0912345678").role(User.Role.USER).build());
            userRepository.save(User.builder()
                    .name("李小華").email("user2@leoshop.com")
                    .password(passwordEncoder.encode("user123"))
                    .phone("0923456789").role(User.Role.USER).build());
            log.info("Mock users created");
        }

        // Mock products
        if (productRepository.count() == 0) {
            productRepository.saveAll(List.of(
                product("經典白色 T-Shirt", "classic-white-tshirt", "舒適純棉白色短袖 T恤", 590, 790, "上衣", 100,
                        "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500"),
                product("修身牛仔褲", "slim-fit-jeans", "彈性修身直筒牛仔褲", 1290, 1690, "褲子", 50,
                        "https://images.unsplash.com/photo-1542272604-787c3835535d?w=500"),
                product("連帽外套", "hoodie-jacket", "保暖刷毛連帽外套", 1490, 1990, "外套", 30,
                        "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=500"),
                product("運動短褲", "sport-shorts", "透氣速乾運動短褲", 690, 890, "褲子", 80,
                        "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=500"),
                product("格紋襯衫", "plaid-shirt", "休閒格紋長袖襯衫", 890, 1190, "上衣", 40,
                        "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500"),
                product("皮革手提包", "leather-handbag", "真皮手提斜背兩用包", 2490, 3290, "配件", 20,
                        "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=500"),
                product("棒球帽", "baseball-cap", "經典刺繡棒球帽", 490, 690, "配件", 60,
                        "https://images.unsplash.com/photo-1588850561407-ed78c334e67a?w=500")
            ));
            log.info("Mock products created");
        }
    }

    private Product product(String name, String slug, String desc, int price, int comparePrice, String category, int stock, String imageUrl) {
        Product p = new Product();
        p.setName(name);
        p.setSlug(slug);
        p.setDescription(desc);
        p.setPrice(BigDecimal.valueOf(price));
        p.setComparePrice(BigDecimal.valueOf(comparePrice));
        p.setCategory(category);
        p.setStock(stock);
        p.setImageUrl(imageUrl);
        p.setActive(true);
        return p;
    }
}
