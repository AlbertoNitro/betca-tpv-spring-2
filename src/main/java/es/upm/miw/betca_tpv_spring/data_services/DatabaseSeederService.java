package es.upm.miw.betca_tpv_spring.data_services;

import es.upm.miw.betca_tpv_spring.documents.*;
import es.upm.miw.betca_tpv_spring.repositories.*;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class DatabaseSeederService {

    public static final String VARIOUS_CODE = "1";
    public static final String VARIOUS_NAME = "Various";

    @Value("${miw.admin.mobile}")
    private String mobile;
    @Value("${miw.admin.username}")
    private String username;
    @Value("${miw.admin.password}")
    private String password;

    private TicketRepository ticketRepository;
    private InvoiceRepository invoiceRepository;
    private CashierClosureRepository cashierClosureRepository;
    private Environment environment;
    private UserRepository userRepository;
    private VoucherRepository voucherRepository;
    private ProviderRepository providerRepository;
    private ArticleRepository articleRepository;
    private BudgetRepository budgetRepository;
    private ArticlesFamilyRepository articlesFamilyRepository;
    private FamilyArticleRepository familyArticleRepository;
    private FamilyCompositeRepository familyCompositeRepository;
    private OrderRepository orderRepository;
    private TagRepository tagRepository;
    private CustomerDiscountRepository customerDiscountRepository;
    private SendingsRepository sendingsRepository;
    private StaffRepository staffRepository;
    private StockAlarmRepository stockAlarmRepository;

    @Autowired
    public DatabaseSeederService(
            TicketRepository ticketRepository,
            InvoiceRepository invoiceRepository,
            CashierClosureRepository cashierClosureRepository,
            Environment environment,
            UserRepository userRepository,
            VoucherRepository voucherRepository,
            ProviderRepository providerRepository,
            ArticleRepository articleRepository,
            BudgetRepository budgetRepository,
            FamilyArticleRepository familyArticleRepository,
            FamilyCompositeRepository familyCompositeRepository,
            OrderRepository orderRepository,
            TagRepository tagRepository,
            ArticlesFamilyRepository articlesFamilyRepository,
            CustomerDiscountRepository customerDiscountRepository,
            SendingsRepository sendingsRepository,
            StaffRepository staffRepository,
            StockAlarmRepository stockAlarmRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.invoiceRepository = invoiceRepository;
        this.cashierClosureRepository = cashierClosureRepository;
        this.environment = environment;
        this.userRepository = userRepository;
        this.voucherRepository = voucherRepository;
        this.providerRepository = providerRepository;
        this.articleRepository = articleRepository;
        this.budgetRepository = budgetRepository;
        this.familyArticleRepository = familyArticleRepository;
        this.familyCompositeRepository = familyCompositeRepository;
        this.articlesFamilyRepository = articlesFamilyRepository;
        this.orderRepository = orderRepository;
        this.tagRepository = tagRepository;
        this.customerDiscountRepository = customerDiscountRepository;
        this.sendingsRepository = sendingsRepository;
        this.staffRepository = staffRepository;
        this.stockAlarmRepository = stockAlarmRepository;
    }

    @PostConstruct
    public void constructor() {
        String[] profiles = this.environment.getActiveProfiles();
        if (Arrays.asList(profiles).contains("dev")) {
            this.deleteAllAndInitializeAndSeedDataBase();
        } else if (Arrays.asList(profiles).contains("prod")) {
            this.initialize();
        }
    }

    private void initialize() {
        if (!this.userRepository.findByMobile(this.mobile).isPresent()) {
            LogManager.getLogger(this.getClass()).warn("------- Create Admin -----------");
            User user = new User(this.mobile, this.username, this.password);
            user.setRoles(new Role[]{Role.ADMIN});
            this.userRepository.save(user);
        }
        CashierClosure cashierClosure = this.cashierClosureRepository.findFirstByOrderByOpeningDateDesc();
        if (cashierClosure == null) {
            LogManager.getLogger(this.getClass()).warn("------- Create cashierClosure -----------");
            cashierClosure = new CashierClosure(BigDecimal.ZERO);
            cashierClosure.close(BigDecimal.ZERO, BigDecimal.ZERO, "Initial");
            this.cashierClosureRepository.save(cashierClosure);
        }
        if (!this.articleRepository.existsById(VARIOUS_CODE)) {
            LogManager.getLogger(this.getClass()).warn("------- Create Article Various -----------");
            Provider provider = Provider.builder(VARIOUS_NAME).build();
            this.providerRepository.save(provider);
            this.articleRepository.save(Article.builder(VARIOUS_CODE).reference(VARIOUS_NAME).description(VARIOUS_NAME)
                    .retailPrice("100.00").stock(1000).provider(provider).build());
        }
    }

    public void deleteAllAndInitialize() {
        LogManager.getLogger(this.getClass()).warn("------- Delete All -----------");
        // Delete Repositories -----------------------------------------------------
        this.familyCompositeRepository.deleteAll();
        this.invoiceRepository.deleteAll();

        this.budgetRepository.deleteAll();
        this.familyArticleRepository.deleteAll();
        this.orderRepository.deleteAll();
        this.tagRepository.deleteAll();
        this.ticketRepository.deleteAll();
        this.articleRepository.deleteAll();

        this.cashierClosureRepository.deleteAll();
        this.providerRepository.deleteAll();
        this.userRepository.deleteAll();
        this.voucherRepository.deleteAll();
        this.sendingsRepository.deleteAll();
        this.staffRepository.deleteAll();
        this.stockAlarmRepository.deleteAll();
        // -------------------------------------------------------------------------
        this.initialize();
    }

    public void deleteAllAndInitializeAndSeedDataBase() {
        this.deleteAllAndInitialize();
        this.seedDataBaseJava();
    }

    public void seedDataBaseJava() {
        LogManager.getLogger(this.getClass()).warn("------- Initial Load from JAVA -----------");
        Role[] allRoles = {Role.ADMIN, Role.MANAGER, Role.OPERATOR};
        User[] users = {
                new User("666666000", "all-roles", "p000", null, "C/TPV, 0, MIW", "u000@gmail.com", allRoles),
                new User("666666001", "manager", "p001", "66666601C", "C/TPV, 1", "u001@gmail.com", Role.MANAGER),
                new User("666666002", "u002", "p002", "66666602K", "C/TPV, 2", "u002@gmail.com", Role.OPERATOR),
                new User("666666003", "u003", "p003", "66666603E", "C/TPV, 3", "u003@gmail.com", Role.OPERATOR),
                new User("666666004", "u004", "p004", "66666604T", "C/TPV, 4", "u004@gmail.com", Role.CUSTOMER),
                new User("666666005", "u005", "p005", "66666605R", "C/TPV, 5", "u005@gmail.com", Role.CUSTOMER),
        };
        this.userRepository.saveAll(Arrays.asList(users));
        LogManager.getLogger(this.getClass()).warn("        ------- users");
        Voucher[] vouchers = {
                new Voucher(new BigDecimal("66.666")),
                new Voucher(new BigDecimal("11.111")),
                new Voucher(new BigDecimal("50")),
        };
        vouchers[1].use();
        this.voucherRepository.saveAll(Arrays.asList(vouchers));
        LogManager.getLogger(this.getClass()).warn("        ------- vouchers");

        Sendings[] sendings = {
                new Sendings("202003114", "all-roles"),
                new Sendings("202003115", "u002"),
                new Sendings("202003116", "u003"),
        };
        this.sendingsRepository.saveAll(Arrays.asList(sendings));
        LogManager.getLogger(this.getClass()).warn("        ------- sendings");

        Staff[] staff = {
                new Staff("6661", "2020", "3", "13", 4.00f, LocalDateTime.of(2020, 3, 13, 9, 0, 0)),
                new Staff("6662", "2020", "3", "13", 2.00f, LocalDateTime.of(2020, 3, 13, 8, 0, 0)),
                new Staff("6663", "2020", "3", "13", 0.00f, LocalDateTime.of(2020, 3, 13, 7, 0, 0))
        };
        this.staffRepository.saveAll(Arrays.asList(staff));
        LogManager.getLogger(this.getClass()).warn("        ------- staffs");

        Provider[] providers = {
                Provider.builder("pro1").nif("12345678b").address("C/TPV-pro, 1").phone("9166666601").email("p1@gmail.com").note("p1").active(true).build(),
                Provider.builder("pro2").nif("12345678z").address("C/TPV-pro, 2").phone("9166666602").email("p2@gmail.com").note("p2").active(false).build()
        };
        this.providerRepository.saveAll(Arrays.asList(providers));
        LogManager.getLogger(this.getClass()).warn("        ------- providers");
        Article[] articles = {
                Article.builder("8400000000017").reference("Zz Falda T2").description("Zarzuela - Falda T2")
                        .retailPrice("20").stock(10).provider(providers[0]).build(),
                Article.builder("8400000000024").reference("Zz Falda T4").description("Zarzuela - Falda T4")
                        .retailPrice("27.8").stock(5).provider(providers[0]).build(),
                Article.builder("8400000000031").reference("ref-a3").description("descrip-a3")
                        .retailPrice("10.12").stock(8).tax(Tax.FREE).provider(providers[0]).build(),
                Article.builder("8400000000048").reference("ref-a4").description("descrip-a4")
                        .retailPrice("0.23").stock(1).tax(Tax.REDUCED).provider(providers[0]).build(),
                Article.builder("8400000000055").reference("ref-a5").description("descrip-a5")
                        .retailPrice("0.23").stock(0).tax(Tax.SUPER_REDUCED).provider(providers[0]).build(),
                Article.builder("8400000000062").reference("ref-a6").description("descrip-a6")
                        .retailPrice("0.01").stock(0).discontinued(true).provider(providers[1]).build(),
                Article.builder("8400000000079").reference("Zz Polo T2").description("Zarzuela - Polo T2")
                        .retailPrice("16").stock(10).provider(providers[0]).build(),
                Article.builder("8400000000086").reference("Zz polo T4").description("Zarzuela - Polo T4")
                        .retailPrice("17.8").stock(5).provider(providers[0]).build(),

        };
        this.articleRepository.saveAll(Arrays.asList(articles));
        LogManager.getLogger(this.getClass()).warn("        ------- articles");
        Tag[] tags = {
                new Tag("tag1", Arrays.asList(articles[0], articles[1], articles[2])),
                new Tag("tag2", Arrays.asList(articles[0], articles[1], articles[4])),
        };
        this.tagRepository.saveAll(Arrays.asList(tags));// subscribe() for not blocking
        LogManager.getLogger(this.getClass()).warn("        ------- tags");
        Shopping[] shoppingList = {
                new Shopping(1, BigDecimal.ZERO, ShoppingState.COMMITTED, articles[0].getCode(),
                        articles[0].getDescription(), articles[0].getRetailPrice()),
                new Shopping(3, new BigDecimal("50"), ShoppingState.NOT_COMMITTED, articles[1].getCode(),
                        articles[1].getDescription(), articles[1].getRetailPrice()),
                new Shopping(1, BigDecimal.TEN, ShoppingState.COMMITTED, articles[0].getCode(),
                        articles[0].getDescription(), articles[0].getRetailPrice()),
                new Shopping(3, new BigDecimal("50"), ShoppingState.COMMITTED, articles[2].getCode(),
                        articles[2].getDescription(), articles[2].getRetailPrice()),
                new Shopping(3, BigDecimal.ZERO, ShoppingState.COMMITTED, articles[4].getCode(),
                        articles[4].getDescription(), articles[4].getRetailPrice()),
        };
        Ticket[] tickets = {
                new Ticket(1, BigDecimal.TEN, new BigDecimal("25.0"), BigDecimal.ZERO,
                        new Shopping[]{shoppingList[0], shoppingList[1]}, users[4], "note"),
                new Ticket(2, new BigDecimal("18.0"), BigDecimal.ZERO, BigDecimal.ZERO,
                        new Shopping[]{shoppingList[2]}, users[4], "note"),
                new Ticket(3, BigDecimal.ZERO, new BigDecimal("16.18"), new BigDecimal("5"),
                        new Shopping[]{shoppingList[3], shoppingList[4]}, null, "note"),
        };
        tickets[0].setId("201901121");
        tickets[1].setId("201901122");
        tickets[2].setId("201901123");
        this.ticketRepository.saveAll(Arrays.asList(tickets));
        LogManager.getLogger(this.getClass()).warn("        ------- tickets");
        Invoice[] invoices = {
                new Invoice(1, users[4], tickets[1])
        };
        this.invoiceRepository.saveAll(Arrays.asList(invoices));
        LogManager.getLogger(this.getClass()).warn("        ------- invoices");
        Budget[] budgets = {
                new Budget(new Shopping[]{shoppingList[0], shoppingList[1]})
        };
        this.budgetRepository.saveAll(Arrays.asList(budgets));
        LogManager.getLogger(this.getClass()).warn("        ------- budgets");
        ArticlesFamily[] familyArticleList = {
                new FamilyArticle(articles[0]),
                new FamilyArticle(articles[1]),
                new FamilyArticle(articles[2]),
                new FamilyArticle(articles[3]),
                new FamilyArticle(articles[4]),
                new FamilyArticle(articles[5]),
                new FamilyArticle(articles[6]),
                new FamilyArticle(articles[7]),
        };
        this.articlesFamilyRepository.saveAll(Arrays.asList(familyArticleList));
        ArticlesFamily[] familyCompositeSizesList = {
                new FamilyComposite(FamilyType.SIZES, "Zz Falda", "Zarzuela - Falda"),
                new FamilyComposite(FamilyType.SIZES, "Zz Polo", "Zarzuela - Polo")
        };
        familyCompositeSizesList[0].add(familyArticleList[0]);
        familyCompositeSizesList[0].add(familyArticleList[1]);
        familyCompositeSizesList[1].add(familyArticleList[6]);
        familyCompositeSizesList[1].add(familyArticleList[7]);
        this.articlesFamilyRepository.saveAll(Arrays.asList(familyCompositeSizesList));
        ArticlesFamily[] familyCompositeArticlesList = {
                new FamilyComposite(FamilyType.ARTICLES, "root", "root"),
                new FamilyComposite(FamilyType.ARTICLES, "Zz", "Zarzuela"),
                new FamilyComposite(FamilyType.ARTICLES, "varios", "varios"),
        };
        this.articlesFamilyRepository.saveAll(Arrays.asList(familyCompositeArticlesList));
        familyCompositeArticlesList[0].add(familyCompositeArticlesList[1]);
        familyCompositeArticlesList[0].add(familyCompositeArticlesList[2]);
        familyCompositeArticlesList[0].add(familyArticleList[2]);
        familyCompositeArticlesList[1].add(familyCompositeSizesList[0]);
        familyCompositeArticlesList[1].add(familyCompositeSizesList[1]);
        familyCompositeArticlesList[1].add(familyArticleList[3]);
        familyCompositeArticlesList[2].add(familyArticleList[4]);
        familyCompositeArticlesList[2].add(familyArticleList[5]);
        this.articlesFamilyRepository.saveAll(Arrays.asList(familyCompositeArticlesList));
        LogManager.getLogger(this.getClass()).warn("        ------- articles family");
        OrderLine[] orderLines = {
                new OrderLine(articles[0], 10),
                new OrderLine(articles[1], 8),
                new OrderLine(articles[2], 6),
                new OrderLine(articles[3], 4),
        };
        Order[] orders = {
                new Order("order1", providers[0], orderLines)
        };
        this.orderRepository.saveAll(Arrays.asList(orders));
        LogManager.getLogger(this.getClass()).warn("        ------- orders");
        CustomerDiscount[] customerDiscounts = {
                new CustomerDiscount("discount1", BigDecimal.TEN, BigDecimal.TEN, users[4])
        };
        this.customerDiscountRepository.saveAll(Arrays.asList(customerDiscounts));
        LogManager.getLogger(this.getClass()).warn("        ------- customerDiscounts");

        AlarmArticle[] alarmArticles = {
                new AlarmArticle("1", 1, 1),
                new AlarmArticle("2", 2, 2)
        };

        StockAlarm[] stockAlarms = {
                new StockAlarm("111", "1111", "upm", 1, 1, alarmArticles),
                new StockAlarm("222", "2222", "upm", 2, 2, alarmArticles)
        };
        this.stockAlarmRepository.saveAll(Arrays.asList(stockAlarms));
        LogManager.getLogger(this.getClass()).warn("        ------- stockAlarms");
    }

}



