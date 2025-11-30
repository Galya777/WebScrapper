package bg.university.mpr2025.benchmark;

import bg.university.mpr2025.scrapper.ParallelScraper;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public class Benchmark {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: Benchmark <url> <output.csv> [rows] [maxThreads]");
            return;
        }

        String url = args[0];
        String csv = args[1];
        int rows = args.length >= 3 ? Integer.parseInt(args[2]) : 50;
        int maxT = args.length >= 4 ? Integer.parseInt(args[3]) : 8;

        ParallelScraper scraper = new ParallelScraper();

        try (PrintWriter out = new PrintWriter(new FileWriter(csv))) {
            out.println("threads,timeMs,rows");

            for (int t = 1; t <= maxT; t *= 2) {
                long start = System.currentTimeMillis();
                List<String> r = scraper.scrape(url, t, rows);
                long ms = System.currentTimeMillis() - start;
                out.println(t + "," + ms + "," + r.size());
                System.out.println("threads=" + t + " time=" + ms + "ms");
            }
        }
    }
}
