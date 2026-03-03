package com.leoshop.config;

import com.leoshop.model.PaymentMethod;
import com.leoshop.model.Product;
import com.leoshop.model.User;
import com.leoshop.repository.PaymentMethodRepository;
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
    private final PaymentMethodRepository paymentMethodRepository;
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
                product("經典白色 T-Shirt", "classic-white-tshirt", "舒適純棉白色短袖 T恤，適合日常穿搭", 590, 790, "上衣", 100,
                        "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600",
                        "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600,https://images.unsplash.com/photo-1622445275463-afa2ab738c34?w=600,https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=600"),
                product("修身牛仔褲", "slim-fit-jeans", "彈性修身直筒牛仔褲，經典百搭款", 1290, 1690, "褲子", 50,
                        "https://images.unsplash.com/photo-1542272604-787c3835535d?w=600",
                        "https://images.unsplash.com/photo-1542272604-787c3835535d?w=600,https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600,https://images.unsplash.com/photo-1604176354204-9268737828e4?w=600"),
                product("連帽外套", "hoodie-jacket", "保暖刷毛連帽外套，秋冬必備", 1490, 1990, "外套", 30,
                        "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=600",
                        "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=600,https://images.unsplash.com/photo-1578768079470-c6e3db5b1e75?w=600,https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=600"),
                product("運動短褲", "sport-shorts", "透氣速乾運動短褲，跑步健身最佳選擇", 690, 890, "褲子", 80,
                        "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=600",
                        "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=600,https://images.unsplash.com/photo-1562157873-818bc0726f68?w=600"),
                product("格紋襯衫", "plaid-shirt", "休閒格紋長袖襯衫，質感面料", 890, 1190, "上衣", 40,
                        "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600",
                        "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600,https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600,https://images.unsplash.com/photo-1589310243389-96a5483213a8?w=600"),
                product("皮革手提包", "leather-handbag", "真皮手提斜背兩用包，優雅實用", 2490, 3290, "配件", 20,
                        "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=600",
                        "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=600,https://images.unsplash.com/photo-1590874103328-eac38a683ce7?w=600,https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=600"),
                product("棒球帽", "baseball-cap", "經典刺繡棒球帽，街頭潮流", 490, 690, "配件", 60,
                        "https://images.unsplash.com/photo-1556306535-0f09a537f0a3?w=600",
                        "https://images.unsplash.com/photo-1556306535-0f09a537f0a3?w=600,https://images.unsplash.com/photo-1575428652377-a2d80e2277fc?w=600"),
                // New products
                product("羊毛大衣", "wool-coat", "高品質羊毛混紡大衣，保暖有型", 3990, 4990, "外套", 15,
                        "https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=600",
                        "https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=600,https://images.unsplash.com/photo-1544923246-77307dd270ce?w=600,https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=600"),
                product("帆布後背包", "canvas-backpack", "大容量帆布後背包，通勤旅行皆宜", 1290, 1590, "配件", 35,
                        "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600",
                        "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600,https://images.unsplash.com/photo-1581605405669-fcdf81165afa?w=600,https://images.unsplash.com/photo-1622260614153-03223fb72052?w=600"),
                product("Polo 衫", "polo-shirt", "商務休閒兩用 Polo 衫，透氣舒適", 790, 990, "上衣", 70,
                        "https://images.unsplash.com/photo-1625910513413-5fc421e0e5f0?w=600",
                        "https://images.unsplash.com/photo-1625910513413-5fc421e0e5f0?w=600,https://images.unsplash.com/photo-1586363104862-3a5e2ab60d99?w=600"),
                product("真皮皮帶", "leather-belt", "義大利頭層牛皮皮帶，自動扣設計", 890, 1290, "配件", 45,
                        "https://images.unsplash.com/photo-1553704571-c32d20e6c824?w=600",
                        "https://images.unsplash.com/photo-1553704571-c32d20e6c824?w=600,https://images.unsplash.com/photo-1624222247344-550fb60583dc?w=600"),
                product("防風夾克", "windbreaker", "輕量防風防潑水夾克，戶外必備", 1890, 2490, "外套", 25,
                        "https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=600",
                        "https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=600,https://images.unsplash.com/photo-1545594861-3bef43ff2fc8?w=600,https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?w=600"),
                product("卡其休閒褲", "chino-pants", "修身版型卡其褲，舒適彈性面料", 990, 1290, "褲子", 55,
                        "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=600",
                        "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=600,https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=600"),
                product("太陽眼鏡", "sunglasses", "UV400 偏光太陽眼鏡，時尚百搭", 690, 990, "配件", 90,
                        "https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=600",
                        "https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=600,https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=600,https://images.unsplash.com/photo-1577803645773-f96470509666?w=600"),
                product("針織毛衣", "knit-sweater", "柔軟親膚針織毛衣，秋冬百搭款", 1190, 1590, "上衣", 35,
                        "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=600",
                        "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=600,https://images.unsplash.com/photo-1620799139507-2a76f79a2f4d?w=600,https://images.unsplash.com/photo-1578587018452-892bacefd3f2?w=600")
            ));
            log.info("Mock products created");
        }

        // Default payment methods
        if (paymentMethodRepository.count() == 0) {
            paymentMethodRepository.saveAll(List.of(
                PaymentMethod.builder()
                    .name("Bitcoin").symbol("BTC").network("bitcoin")
                    .gateway("direct").enabled(false).sortOrder(1).build(),
                PaymentMethod.builder()
                    .name("USDT (TRC-20)").symbol("USDT").network("tron")
                    .gateway("direct").enabled(false).sortOrder(2).build(),
                PaymentMethod.builder()
                    .name("Ethereum").symbol("ETH").network("ethereum")
                    .gateway("direct").enabled(false).sortOrder(3).build()
            ));
            log.info("Default payment methods created");
        }
    }

    private Product product(String name, String slug, String desc, int price, int comparePrice, String category, int stock, String imageUrl, String imageUrls) {
        Product p = new Product();
        p.setName(name);
        p.setSlug(slug);
        p.setDescription(desc);
        p.setPrice(BigDecimal.valueOf(price));
        p.setComparePrice(BigDecimal.valueOf(comparePrice));
        p.setCategory(category);
        p.setStock(stock);
        p.setImageUrl(imageUrl);
        p.setImageUrls(imageUrls);
        p.setActive(true);
        return p;
    }
}
