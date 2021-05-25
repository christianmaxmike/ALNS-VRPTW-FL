package vrptwfl.metaheuristic.functionalityChecks;

// Code based on example presented on Udemy: "Practical Data Structures & Algorithms in Java"

public class EmployHashTables {

    String[] hashArray; // underlying container
    int arraySize; // number of slots available in hash table
    int size = 0; // counter for number of elements in hash table

    // numberOfSlots should roughly be at least twice the number of elements we are expecting
    public EmployHashTables(int numberOfSlots) {
        // numberOfSlots should be prime number
        if (isPrime(numberOfSlots)) {
            this.hashArray = new String[numberOfSlots]; // initialize underlying container
            this.arraySize = numberOfSlots;
        } else {
            int primeCount = getNextPrime(numberOfSlots);
            this.hashArray = new String[primeCount];
            this.arraySize = primeCount;
            System.out.println("Hash table size given " + numberOfSlots + " is not a prime number.");
            System.out.println("Hash table size changed to " + primeCount);
        }
    }

    private boolean isPrime(int num) {
        for (int i = 2; i*i <= num; i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }

    private int getNextPrime(int minNumber) {
        for (int i = minNumber; true; i++) {
            if(isPrime(i)) {
                return i;
            }
        }
    }

    // returns preferred index position (double hash table)
    private int hashFunc1(String word) {
        int hashVal = word.hashCode();
        hashVal %= arraySize;
        if (hashVal < 0)hashVal += arraySize;

        return  hashVal; // ideal index position we'd like to insert or search in
    }

    // return step size greater than 0 (double hash table)
    private int hashFunc2(String word) {
        int hashVal = word.hashCode();
        hashVal %= arraySize;
        if (hashVal < 0)hashVal += arraySize;

        int primeLessThanArraySize = 3; // could checks that number is actually less than array size

        return  primeLessThanArraySize - hashVal % primeLessThanArraySize; // ideal index position we'd like to insert or search in
    }

    public void insertWord(String word) {
        int preferredIndexPosition = hashFunc1(word);
        int stepSize = hashFunc2(word);

        while (hashArray[preferredIndexPosition] != null) {
            preferredIndexPosition = preferredIndexPosition + stepSize;
            preferredIndexPosition %= arraySize;
        }

        hashArray[preferredIndexPosition] = word;
        System.out.println("Inserted word: " + word);
        size++;
    }

    public String find(String word) {
        int preferredIndexPosition = hashFunc1(word);
        int stepSize = hashFunc2(word);

        while (hashArray[preferredIndexPosition] != null) {

            // if there is a value, check if that's the word we are searching for
            if (hashArray[preferredIndexPosition].equals(word)) {
                return hashArray[preferredIndexPosition];
            } else {
                preferredIndexPosition = preferredIndexPosition + stepSize;
                preferredIndexPosition %= arraySize;
            }
        }
        // if we leave while loop, we were not able to find what we were looking for
        // so, we just return the hashValue at the position which is probably null
        return hashArray[preferredIndexPosition];
    }


    // check if code is working
    public static void main(String[] args) {
        EmployHashTables table = new EmployHashTables(25);
        table.insertWord("Apple");
        table.insertWord("Hello");
        table.insertWord("Feeling");
        table.insertWord("Water");
        table.insertWord("Africa");
        table.insertWord("Speed");
        table.insertWord("Phone");
        table.insertWord("September");
        table.insertWord("Micheal");
        table.insertWord("Milk");
        table.insertWord("Huge");
        table.insertWord("Dogs");

        System.out.println("------FIND ELEMENTS------");
        System.out.println(table.find("Apple"));
        System.out.println(table.find("Zebra"));
        System.out.println(table.find("Feeling"));
        System.out.println(table.find("Water"));
        System.out.println(table.find("Africa"));
        System.out.println(table.find("Feeling"));

        table.displayTable();
    }

    public void displayTable() {
        System.out.println("\nTable: ");
        for (int i = 0; i < arraySize; i++) {
            if (hashArray[i] != null) {
                System.out.print(hashArray[i] + " ");
            } else {
                System.out.print("** ");
            }
            System.out.println("");
        }
    }
}
