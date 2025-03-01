@SpringBootApplication
@RestController
public class DefaultController {
    public static void main(String[] args) {
        SpringApplication.run(DefaultController.class, args);
    }

    @GetMapping("/test")
    public String test() {
        return "Backend działa! 🚀";
    }
}