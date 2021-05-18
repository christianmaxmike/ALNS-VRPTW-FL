import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class Config {

    public void readConfig() {
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("config.yaml");
        Map<String, Object> obj = yaml.load(inputStream);
        System.out.println(obj);
    }

}
