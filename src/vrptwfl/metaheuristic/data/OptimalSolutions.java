package vrptwfl.metaheuristic.data;

import java.util.Map;

import static java.util.Map.entry;

/**
 * This class stores the best scores reported in the literature for
 * the solomon instances. 
 * 
 * @author: Alexander Jungwirth
 */
public class OptimalSolutions {

    // private static OptimalSolutions opt = new OptimalSolutions();

    // 25, 50, 100 customers
    public static Map<String, double[]> optimalObjFuncValue = Map.ofEntries(
            entry("R101",  new double[] {617.1, 1044.0,  1637.7}),
            entry("R102",  new double[] {547.1, 909, 1466.6}),
            entry("R103",  new double[] {454.6, 772.9, 1208.7}),
            entry("R104",  new double[] {416.9, 625.4,  971.5}),
            entry("R105",  new double[] {530.5, 899.3,  1355.3}),
            entry("R106",  new double[] {465.4, 793,  1234.6}),
            entry("R107",  new double[] {424.3, 711.1, 1064.6}),
            entry("R108",  new double[] {397.3, 617.7, 932.1}),
            entry("R109",  new double[] {441.3, 786.8, 1146.9}),
            entry("R110",  new double[] {444.1, 697.0, 1068}),
            entry("R111",  new double[] {428.8, 707.2, 1048.7}),
            entry("R112",  new double[] {393, 630.2, 948.6}),
            entry("C101",  new double[] {191.3, 362.4, 827.3}),
            entry("C102",  new double[] {190.3, 361.4, 827.3}),
            entry("C103",  new double[] {190.3, 361.4, 826.3}),
            entry("C104",  new double[] {186.9, 358.0, 822.9}),
            entry("C105",  new double[] {191.3, 362.4, 827.3}),
            entry("C106",  new double[] {191.3, 362.4, 827.3}),
            entry("C107",  new double[] {191.3, 362.4, 827.3}),
            entry("C108",  new double[] {191.3, 362.4, 827.3}),
            entry("C109",  new double[] {191.3, 362.4, 827.3}),
            entry("RC101", new double[] {461.1, 944,  1619.8}),
            entry("RC102", new double[] {351.8, 822.5, 1457.4}),
            entry("RC103", new double[] {332.8, 710.9, 1258.0}),
            entry("RC104", new double[] {306.6, 545.8, 1132.3}),
            entry("RC105", new double[] {411.3, 855.3, 1513.7}),
            entry("RC106", new double[] {345.5, 723.2, 1372.7}),
            entry("RC107", new double[] {298.3, 642.7, 1207.8}),
            entry("RC108", new double[] {294.5, 598.1, 1114.2})
    );

    // 25, 50, 100 customers
    public static Map<String, int[]> optimalNrOfVehicles = Map.ofEntries(
            entry("R101",  new int[] {8, 12, 20}),
            entry("R102",  new int[] {7, 11, 18}),
            entry("R103",  new int[] {5, 9, 14}),
            entry("R104",  new int[] {4, 6, 11}),
            entry("R105",  new int[] {6, 9, 15}),
            entry("R106",  new int[] {5, 8, 13}),
            entry("R107",  new int[] {4, 7, 11}),
            entry("R108",  new int[] {4, 6, 10}),
            entry("R109",  new int[] {5, 8, 13}),
            entry("R110",  new int[] {4, 7, 12}), // NOTE or {5, 7, 12};
            entry("R111",  new int[] {5, 7, 12}),
            entry("R112",  new int[] {4, 6, 10}),
            entry("C101",  new int[] {3, 5, 10}),
            entry("C102",  new int[] {3, 5, 10}),
            entry("C103",  new int[] {3, 5, 10}),
            entry("C104",  new int[] {3, 5, 10}),
            entry("C105",  new int[] {3, 5, 10}),
            entry("C106",  new int[] {3, 5, 10}),
            entry("C107",  new int[] {3, 5, 10}),
            entry("C108",  new int[] {3, 5, 10}),
            entry("C109",  new int[] {3, 5, 10}),
            entry("RC101", new int[] {4, 8, 15}),
            entry("RC102", new int[] {3, 7, 14}),
            entry("RC103", new int[] {3, 6, 11}),
            entry("RC104", new int[] {3, 5, 10}),
            entry("RC105", new int[] {4, 8, 15}),
            entry("RC106", new int[] {3, 6, 13}),
            entry("RC107", new int[] {3, 6, 12}),
            entry("RC108", new int[] {3, 6, 11})
    );
    
    public static Map<String, Double> optimalHospital = Map.ofEntries(
    		// I040 - B1 - F6
    		entry("i040_b1_f6_v01_m", 56.0),
    		entry("i040_b1_f6_v01_e", 71.0),
    		entry("i040_b1_f6_v02_m", 62.0),
    		entry("i040_b1_f6_v02_e", 49.0),
    		entry("i040_b1_f6_v03_m", 70.0),
    		entry("i040_b1_f6_v03_e", 47.0),
    		entry("i040_b1_f6_v04_m", 60.0),
    		entry("i040_b1_f6_v04_e", 49.0),
    		entry("i040_b1_f6_v05_m", 51.0),
    		entry("i040_b1_f6_v05_e", 62.0),
    		// I040 - B2 - F3
    		entry("i040_b2_f3_v01_m", 50.0),
    		entry("i040_b2_f3_v01_e", 41.0),
    		entry("i040_b2_f3_v02_m", 44.0),
    		entry("i040_b2_f3_v02_e", 57.0),
    		entry("i040_b2_f3_v03_m", 53.0),
    		entry("i040_b2_f3_v03_e", 71.0),
    		entry("i040_b2_f3_v04_m", 44.0),
    		entry("i040_b2_f3_v04_e", 51.0),
    		entry("i040_b2_f3_v05_m", 53.0),
    		entry("i040_b2_f3_v05_e", 59.0),
    		// I040 - B6 - F1
    		entry("i040_b6_f1_v01_m", 77.0),
    		entry("i040_b6_f1_v01_e", 44.0),
    		entry("i040_b6_f1_v02_m", 54.0),
    		entry("i040_b6_f1_v02_e", 34.0),
    		entry("i040_b6_f1_v03_m", 59.0),
    		entry("i040_b6_f1_v03_e", 53.0),
    		entry("i040_b6_f1_v04_m", 53.0),
    		entry("i040_b6_f1_v04_e", 46.0),
    		entry("i040_b6_f1_v05_m", 39.0),
    		entry("i040_b6_f1_v05_e", 45.0),
    		//
    		//
    		// I080 - B1 - F6
    		entry("i080_b1_f6_v01_m", 79.0),
    		entry("i080_b1_f6_v01_e", 91.0),
    		entry("i080_b1_f6_v02_m", 86.0),
    		entry("i080_b1_f6_v02_e", 78.0),
    		entry("i080_b1_f6_v03_m", 88.0),
    		entry("i080_b1_f6_v03_e", 96.0),
    		entry("i080_b1_f6_v04_m", 75.0),
    		entry("i080_b1_f6_v04_e", 96.0),
    		entry("i080_b1_f6_v05_m", 94.0),
    		entry("i080_b1_f6_v05_e", 74.0),
    		// I080 - B2 - F3
    		entry("i080_b2_f3_v01_m", 89.0),
    		entry("i080_b2_f3_v01_e", 80.0),
    		entry("i080_b2_f3_v02_m", 92.0),
    		entry("i080_b2_f3_v02_e", 79.0),
    		entry("i080_b2_f3_v03_m", 92.0),
    		entry("i080_b2_f3_v03_e", 108.0),
    		entry("i080_b2_f3_v04_m", 70.0),
    		entry("i080_b2_f3_v04_e", 102.0),
    		entry("i080_b2_f3_v05_m", 109.0),
    		entry("i080_b2_f3_v05_e", 96.0),  // LB 94.57
    		// I080 - B6 - F1
    		entry("i080_b6_f1_v01_m", 78.0),
    		entry("i080_b6_f1_v01_e", 92.0),
    		entry("i080_b6_f1_v02_m", 59.0),
    		entry("i080_b6_f1_v02_e", 108.0),
    		entry("i080_b6_f1_v03_m", 84.0),
    		entry("i080_b6_f1_v03_e", 84.0),
    		entry("i080_b6_f1_v04_m", 79.0),
    		entry("i080_b6_f1_v04_e", 99.0),
    		entry("i080_b6_f1_v05_m", 77.0),
    		entry("i080_b6_f1_v05_e", 93.0),
    		//
    		//
    		// I120 - B1 - F6
    		entry("i120_b1_f6_v01_m", 124.0),
    		entry("i120_b1_f6_v01_e", 128.0),
    		entry("i120_b1_f6_v02_m", 110.0),
    		entry("i120_b1_f6_v02_e", 118.0),
    		entry("i120_b1_f6_v03_m", 105.0),
    		entry("i120_b1_f6_v03_e", 153.0),
    		entry("i120_b1_f6_v04_m", 113.0),
    		entry("i120_b1_f6_v04_e", 121.98),
    		entry("i120_b1_f6_v05_m", 120.0),
    		entry("i120_b1_f6_v05_e", 134.0),
    		// I120 - B2 - F3
    		entry("i120_b2_f3_v01_m", 126.0),
    		entry("i120_b2_f3_v01_e", 140.0),
    		entry("i120_b2_f3_v02_m", 108.0),
    		entry("i120_b2_f3_v02_e", 100.0),
    		entry("i120_b2_f3_v03_m", 115.0),
    		entry("i120_b2_f3_v03_e", 96.0),
    		entry("i120_b2_f3_v04_m", 113.0),
    		entry("i120_b2_f3_v04_e", 135.0),
    		entry("i120_b2_f3_v05_m", 120.0),
    		entry("i120_b2_f3_v05_e", 111.0), // LB 109.10
    		// I120 - B6 - F1
    		entry("i120_b6_f1_v01_m", 111.0),
    		entry("i120_b6_f1_v01_e", 128.0),
    		entry("i120_b6_f1_v02_m", 119.0),
    		entry("i120_b6_f1_v02_e", 95.0),
    		entry("i120_b6_f1_v03_m", 126.0),
    		entry("i120_b6_f1_v03_e", 103.0),
    		entry("i120_b6_f1_v04_m", 106.0),
    		entry("i120_b6_f1_v04_e", 117.0),
    		entry("i120_b6_f1_v05_m", 143.0),
    		entry("i120_b6_f1_v05_e", 119.0)
    );
}
