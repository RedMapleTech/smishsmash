package tech.redmaple.smishsmash.analysis;

import org.apache.commons.net.WhoisClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;

import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import timber.log.Timber;

public class URLAnalysis {

    // Pattern for recognizing a URL, based off RFC 3986
    /*private static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);*/

    protected static final String NO_DETAILS = "No details";
    private static final int SUSPICIOUS_DOMAIN_AGE_MONTHS = 6;
    private static final int WHOIS_TIMEOUT_MS = 1000;

    protected static String[] extractURLs(String msgBody) {
        List<String> URLs = new ArrayList<>();

        // look for URLs regardless of keywords
        Matcher urlMatcher = android.util.Patterns.WEB_URL.matcher(msgBody);

        // look for URLs in body
        while (urlMatcher.find()) {
            int matchStart = urlMatcher.start(1);
            int matchEnd = urlMatcher.end();
            String url = msgBody.substring(matchStart, matchEnd);

            if (matchStart < 0 || matchEnd > msgBody.length()) {
                Timber.d("inspectURLs: Found URL outside of message body (from %d to %d))", matchStart, matchEnd);
            } else {
                Timber.d("inspectURLs: Found URL in message body: \"%s\"", url);
                URLs.add(url);
            }
        }

        if (URLs.size() == 0) {
            return null;
        } else {
            String[] array = new String[URLs.size()];

            for (int i = 0; i < URLs.size(); i++) {
                array[i] = URLs.get(i);
            }

            return array;
        }
    }

    protected static AnalysedURL analyseURL(String url) {
        Timber.d("Analysing URL: \"%s\"", url);

        AnalysedURL done = new AnalysedURL();
        WhoisClient whois = new WhoisClient();

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<AnalysedURL> callable = new Callable<AnalysedURL>() {
            @Override
            public AnalysedURL call() {
                AnalysedURL analysedURL = new AnalysedURL();

                try {
                    String domain = getDomainName(url);
                    whois.connect(WhoisClient.DEFAULT_HOST);

                    Timber.d("Starting whois query");
                    String whoisData = whois.query(domain);

                    if (whoisData.length() > 0) {
                        analysedURL = processWhoisData(domain, whoisData);
                    }

                    whois.disconnect();

                } catch (IOException e) {
                    Timber.d("Error I/O exception: %s", e.getMessage());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                return analysedURL;
            }
        };

        Future<AnalysedURL> result = executor.submit(callable);
        executor.shutdown();

        try {
            done = result.get(WHOIS_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            // it complains about java.util.IllegalFormatConversionException, but don't think we care
            //e.printStackTrace();
        } catch (TimeoutException e) {
            Timber.d("Timeout on whois");
        }

        return done;
    }

    private static AnalysedURL processWhoisData(String domain, String whoisData) {
        Timber.d("Analysis whois data for %s:\n%s", domain, whoisData);
        AnalysedURL analysedURL = new AnalysedURL();
        String[] lines = whoisData.split("\r\n");

        final String registrarPrefix = "Registrar: ";
        final String updatedDatePrefix = "Updated Date: ";
        final String createdDatePrefix = "Creation Date: ";

        String registrar = "";
        String created = "";
        String updated = "";

        for (String line : lines) {
            if (line != null && line.length() > 0) {

                if (line.contains(registrarPrefix)) {
                    registrar = line.replace(registrarPrefix, "").trim();
                    Timber.d("processWhoisData Registrar for %s: %s", domain, registrar);
                } else if (line.contains(createdDatePrefix)) {
                    created = line.replace(createdDatePrefix, "").trim();
                    Timber.d("processWhoisData Created for %s: %s", domain, created);
                } else if (line.contains(updatedDatePrefix)) {
                    updated = line.replace(updatedDatePrefix, "").trim();
                    Timber.d("processWhoisData Updated for %s: %s", domain, updated);
                }
            }
        }

        if (!registrar.equals("") && !created.equals("") && !updated.equals("")) {
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
            DateTime createdTime = fmt.parseDateTime(created);
            DateTime updatedTime = fmt.parseDateTime(updated);
            DateTime now = new DateTime();

            int monthsOld = Months.monthsBetween(createdTime, now).getMonths();
            int monthsSinceUpdate = Months.monthsBetween(updatedTime, now).getMonths();

            if (monthsOld == 0 || monthsSinceUpdate == 0) {
                int daysOld = Days.daysBetween(createdTime, now).getDays();
                analysedURL.setSuspicious(true);
                Timber.d("Suspicious domain %s was registered with %s %d days ago (%s). Malicious sites ", domain, registrar, daysOld, created);
                analysedURL.setAnalysis(String.format(Locale.getDefault(), "\n\nSuspicious site was registered with %s %d days ago. Malicious sites tend to be recently registered.", registrar, daysOld));
            } else {
                if (monthsOld < SUSPICIOUS_DOMAIN_AGE_MONTHS) {
                    analysedURL.setSuspicious(true);
                    Timber.d("Suspicious site was recently registered with %s %d months ago (%s). Last updated %d months ago (%s).", registrar, monthsOld, created, monthsSinceUpdate, updated);
                    analysedURL.setAnalysis(String.format(Locale.getDefault(), "\n\nSuspicious site was recently registered with %s %d months ago, and last updated %d months ago. Malicious sites tend to be recently registered.", registrar, monthsOld, monthsSinceUpdate));
                } else {
                    analysedURL.setSuspicious(false);
                    Timber.d("Site was registered with %s %d months ago (%s). Last updated %d months ago (%s).", registrar, monthsOld, created, monthsSinceUpdate, updated);
                }
            }
        } else {
            analysedURL.setSuspicious(false);
            analysedURL.setAnalysis(NO_DETAILS);
        }

        analysedURL.setAnalysed(true);

        return analysedURL;
    }

    // thanks https://stackoverflow.com/questions/9607903/get-domain-name-from-given-url
    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();

        if (domain != null) {
            domain = domain.startsWith("www.") ? domain.substring(4) : domain;
        } else {
            domain = url;
        }

        return domain;
    }
}
