package org.mamirault.emailscraper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public class EmailScraper {
	private final URI baseDomain;

	private Set<String> pagesToScrape = Sets.newHashSet();
	private Set<String> pagesCurrentlyScraping = Sets.newHashSet();
	private Set<String> pagesAlreadyScraped = Sets.newHashSet();

	private Set<String> emails = Sets.newHashSet();

	private static final Pattern EMAIL_PATERN = Pattern.compile(
			"\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b",
			Pattern.CASE_INSENSITIVE);

	EmailScraper(String domain) throws URISyntaxException {
		this.baseDomain = new URI("http://" + domain);

		pagesToScrape.add("http://" + domain);
	}

	public void scrape() {
		while (!pagesToScrape.isEmpty()) {
			scrapeCurrent();
		}

		System.out.println("Found these email addresses:");
		for (String email : emails) {
			System.out.println(email);
		}
	}

	private void scrapeCurrent() {
		pagesCurrentlyScraping.addAll(pagesToScrape);
		pagesToScrape.clear();

		for (String page : pagesCurrentlyScraping) {
			scrapePage(page);
			pagesAlreadyScraped.add(page);
		}

		pagesAlreadyScraped.addAll(pagesCurrentlyScraping);
		pagesCurrentlyScraping.clear();

	}

	private void scrapePage(String page) {
		try {
			Document doc = Jsoup.connect(page).get();

			for (Element element : doc.getAllElements()) {
				scrapeLinks(element);
				scrapeEmails(element);
			}
		} catch (IOException e) {
			System.out.println("Tried to load " + page + " but there was a problem.");
		}
	}

	private void scrapeEmails(Element element) {
		Matcher matcher = EMAIL_PATERN.matcher(element.text());
		
		while (matcher.find()) {
			emails.add(matcher.group());
		}
	}

	private void scrapeLinks(Element element) {
		String url = element.attr("abs:href");

		if (!Strings.isNullOrEmpty(url)) {
			try {
				URI uri = new URI(url);
				if (uri.getHost() != null && uri.getHost().endsWith(baseDomain.getHost())) {
					if (!pagesAlreadyScraped.contains(url) && !pagesCurrentlyScraping.contains(url)) {
						pagesToScrape.add(url);
					}
				}
			} catch (URISyntaxException e) {
				System.out.println("Bad URI syntax for " + url);
			}
		}
	}
}