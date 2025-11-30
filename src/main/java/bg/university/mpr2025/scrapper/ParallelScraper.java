package bg.university.mpr2025.scrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.*;

public class ParallelScraper {
    
    private static final Set<String> CONTENT_SELECTORS = new HashSet<>(Arrays.asList(
        "h2.post-block__title a",  // Article titles
        "div.post-block__content", // Article excerpts
        "a.post-block__title__link" // Article links
    ));
    
    private static final Set<String> IGNORED_ELEMENTS = new HashSet<>(Arrays.asList(
        "script", "style", "noscript", "iframe", "object", "embed",
        "nav", "footer", "header", "aside", "form", "button"
    ));
    
    private static final Set<String> IGNORED_CLASSES = new HashSet<>(Arrays.asList(
        "menu", "advertisement", "social", "share", "related", "popular", "trending",
        "newsletter", "subscribe", "cookie", "privacy", "modal", "overlay", "banner"
    ));
    
    /**
     * Scrapes a website and returns a list of results
     * @param url The URL to scrape
     * @param threads Number of threads to use for parallel processing
     * @param rowsLimit Maximum number of results to return
     * @return List of scraped results
     */
    public List<String> scrape(String url, int threads, int rowsLimit) throws Exception {
        long startTime = System.currentTimeMillis();
        
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(15000)
                    .followRedirects(true)
                    .get();
            
            cleanDocument(doc);
            
            Elements contentElements = new Elements();
            for (String selector : CONTENT_SELECTORS) {
                contentElements.addAll(doc.select(selector));
            }
            
            List<String> results = processElementsInParallel(contentElements, threads, rowsLimit);
            
            if (results.isEmpty()) {
                results.add("No content found. The website structure might have changed.");
            } else if (rowsLimit > 0 && results.size() > rowsLimit) {
                return results.subList(0, rowsLimit);
            }
            
            return results;
            
        } catch (Exception e) {
            throw new Exception("Scraping failed: " + e.getMessage(), e);
        } finally {
            System.out.println("Scraping completed in " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }
    
    private void cleanDocument(Document doc) {
        // Remove script and style elements
        for (String tag : IGNORED_ELEMENTS) {
            doc.select(tag).remove();
        }
        
        // Remove elements with ignored classes or IDs
        for (String cls : IGNORED_CLASSES) {
            doc.select("." + cls).remove();
            doc.select("#" + cls).remove();
            doc.select("[class*=" + cls + "]").remove();
        }
        
        // Remove common non-content elements
        doc.select("header, footer, nav, aside, form, button, input, textarea, select, iframe").remove();
        
        // Remove empty elements
        doc.select(":empty").remove();
    }
    
    /**
     * Processes HTML elements in parallel using the specified number of threads
     */
    private List<String> processElementsInParallel(Elements elements, int threadCount, int rowsLimit) 
            throws InterruptedException, ExecutionException {
            
        if (elements == null || elements.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(
            Math.min(threadCount, elements.size()),
            r -> {
                Thread t = new Thread(r);
                t.setDaemon(true); // Make threads daemon threads
                return t;
            }
        );
        
        try {
            // Process elements in parallel
            List<Future<String>> futures = new ArrayList<>();
            for (Element element : elements) {
                futures.add(executor.submit(() -> processElement(element)));
            }
            
            // Collect results
            List<String> results = new ArrayList<>();
            for (Future<String> future : futures) {
                String result = future.get();
                if (result != null && !result.trim().isEmpty()) {
                    results.add(result.trim());
                    
                    // Early termination if we've reached the row limit
                    if (rowsLimit > 0 && results.size() >= rowsLimit) {
                        break;
                    }
                }
            }
            
            return results;
            
        } finally {
            // Shutdown the executor
            executor.shutdownNow();
        }
    }
    
    /**
     * Processes a single HTML element and extracts relevant text
     */
    private String processElement(Element element) {
        if (element == null) {
            return "";
        }
        
        // Skip elements with no text or very short text
        String text = element.text().trim();
        if (text.isEmpty() || text.length() < 10) {
            return "";
        }
        
        // Skip elements that are likely navigation or metadata
        String className = element.className().toLowerCase();
        String id = element.id().toLowerCase();
        
        if (isUnwantedElement(className, id, element.tagName())) {
            return "";
        }
        
        return text;
    }
    
    /**
     * Checks if an element should be ignored based on its classes, ID, or tag name
     */
    private boolean isUnwantedElement(String className, String id, String tagName) {
        // Skip common navigation and footer elements
        if (className.contains("navi") || className.contains("footer") || 
            className.contains("header") || className.contains("menu") ||
            id.contains("navi") || id.contains("footer") || 
            id.contains("header") || id.contains("menu")) {
            return true;
        }
        
        // Skip common metadata tags
        return "meta".equals(tagName) || "link".equals(tagName) || "script".equals(tagName) || 
               "style".equals(tagName) || "noscript".equals(tagName);
    }
}

