package vrptwfl.metaheuristic.functionalityChecks;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GuavaSimpleCache {

    private LoadingCache<String, String> cache;

    GuavaSimpleCache() {
        this.cache = CacheBuilder.newBuilder().refreshAfterWrite(2, TimeUnit.SECONDS).build(new CacheLoader<String, String>() {
            @Override
            public String load(String s) throws Exception {
                return addCache(s);
            }
        });
    }

    private String addCache(String s) {
        System.out.println("adding cache");
        return s.toUpperCase();
    }

    private String getEntry(String args) throws ExecutionException {
        System.out.println("cache.size() " + cache.size());
        return this.cache.get(args);
    }

    public static void main(String[] args)  {
        GuavaSimpleCache gt = new GuavaSimpleCache();
        try {
            System.out.println(gt.getEntry("Alex"));
            Thread.sleep(500);
            System.out.println(gt.getEntry("Alex"));
            Thread.sleep(2100);
            System.out.println(gt.getEntry("Alex"));
            Thread.sleep(500);
            System.out.println(gt.getEntry("Alex"));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }


    }

}
