package com.main.farforlow.view;

public enum ServiceMessages {
    DEPARTURE_CITY("Enter departure city (e.g. Moscow):"),
    DESTINATION_CITY("Destination city (e.g. Berlin):"),
    TRIP_DURATION("Trip duration, days (min-max e.g. 8-10):"),
    SEARCH_PERIOD("Search period (e.g. 25.03.2030-15.04.2030):"),
    REQUEST_FILLED("You are all set! Stay tuned for best deal of the day."),
    NO_BOTS("No bots please!"),
    STOP("OK. Let me know when you are ready to /start again."),
    REQUST_EXISTS("You already have request in process. Send /stop and then /start for new request."),
    NO_REQUSTS("No requests at the moment. Send /start."),
    REQUST_EXPIRED("Your request has expired. Hope you found tickets to your dream destination! Send /start for new request."),
    REQUST_QUEUING("Thank you for your patience. This is non-commercial project with limited resources and currently server capacity is fully utilized by requests from other users. Requests in front of you: "),
    CLOSE_REQUST("I can't find any options so closing the request (might be that data source doesn't have this connection or no flights are available for these dates). Send /start to shape new request."),
    NO_RESULTS("I can't find any options."),
    DAILY_OFFER_VS_BEST("Best offer of the day: %d %s (%+d vs lowest ever found price). More details: %s"),
    DAILY_OFFER("Best offer of the day: %d %s. More details: %s"),
    OTHER_OFFERS_HEADER("Few closest offers for other dates:\n"),
    OTHER_OFFER("%d: %d %s. More details: %s\n"),
    NO_SUCH_COMMAND("Don't know such command or something went wrong. Let's try again or send /stop and then /start."),
    DURATION_EXCEPTION("Please use the format: 12-14"),
    SEARCH_PERIOD_EXCEPTION("Please check dates. Use exact format: 25.03.2030-15.04.2030. Plus make sure both search period dates are in the future and start date followed by end date."),
    CITY_EXCEPTION_NO_OPTIONS("Can't find such city or it don't have international airports. Let's try again."),
    CITY_EXCEPTION_WITH_OPTIONS("Found many cities. Type in your city: %s"),
    REQUESTS_QUANTITIES_EXCEPTION("Request is too loose :(. %d options need to be checked and current limit is %d options per request. Try more precise trip duration and/or search period.");

    public final String text;

    private ServiceMessages(String text) {
        this.text = text;
    }
}
