package vrptwfl.metaheuristic.utils;

import java.util.List;

public class DataUtils {

    public static int[] convertListToArray(List<Integer> listResult) {
        int[] result = new int[listResult.size()];
        int i = 0;
        for (int num : listResult) {
            result[i++] = num;
        }
        return result;
    }

}
