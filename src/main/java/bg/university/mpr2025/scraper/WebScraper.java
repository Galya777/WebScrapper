package bg.university.mpr2025.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebScraper {
    
    /**
     * Scrapes a website and returns a list of results
     * @param url The URL to scrape
     * @param maxResults Maximum number of results to return
     * @return List of scraped results
     */
    public List<String> scrape(String url, int maxResults) {
        List<String> results = new ArrayList<>();
        
        try {
            // Fetch the HTML content of the page
            Document doc = Jsoup.connect(url).get();
            
            // Example: Scrape headlines from a news website
            // This is just an example - you'll need to adjust the selectors based on the target website
            Elements elements = doc.select("h1, h2, h3, h4, h5, h6, p, a");
            
            int count = 0;
            for (Element element : elements) {
                if (count >= maxResults) break;
                
                String text = element.text().trim();
                if (!text.isEmpty()) {
                    results.add(text);
                    count++;
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            results.add("Error scraping URL: " + e.getMessage());
        }
        
        return results;
    }
}
