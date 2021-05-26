package vrptwfl.metaheuristic.functionalityChecks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO read documentary http://logback.qos.ch/manual/introduction.html

// based on https://www.youtube.com/watch?v=oiaEP57nsmI by Marcus Biel
public class LoggerCarService {

    private final Logger logger = LoggerFactory.getLogger(LoggerCarService.class);

    public void process(String input){
        logger.debug("processing car: {}", input);
        logger.info("still processing car: {}", input);
    }

    public static void main(String[] args) {
        LoggerCarService service = new LoggerCarService();
        service.process("BMW");
    }
}
