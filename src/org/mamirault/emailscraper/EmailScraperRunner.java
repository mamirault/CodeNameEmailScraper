package org.mamirault.emailscraper;

import java.net.URISyntaxException;

public class EmailScraperRunner {

	public static void main(String[] args) {
		String domain = args[0];

		EmailScraper emailScraper;
		try {
			emailScraper = new EmailScraper(domain);
			emailScraper.scrape();
		} catch (URISyntaxException e) {
			System.out.println(e.getMessage());
		}
	}
}
