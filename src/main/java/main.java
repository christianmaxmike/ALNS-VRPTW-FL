import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

class Config() {

    public void config() {
        
        }

    public void readConfig() {
            Yaml yaml = new Yaml();
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("config.yaml");
            Map<String, Object> obj = yaml.load(inputStream);
            System.out.println(obj);
            }

        }

public class main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");

        Config config = new Config();
    }


}

