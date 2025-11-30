package bg.university.mpr2025.scrapper;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelScraper {

    public List<String> scrape(String url, int threads, int rowsLimit) throws Exception {

        long start = System.currentTimeMillis();

        // 1. Изтегляме HTML
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(5000)
                .get();

        // 2. Вземаме всички <p> елементи (може да сменим с h1/h2/div/table-row)
        Elements paragraphs = doc.select("p");

        // 3. Списък за резултати
        List<String> results = new CopyOnWriteArrayList<>();

        // 4. ExecutorService с подадения брой нишки
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();

        // Паралелно обработваме параграфите
        for (var elem : paragraphs) {
            futures.add(executor.submit(() -> {
                String text = elem.text().trim();
                if (!text.isEmpty()) {
                    results.add(text);
                }
            }));
        }

        // 5. Изчакваме всички нишки
        for (Future<?> f : futures) {
            f.get();
        }
        executor.shutdown();

        long end = System.currentTimeMillis();
        long total = end - start;

        // Ограничение на броя редове
        if (rowsLimit > 0 && results.size() > rowsLimit) {
            return results.subList(0, rowsLimit);
        }

        return results;
    }
}

