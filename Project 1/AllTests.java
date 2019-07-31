
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.rules.Timeout;
import org.junit.runner.Description;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertTrue;

public class AllTests {

    // general functionality
    static final int totalPoints = 380; //not including efficiency, which will be added later

    static int p = 9001;
    static int partial = 0;
    static int totalEarned = 0;
    static int totalLost = 0;
    static StringBuilder builder = new StringBuilder();

    // test-specific data
    //static long shortestPathIts = effVerticesCount * effVerticesCount / 16;
    //static long influenceIts = effVerticesCount / 20;

    // correctness data for efficiency calculations
    static int addCorrectness = 0;
    static int extractCorrectness = 0;
    static int removeCorrectness = 0;
    static int decrementCorrectness = 0;

    static final int addCorrectnessTot = 20;
    static final int extractCorrectnessTot = 20;
    static final int removeCorrectnessTot = 20;
    static final int decrementCorrectnessTot = 20;

    // timing data
    static final int SEED = 23;
    static final int TEST_NUM = 5;
    static final int TEST_SIZE = 500_000;


    static long addRefTime = Long.MAX_VALUE;
    static long extractRefTime = Long.MAX_VALUE;
    static long removeRefTime = Long.MAX_VALUE;
    static long decrementRefTime = Long.MAX_VALUE;

    static long addUserTime = Long.MAX_VALUE;
    static long extractUserTime = Long.MAX_VALUE;
    static long removeUserTime = Long.MAX_VALUE;
    static long decrementUserTime = Long.MAX_VALUE;

    // crawler stuff
    static List<Pair<String, String>>[] crawlerEdgesUnfocused;
    static List<Pair<String, String>>[] crawlerEdgesFocused;
    static String BASEURL = "http://web.cs.iastate.edu/~pavan";
    static String CRAWLERSEEDS_UNFOCUSED[] = {
            "/wiki/A.html",
            "/wiki/A2.html",
            "/wiki/A1.html",
            "/wiki/D.html",
            "/wiki/D1.html",
            "/wiki/D2.html",
            "/wiki/G.html",
            "/wiki/G1.html",
            "/wiki/G2.html",
            "/wiki/J.html",
            "/wiki/AA.html",
            "/wiki/L.html",
            "/wiki/L1.html",
            "/wiki/L2.html",
            "/wiki/L3.html",
            "/wiki/L.html",
            "/wiki/L1.html"
    };
    static String CRAWLERSEEDS_FOCUSED[] = {
            "/wiki/pA.html",
            "/wiki/pF.html",
            "/wiki/pK.html",
            "/wiki/pQ.html",
            "/wiki/pQ1.html",
            "/wiki/pQ1.html",
            "/wiki/pA2.html",
            "/wiki/pF2.html",
            "/wiki/pK2.html",
            "/wiki/pQ2.html",
            "/wiki/pQ3.html",
            "/wiki/pQ3.html",
            "/wiki/pAA.html",
            "/wiki/pEE.html",
            "/wiki/pWW.html"
    };
    static String[][] topics = {
            new String[] {"cat", "dog"},
            new String[] {"cat", "dog"},
            new String[] {"cat", "dog"},
            new String[] {"dog"},
            new String[] {"dog"},
            new String[] {"cat"},
            new String[] {"cat", "dog"},
            new String[] {"cat", "dog"},
            new String[] {"cat", "dog"},
            new String[] { "dog"},
            new String[] { "dog"},
            new String[] {"cat"},
            new String[] { "dog"},
            new String[] {"cat", "dog", "mouse"},
            new String[] {"dog"},
    };
    static final String OUTPUTFILE = "temp.txt";

    // pq stuff
    private static PQStartIndex userStartIndex;

    @Rule
    public Timeout timeout = Timeout.seconds(60);

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {

            if (p > totalPoints) {
                System.out.println("ERROR: POINT VALUES NOT RESET!"); // this ensures that we don't miss resetting point values
            }

            totalEarned += p;
            p = 9000;
        }

        @Override
        protected void failed(Throwable e, Description description) {

            if (p > totalPoints) {
                System.out.println("ERROR: POINT VALUES NOT RESET!"); // this ensures that we don't miss resetting point values
            }

            if (partial > 0) {
                builder.append("test " + description + " partially failed with exception " + e + " (-" + (p - partial) + ");\n");

                totalLost += p - partial;
                totalEarned += partial;
                partial = 0;
            } else {
                builder.append("test " + description + " failed with exception " + e + " (-" + p + "); \n");
                totalLost += p;
            }

            p = 9000;
        }
    };

    @AfterClass
    public static void printResults() {
        if (totalEarned + totalLost != totalPoints) {
            System.out.println("\n\nERROR!  Earned " + totalEarned + " but lost " + totalLost + "; should add to " + totalPoints);
            System.out.println("missing " + (totalPoints - totalEarned - totalLost));
        }

        float addRatio = (float) addCorrectness / (float) addCorrectnessTot;
        float extractRatio = (float) extractCorrectness / (float) extractCorrectnessTot;
        float removeRatio = (float) removeCorrectness / (float) removeCorrectnessTot;
        float decrementRatio = (float) decrementCorrectness / (float) decrementCorrectnessTot;

        float addEff = 30f * addRatio * Math.min(1f, 1.5f * (float) addRefTime / (float) addUserTime);
        float extractEff = 30f * extractRatio * Math.min(1f, 1.5f * (float) extractRefTime / (float) extractUserTime);
        float removeEff = 30f * removeRatio * Math.min(1f, 1.5f * (float) removeRefTime / (float) removeUserTime);
        float decrementEff = 30f * decrementRatio * Math.min(1f, 1.5f * (float) decrementRefTime / (float) decrementUserTime);

        float pointsEff = addEff + extractEff + removeEff + decrementEff;
        float pointsCorr = totalEarned;

        int total = (int) Math.ceil(pointsEff + pointsCorr);


        System.out.println("\n================================================================");
        System.out.println(" comments below ");
        System.out.println("================================================================");
        System.out.println("[total points: " + Math.min(total, 500) + "/500]");
        System.out.println("");
        System.out.println("[correctness: " + pointsCorr + "/380; correctness-scaled efficiency: " + pointsEff + "/120]");
        System.out.println("[correctness deductions:");
        System.out.println(builder.toString());
        System.out.println("]");
        System.out.println("");
        System.out.println("[We calculated several efficiency categories; for each, we scaled by correctness and compared student runtime to a reference implementation:");
        System.out.println("efficiencyPoints = pointsPossible * correctnessPoints / correctnessPointsPossible * min(1, 1.5 * referenceTime / studentTime).");
        System.out.println("(Units are nanoseconds)]");
        System.out.println("[add (" + addEff + "/30): " + addUserTime + " student, " + addRefTime + " ref]");
        System.out.println("[extract (" + extractEff + "/30): " + extractUserTime + " student, " + extractRefTime + " ref]");
        System.out.println("[remove (" + removeEff + "/30): " + removeUserTime + " student, " + removeRefTime + " ref]");
        System.out.println("[decrement (" + decrementEff + "/30): " + decrementUserTime + " student, " + decrementRefTime + " ref]");
        System.out.println("================================================================");

    }

    // ================================================================
    // SETUP
    // ================================================================
    @BeforeClass
    public static void setUp() throws Exception {
        boolean success = true;

        // Init wiki crawler solutions.
        try {
            crawlerEdgesUnfocused = new List[17];
            for (int i = 1; i <= 15; i++) {
                int max = 3;
                if (i == 15) max = 4;
                String path = "./data/graph" + i + "-" + "max" + max + "-notopics.txt";
                crawlerEdgesUnfocused[i - 1] = parseGraph(path);
            }
            String path = "./data/graph" + 12 + "-" + "max" + 4 + "-2topics.txt";
            crawlerEdgesUnfocused[15] = parseGraph(path);
            path = "./data/graph" + 13 + "-" + "max" + 4 + "-2topics.txt";
            crawlerEdgesUnfocused[16] = parseGraph(path);
        } catch (Exception e) {
            success = false;
        }
        try {
            crawlerEdgesFocused = new List[15];
            String path = "./data/graph" + 1 + "-" + "max" + 4 + "-topics.txt";
            crawlerEdgesFocused[0] = parseGraph(path);
            path = "./data/graph" + 2 + "-" + "max" + 4 + "-topics.txt";
            crawlerEdgesFocused[1] = parseGraph(path);
            path = "./data/graph" + 3 + "-" + "max" + 3 + "-topics.txt";
            crawlerEdgesFocused[2] = parseGraph(path);
            path = "./data/graph" + 4 + "-" + "max" + 5 + "-topics.txt";
            crawlerEdgesFocused[3] = parseGraph(path);
            path = "./data/graph" + 5 + "-" + "max" + 5 + "-topics.txt";
            crawlerEdgesFocused[4] = parseGraph(path);
            path = "./data/graph" + 6 + "-" + "max" + 5 + "-topics.txt";
            crawlerEdgesFocused[5] = parseGraph(path);
            path = "./data/graph" + 7 + "-" + "max" + 4 + "-topics.txt";
            crawlerEdgesFocused[6] = parseGraph(path);
            path = "./data/graph" + 8 + "-" + "max" + 4 + "-topics.txt";
            crawlerEdgesFocused[7] = parseGraph(path);
            path = "./data/graph" + 9 + "-" + "max" + 3 + "-topics.txt";
            crawlerEdgesFocused[8] = parseGraph(path);
            path = "./data/graph" + 10 + "-" + "max" + 5 + "-topics.txt";
            crawlerEdgesFocused[9] = parseGraph(path);
            path = "./data/graph" + 11 + "-" + "max" + 5 + "-topics.txt";
            crawlerEdgesFocused[10] = parseGraph(path);
            path = "./data/graph" + 12 + "-" + "max" + 5 + "-topics.txt";
            crawlerEdgesFocused[11] = parseGraph(path);
            path = "./data/graph" + 13 + "-" + "max" + 4 + "-topics.txt";
            crawlerEdgesFocused[12] = parseGraph(path);
            path = "./data/graph" + 14 + "-" + "max" + 8 + "-topics.txt";
            crawlerEdgesFocused[13] = parseGraph(path);
            path = "./data/graph" + 15 + "-" + "max" + 3 + "-topics.txt";
            crawlerEdgesFocused[14] = parseGraph(path);
        } catch (Exception e) {
            success = false;
        }

        if (!success) {
            System.out.println("WARNING: graph setup failed for at least one test graph");
        }

        // Init pq settings.
        try {
            userStartIndex = findStartIndex(new PriorityQ());
        } catch (Exception ex) {
            System.out.println();
        }

        // Check times.
        long t = (45L * Math.max(addRefTime, Math.max(decrementRefTime, Math.max(extractRefTime, removeRefTime)))) / 1_000_000_000L;
    }

    public static boolean strListEquals(List<String> a, List<String> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!(a.get(i).equals(b.get(i)))) return false;
        }
        return true;
    }

    boolean setEquality(ArrayList<String> a, ArrayList<String> b) {
        return a.containsAll(b) && b.containsAll(a);
    }

    // ================================================================
    // PriorityQ tests
    // ================================================================

    @Test
    public void addTest1() throws Exception {
        p = 6;

        PriorityQ pq = new PriorityQ();

        pq.add("the", 3);
        pq.add("quick", 5);

        Assert.assertEquals(5, pq.getKey(zeroIndexToUserIndex(0)));
        Assert.assertEquals(3, pq.getKey(zeroIndexToUserIndex(1)));
        Assert.assertEquals("quick", pq.getValue(zeroIndexToUserIndex(0)));
        Assert.assertEquals("the", pq.getValue(zeroIndexToUserIndex(1)));

        addCorrectness += p;
    }

    @Test
    public void addTest2() throws Exception {
        p = 6;

        PriorityQ pq = new PriorityQ();

        pq.add("the", 3);
        pq.add("quick", 5);
        pq.add("jumped", 6);
        pq.add("a", 1);

        Assert.assertEquals(6, pq.getKey(zeroIndexToUserIndex(0)));
        Assert.assertEquals("jumped", pq.getValue(zeroIndexToUserIndex(0)));

        Assert.assertTrue(isBinHeapValid(getPriorityArrayManually(pq, 4)));

        addCorrectness += p;
    }

    @Test
    public void addTest3() throws Exception {
        p = 8;

        int[] contents = new int[]{1, 8, 2, 7, 3, 6, 4, 5};

        PriorityQ pq = new PriorityQ();

        for (int i : contents)
            pq.add(Integer.toString(i), i);

        Assert.assertEquals(8, pq.getKey(zeroIndexToUserIndex(0)));
        Assert.assertEquals(Integer.toString(8), pq.getValue(zeroIndexToUserIndex(0)));

        Assert.assertTrue(isBinHeapValid(getPriorityArrayManually(pq, 8)));

        addCorrectness += p;
    }

    @Test
    public void returnTest1() throws Exception {
        p = 6;

        PriorityQ pq = new PriorityQ();

        pq.add("the", 3);
        pq.add("quick", 5);

        Assert.assertEquals("quick", pq.returnMax());
    }

    @Test
    public void returnTest2() throws Exception {
        p = 6;

        PriorityQ pq = new PriorityQ();

        pq.add("the", 3);
        pq.add("quick", 5);
        pq.add("jumped", 6);
        pq.add("a", 1);

        Assert.assertEquals("jumped", pq.returnMax());
    }

    @Test
    public void returnTest3() throws Exception {
        p = 8;

        int[] contents = new int[]{1, 8, 2, 7, 3, 6, 4, 5};

        PriorityQ pq = new PriorityQ();

        for (int i : contents)
            pq.add(Integer.toString(i), i);

        Assert.assertEquals(Integer.toString(8), pq.returnMax());
    }

    @Test
    public void extractTest1() throws Exception {
        p = 6;

        PriorityQ pq = new PriorityQ();

        pq.add("the", 3);
        pq.add("quick", 5);

        Assert.assertEquals("quick", pq.extractMax());
        Assert.assertEquals(3, pq.getKey(zeroIndexToUserIndex(0)));
        Assert.assertEquals("the", pq.getValue(zeroIndexToUserIndex(0)));

        extractCorrectness += p;
    }

    @Test
    public void extractTest2() throws Exception {
        p = 6;

        PriorityQ pq = new PriorityQ();

        pq.add("the", 3);
        pq.add("quick", 5);
        pq.add("jumped", 6);
        pq.add("a", 1);

        Assert.assertEquals("jumped", pq.extractMax());
        Assert.assertEquals(5, pq.getKey(zeroIndexToUserIndex(0)));
        Assert.assertEquals("quick", pq.getValue(zeroIndexToUserIndex(0)));
        Assert.assertTrue(isBinHeapValid(getPriorityArrayManually(pq, 3)));

        extractCorrectness += p;
    }

    @Test
    public void extractTest3() throws Exception {
        p = 8;

        int[] contents = new int[]{1, 8, 2, 7, 3, 6, 4, 5};

        PriorityQ pq = new PriorityQ();

        for (int i : contents)
            pq.add(Integer.toString(i), i);

        Assert.assertEquals(Integer.toString(8), pq.extractMax());
        Assert.assertEquals(7, pq.getKey(zeroIndexToUserIndex(0)));
        Assert.assertEquals(Integer.toString(7), pq.getValue(zeroIndexToUserIndex(0)));
        Assert.assertTrue(isBinHeapValid(getPriorityArrayManually(pq, 7)));

        extractCorrectness += p;
    }

    @Test
    public void removeTest1() throws Exception {
        p = 6;

        PriorityQ pq = new PriorityQ();

        pq.add("the", 3);
        pq.add("quick", 5);
        pq.remove(zeroIndexToUserIndex(1));

        Assert.assertEquals(5, pq.getKey(zeroIndexToUserIndex(0)));
        Assert.assertEquals("quick", pq.getValue(zeroIndexToUserIndex(0)));

        removeCorrectness += p;
    }

    @Test
    public void removeTest2() throws Exception {
        p = 6;

        PriorityQ pq = new PriorityQ();

        pq.add("the", 3);
        pq.add("quick", 5);
        pq.add("jumped", 6);
        pq.add("a", 1);
        pq.remove(zeroIndexToUserIndex(0));

        Assert.assertEquals(5, pq.getKey(zeroIndexToUserIndex(0)));
        Assert.assertEquals("quick", pq.getValue(zeroIndexToUserIndex(0)));
        Assert.assertTrue(isBinHeapValid(getPriorityArrayManually(pq, 3)));

        removeCorrectness += p;
    }

    @Test
    public void removeTest3() throws Exception {
        p = 8;

        int[] contents = new int[]{1, 8, 2, 7, 3, 6, 4, 5};

        PriorityQ pq = new PriorityQ();

        for (int i : contents)
            pq.add(Integer.toString(i), i);

        pq.remove(zeroIndexToUserIndex(0));
        pq.remove(zeroIndexToUserIndex(0));
        pq.remove(zeroIndexToUserIndex(5));

        Assert.assertEquals(6, pq.getKey(zeroIndexToUserIndex(0)));
        Assert.assertEquals(Integer.toString(6), pq.getValue(zeroIndexToUserIndex(0)));
        Assert.assertTrue(isBinHeapValid(getPriorityArrayManually(pq, 5)));

        removeCorrectness += p;
    }

    @Test
    public void decrementTest1() throws Exception {
        p = 6;

        PriorityQ pq = new PriorityQ();

        pq.add("the", 3);
        pq.add("quick", 5);
        pq.decrementPriority(zeroIndexToUserIndex(0), 3);

        Assert.assertEquals("the", pq.getValue(zeroIndexToUserIndex(0)));
        Assert.assertEquals(3, pq.getKey(zeroIndexToUserIndex(0)));
        Assert.assertEquals("quick", pq.getValue(zeroIndexToUserIndex(1)));
        Assert.assertEquals(2, pq.getKey(zeroIndexToUserIndex(1)));

        decrementCorrectness += p;
    }

    @Test
    public void decrementTest2() throws Exception {
        p = 6;

        PriorityQ pq = new PriorityQ();

        pq.add("the", 3);
        pq.add("quick", 5);
        pq.add("jumped", 6);
        pq.add("a", 1);
        pq.decrementPriority(zeroIndexToUserIndex(0), 4);
        pq.decrementPriority(zeroIndexToUserIndex(0), 1);

        Assert.assertEquals(4, pq.getKey(zeroIndexToUserIndex(0)));
        Assert.assertEquals("quick", pq.getValue(zeroIndexToUserIndex(0)));
        Assert.assertTrue(isBinHeapValid(getPriorityArrayManually(pq, 4)));

        decrementCorrectness += p;
    }

    @Test
    public void decrementTest3() throws Exception {
        p = 8;

        int[] contents = new int[]{1, 8, 2, 7, 3, 6, 4, 5};

        PriorityQ pq = new PriorityQ();

        for (int i : contents)
            pq.add(Integer.toString(i), i);

        pq.decrementPriority(zeroIndexToUserIndex(3), 1);
        pq.decrementPriority(zeroIndexToUserIndex(3), 1);
        pq.decrementPriority(zeroIndexToUserIndex(1), 2);
        pq.decrementPriority(zeroIndexToUserIndex(1), 2);
        pq.decrementPriority(zeroIndexToUserIndex(0), 1);

        Assert.assertEquals(7, pq.getKey(zeroIndexToUserIndex(0)));
        Assert.assertEquals(Integer.toString(8), pq.getValue(zeroIndexToUserIndex(0)));
        Assert.assertTrue(isBinHeapValid(getPriorityArrayManually(pq, contents.length)));

        decrementCorrectness += p;
    }

    @Test
    public void priorityArrayTest1() throws Exception {
        p = 6;

        PriorityQ pq = new PriorityQ();

        pq.add("the", 3);
        pq.add("quick", 5);
        pq.add("jumped", 6);
        pq.add("a", 1);

        int[] pa = pq.priorityArray();
        int[] pa2 = getPriorityArrayManually(pq, 4);

        Assert.assertTrue(isBinHeapValid(pa));
        Assert.assertTrue(Arrays.equals(pa, pa2));
    }

    @Test
    public void priorityArrayTest2() throws Exception {
        p = 6;

        int[] contents = new int[]{1, 8, 2, 7, 3, 6, 4, 5};

        PriorityQ pq = new PriorityQ();

        for (int i : contents)
            pq.add(Integer.toString(i), i);

        int[] pa = pq.priorityArray();

        Assert.assertTrue(isBinHeapValid(pa));
        Assert.assertTrue(Arrays.equals(pa, getPriorityArrayManually(pq, 8)));
    }

    @Test
    public void priorityArrayTest3() throws Exception {
        p = 8;

        int[] contents = new int[]
                {
                        1, 8, 2, 7, 3, 6, 4, 5,
                        100, 200, 110, 190, 50, 1234, 555, 23
                };

        PriorityQ pq = new PriorityQ();

        for (int i : contents)
            pq.add(Integer.toString(i), i);

        int[] pa = pq.priorityArray();

        Assert.assertTrue(isBinHeapValid(pa));
        Assert.assertTrue(Arrays.equals(pa, getPriorityArrayManually(pq, 16)));
    }

    @Test
    public void getAddTime() throws Exception {
        p = 0;

        addUserTime = timeUserAdd();
    }

    @Test
    public void getExtractTime() throws Exception {
        p = 0;

        extractUserTime = timeUserExtract();
    }

    @Test
    public void getRemoveTime() throws Exception {
        p = 0;

        removeUserTime = timeUserRemove();
    }

    @Test
    public void getDecrementTime() throws Exception {
        p = 0;

        decrementUserTime = timeUserDecrement();
    }

    private long timeUserAdd() throws Exception {
        long start = System.nanoTime();

        for (int i = 0; i < TEST_NUM; i++) {
            PriorityQ pq = new PriorityQ();
            Random r = new Random(SEED);

            for (int j = 0; j < TEST_SIZE; j++) {
                int next = r.nextInt(Integer.MAX_VALUE);
                pq.add(Integer.toString(next), next);
            }
        }

        long end = System.nanoTime();

        return end - start;
    }

    private long timeUserRemove() throws Exception {
        long accum = 0;

        for (int i = 0; i < TEST_NUM; i++) {
            long constructorStart = System.nanoTime();
            PriorityQ pq = new PriorityQ();
            long constructorEnd = System.nanoTime();
            accum += constructorEnd - constructorStart;

            Random r = new Random(SEED);

            for (int j = 0; j < TEST_SIZE; j++) {
                int next = r.nextInt(Integer.MAX_VALUE);
                pq.add(Integer.toString(next), next);
            }

            long start = System.nanoTime();

            for (int j = 0; j < TEST_SIZE; j++) {
                int next = r.nextInt(Integer.MAX_VALUE);
                pq.remove(zeroIndexToUserIndex(next % (TEST_SIZE - j)));
            }

            long end = System.nanoTime();
            accum += (end - start);
        }

        return accum / TEST_NUM;
    }

    private long timeUserExtract() throws Exception {
        long accum = 0;

        for (int i = 0; i < TEST_NUM; i++) {
            long constructorStart = System.nanoTime();
            PriorityQ pq = new PriorityQ();
            long constructorEnd = System.nanoTime();
            accum += constructorEnd - constructorStart;

            Random r = new Random(SEED);

            for (int j = 0; j < TEST_SIZE; j++) {
                int next = r.nextInt(Integer.MAX_VALUE);
                pq.add(Integer.toString(next), next);
            }

            long start = System.nanoTime();

            for (int j = 0; j < TEST_SIZE; j++) {
                pq.extractMax();
            }

            long end = System.nanoTime();
            accum += (end - start);
        }

        return accum / TEST_NUM;
    }

    private long timeUserDecrement() throws Exception {
        long accum = 0;

        for (int i = 0; i < TEST_NUM; i++) {
            long constructorStart = System.nanoTime();
            PriorityQ pq = new PriorityQ();
            long constructorEnd = System.nanoTime();
            accum += constructorEnd - constructorStart;

            Random r = new Random(SEED);

            for (int j = 0; j < TEST_SIZE; j++) {
                int next = j + 100_000_000;
                pq.add(Integer.toString(next), next);
            }

            long start = System.nanoTime();

            for (int j = 0; j < TEST_SIZE; j++) {
                int nexti = r.nextInt(TEST_SIZE);
                int nextk = r.nextInt(100_000_000 / TEST_SIZE);
                pq.decrementPriority(zeroIndexToUserIndex(nexti), nextk);
            }

            long end = System.nanoTime();
            accum += (end - start);
        }

        return accum / TEST_NUM;
    }


    private int[] getPriorityArrayManually(PriorityQ pq, int count) throws Exception {
        int offset = 0;
        if (zeroIndexToUserIndex(0) == 1)
            offset = 1;

        int[] arr = new int[count + offset];
        for (int i = 0; i < count; i++)
            arr[i + offset] = pq.getKey(zeroIndexToUserIndex(i));
        return arr;
    }

    // ================================================================
    // WikiCrawler tests
    // ================================================================

    @Test
    public void crawlerTest1() throws Exception {
        p = 10;
        int testnum = 1;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 3, new String[0]);
    }

    @Test
    public void crawlerTest2() throws Exception {
        p = 10;
        int testnum = 2;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 3, new String[0]);
    }

    @Test
    public void crawlerTest3() throws Exception {
        p = 10;
        int testnum = 3;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 3, new String[0]);
    }

    @Test
    public void crawlerTest4() throws Exception {
        p = 10;
        int testnum = 4;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 3, new String[0]);
    }

    @Test
    public void crawlerTest5() throws Exception {
        p = 10;
        int testnum = 5;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 3, new String[0]);
    }

    @Test
    public void crawlerTest6() throws Exception {
        p = 10;
        int testnum = 6;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 3, new String[0]);
    }

    @Test
    public void crawlerTest7() throws Exception {
        p = 10;
        int testnum = 7;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 3, new String[0]);
    }

    @Test
    public void crawlerTest8() throws Exception {
        p = 10;
        int testnum = 8;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 3, new String[0]);
    }

    @Test
    public void crawlerTest9() throws Exception {
        p = 10;
        int testnum = 9;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 3, new String[0]);
    }

    /*
    @Test
    public void crawlerTest10() throws Exception {
        p = 6;
        int testnum = 10;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 3, new ArrayList<>(0));
    }

    @Test
    public void crawlerTest11() throws Exception {
        p = 6;
        int testnum = 11;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 3, new ArrayList<>(0));
    }
    */

    @Test
    public void crawlerTest12() throws Exception {
        p = 10;
        int testnum = 12;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 3, new String[0]);
    }

    @Test
    public void crawlerTest13() throws Exception {
        p = 10;
        int testnum = 13;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 3, new String[0]);
    }

    @Test
    public void crawlerTest14() throws Exception {
        p = 10;
        int testnum = 14;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 3, new String[0]);
    }

    @Test
    public void crawlerTest15() throws Exception {
        p = 10;
        int testnum = 15;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 4, new String[0]);
    }

    @Test
    public void crawlerTest16() throws Exception {
        p = 10;
        int testnum = 16;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 4, new String[]{"trent", "hugh"});
    }

    @Test
    public void crawlerTest17() throws Exception {
        p = 10;
        int testnum = 17;

        testCrawlerGraph(testnum, CRAWLERSEEDS_UNFOCUSED[testnum - 1], 4, new String[]{"trent", "hugh"});
    }

    @Test
    public void crawlerTestFocused1() throws Exception {
        p = 7;
        int testnum = 1;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 4, topics[testnum - 1], true);
    }

    @Test
    public void crawlerTestFocused2() throws Exception {
        p = 7;
        int testnum = 2;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 4, topics[testnum - 1], true);
    }

    @Test
    public void crawlerTestFocused3() throws Exception {
        p = 7;
        int testnum = 3;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 3, topics[testnum - 1], true);
    }

    @Test
    public void crawlerTestFocused4() throws Exception {
        p = 7;
        int testnum = 4;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 5, topics[ testnum - 1], true);
    }

    @Test
    public void crawlerTestFocused5() throws Exception {
        p = 7;
        int testnum = 5;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 5, topics[testnum - 1], true);
    }

    @Test
    public void crawlerTestFocused6() throws Exception {
        p = 7;
        int testnum = 6;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 5, topics[testnum - 1], true);
    }

    @Test
    public void crawlerTestFocused7() throws Exception {
        p = 7;
        int testnum = 7;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 4, topics[testnum - 1], true);
    }

    @Test
    public void crawlerTestFocused8() throws Exception {
        p = 7;
        int testnum = 8;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 4, topics[testnum - 1], true);
    }

    @Test
    public void crawlerTestFocused9() throws Exception {
        p = 7;
        int testnum = 9;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 3, topics[testnum - 1], true);
    }

    @Test
    public void crawlerTestFocused10() throws Exception {
        p = 7;
        int testnum = 10;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 5, topics[testnum - 1], true);
    }

    @Test
    public void crawlerTestFocused11() throws Exception {
        p = 8;
        int testnum = 11;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 5, topics[testnum - 1], true);
    }

    @Test
    public void crawlerTestFocused12() throws Exception {
        p = 8;
        int testnum = 12;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 5, topics[testnum - 1], true);
    }

    @Test
    public void crawlerTestFocused13() throws Exception {
        p = 8;
        int testnum = 13;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 4, topics[testnum - 1], true);
    }

    @Test
    public void crawlerTestFocused14() throws Exception {
        p = 8;
        int testnum = 14;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 8, topics[testnum - 1], true);
    }

    @Test
    public void crawlerTestFocused15() throws Exception {
        p = 8;
        int testnum = 15;

        testCrawlerGraphFocused(testnum, CRAWLERSEEDS_FOCUSED[testnum - 1], 3, topics[testnum - 1], true);
    }

    private void testCrawlerGraph(int testnum, String seed, int max, String[] topics) throws Exception {
        testCrawlerGraphFocused(testnum, seed, max, topics, false);
    }

    private void testCrawlerGraphFocused(int testnum, String seed, int max, String[] topics, boolean focused) throws Exception {
        Path path = Paths.get(OUTPUTFILE);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {

        }

        WikiCrawler wc = new WikiCrawler(seed, max, topics, OUTPUTFILE);
        wc.crawl(focused);
        List<Pair<String, String>> theirs = parseGraph(OUTPUTFILE);
        List<Pair<String, String>> ours = crawlerEdgesUnfocused[testnum - 1];
        if (focused)
            ours = crawlerEdgesFocused[testnum - 1];

        /*
        // DIAGNOSTICS
        theirs.sort((Pair<String,String> x, Pair<String,String> y) -> {if (x.a.equals(y.a)) return x.b.compareTo(y.b); else return x.a.compareTo(y.a);} );
        ours.sort((Pair<String,String> x, Pair<String,String> y) -> {if (x.a.equals(y.a)) return x.b.compareTo(y.b); else return x.a.compareTo(y.a);} );
        System.out.println("Ours\t - \tTheirs");
        for (int i = 0; i < Math.max(theirs.size(), ours.size()); i++) {
            if (i < ours.size())
                System.out.print(ours.get(i).a + " " + ours.get(i).b);
            System.out.print("\t-\t");
            if (i < theirs.size())
                System.out.print(theirs.get(i).a + " " + theirs.get(i).b);
            System.out.println();
        }
        //*/

        double js = jaccardSimilarity(ours, theirs);
        if (js < 1)
            partial = (int) Math.ceil((p * (js / 2)));
        double scale = getVertexCountScalingRatio(ours, theirs);
        if (scale != 1) {
            System.out.println("Their vertex count is incorrect. Scaling by " + scale);
            if (scale > 1)
                System.out.println("Something appears wrong with crawler graph vertex count scaling. Please investigate.");
            if (js < 1)
                partial = (int) Math.ceil(((float)partial) * scale);
            else
                partial = (int) Math.ceil(((float) p) * scale);
        }
        assertTrue("Incorrect output from wiki crawler test " + testnum + ".", js == 1);
    }

    private double getVertexCountScalingRatio(List<Pair<String, String>> a, List<Pair<String, String>> b) {
        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();
        for (Pair<String,String> pair :
             a) {
            set1.add(pair.a);
            set1.add(pair.b);
        }
        for (Pair<String,String> pair :
                b) {
            set2.add(pair.a);
            set2.add(pair.b);
        }

        if (set1.size() == 0 | set2.size() == 0)
            return 0.0;
        else
            return Math.min(((float) set1.size() / (float) set2.size()), ((float) set2.size() / (float) set1.size()));
    }

    //////////////
    // Helpers. //
    //////////////

    private enum PQStartIndex {Zero, One};

    private static PQStartIndex findStartIndex(PriorityQ pq) throws Exception {
        try {
            pq.add("dummy1", 2);
            pq.add("dummy2", 1);
            pq.remove(1);

            int[] arr = pq.priorityArray();

            if (arr[0] == 2)
                return PQStartIndex.Zero;
            else if (arr[1] == 1)
                return PQStartIndex.One;
            else
                System.out.println("");
        } catch (Exception ex) {
            System.out.println("");
        }

        return PQStartIndex.One;
    }

    private int zeroIndexToUserIndex(int i) {
        switch (userStartIndex) {
            case Zero:
                return i;
            case One:
                return i + 1;
        }

        System.out.println("Something strange happened... it should be investigated.");
        return i;
    }

    private boolean isBinHeapValid(int[] array) {
        switch (userStartIndex) {
            case Zero:
                return isBinHeapValid0(array);
            case One:
                return isBinHeapValid1(array);
        }

        System.out.println("Something strange happened... it should be investigated.");
        return false;
    }

    private boolean isBinHeapValid1(int[] array) {
        if (array == null) return false;
        if (array.length == 0) return true;

        // Check as if it starts at 1.
        boolean isHeap = true;
        for (int i = 1; i < array.length; i++) {
            int p = i;
            int lc = 2 * p;
            int rc = 2 * p + 1;
            if (lc < array.length)
                if (array[lc] > array[p])
                    isHeap = false;
            if (rc < array.length)
                if (array[rc] > array[p])
                    isHeap = false;
        }

        return isHeap;
    }

    private boolean isBinHeapValid0(int[] array) {
        if (array == null) return false;
        if (array.length == 0) return true;

        // Check as if it starts at 0.
        boolean isHeap = true;
        for (int i = 0; i < array.length; i++) {
            int p = i;
            int lc = 2 * p + 1;
            int rc = 2 * p + 2;
            if (lc < array.length)
                if (array[lc] > array[p])
                    isHeap = false;
            if (rc < array.length)
                if (array[rc] > array[p])
                    isHeap = false;
        }

        return isHeap;
    }

    private static double jaccardSimilarity(Collection x, Collection y) {
        Set a = new HashSet(x);
        Set b = new HashSet(y);
        Set c = new HashSet();
        c.addAll(a);
        c.addAll(b);
        double unionSize = c.size();
        c.retainAll(a);
        c.retainAll(b);
        double intersectionSize = c.size();
        return intersectionSize / unionSize;
    }

    private static List<Pair<String, String>> parseGraph(String graphData) throws Exception {
        Path path = Paths.get(graphData);

        List<Pair<String, String>> edges;
        try {
            // Read in file.
            List<String> lines = Files.readAllLines(path);

            // Trim the strings for erroneous whitespace.
            for (int i = 0; i < lines.size(); i++)
                lines.set(i, lines.get(i).trim());

            // Trim blank lines.
            int j = 0;
            while (j < lines.size()) {
                if (lines.get(j).length() < 1)
                    lines.remove(j);
                else
                    j++;
            }

            // Get the alleged number of vertices and initialize.
            int numVerts = Integer.parseInt(lines.get(0));
            edges = new ArrayList<>(numVerts);

            // Loop through lines in the file.
            for (int i = 1; i < lines.size(); i++) {
                String[] verts = lines.get(i).split("\\s+");
                Pair<String, String> edge = new Pair<>(verts[0], verts[1]);
                edges.add(edge);
            }
        } catch (Exception e) {
            if (graphData.equalsIgnoreCase(OUTPUTFILE)) throw new Exception("Failed to parse user's output graph.");
            System.out.println("Exception reading or parsing file (this probably" +
                    " happened during setup for tests and should be investigated): " + e.getMessage());
            edges = new ArrayList<>(0);
        }

        return edges;
    }

    private static class Pair<A, B> {
        private A a;
        private B b;

        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(a, pair.a) &&
                    Objects.equals(b, pair.b);
        }

        @Override
        public int hashCode() {

            return Objects.hash(a, b);
        }
    }

}










