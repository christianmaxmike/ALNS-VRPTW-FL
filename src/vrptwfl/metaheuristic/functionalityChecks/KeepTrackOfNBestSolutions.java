package vrptwfl.metaheuristic.functionalityChecks;

import com.google.common.base.Objects;

import java.util.Comparator;
import java.util.TreeSet;

public class KeepTrackOfNBestSolutions {


    private void testHashSet() {
        @SuppressWarnings({ "unchecked", "rawtypes" })
		TreeSet<TestSolution> set = new TreeSet(new Comparator() {
            @Override
            public int compare(Object o, Object t1) {
                return 0;
            }
        });
        set.add(new TestSolution(0, 4.0));
        set.add(new TestSolution(1, -2.0));
        set.add(new TestSolution(2, 2.0));
        set.add(new TestSolution(3, 1.0));
        set.add(new TestSolution(4, 10.0));
        set.add(new TestSolution(5, 11.0));
        set.add(new TestSolution(6, -1.0));


        for (TestSolution s : set) {
            System.out.println(s.toString());
        }
        System.out.println(set.size());
    }


    class TestSolution {
    	
        private int id;
        private double value;
    	
        TestSolution(int id, double value) {
            this.id = id;
            this.value = value;
        }

        public int getId() {
            return id;
        }

        public double getValue() {
        	return value;
        }
        
        public void setId(int id) {
            this.id = id;
        }

        public void setValue(double value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestSolution)) return false;
            TestSolution that = (TestSolution) o;
            return id == that.id &&
                    Double.compare(that.value, value) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id, value);
        }

        public String toString() {
            return "id: " + id + ", value: " + value;
        }

        public int compare(TestSolution o) {
            return Double.compare(this.value, o.value);
        }
    }
    
    
    // TODO
    //  store best n solutions
    //  assign hash key to solutions
    //  store hash key in hash table
    public static void main(String[] args) {
        //Creating HashSet and adding elements
//        HashSet<String> set = new HashSet();
//        set.add("One");
//        set.add("Two");
//        set.add("Three");
//        set.add("Four");
//        set.add("Five");
//        for (String s : set) {
//            System.out.println(s);
//        }
//        System.out.println(set.size());

        KeepTrackOfNBestSolutions main = new KeepTrackOfNBestSolutions();
        main.testHashSet();
    }
}