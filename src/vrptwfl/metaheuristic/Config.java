package vrptwfl.metaheuristic;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class Config {

    public void loadConfig() throws FileNotFoundException {
//        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        Yaml yaml = new Yaml();
//        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("resources/config.yaml");
        InputStream inputStream = new FileInputStream(new File("config.yaml"));
        Map<String, Object> obj = yaml.load(inputStream);
        System.out.println(obj);

        // https://stackabuse.com/reading-and-writing-yaml-files-in-java-with-snakeyaml/
//        InputStream inputStream = new FileInputStream(new File("resources/student.yaml"));
//
//        Yaml yaml = new Yaml();
//        Map<String, Object> data = yaml.load(inputStream);
//        System.out.println(data);

    }

    public static void main(String[] args) throws FileNotFoundException {

        Config config = new Config();
        config.loadConfig();
    }
}
