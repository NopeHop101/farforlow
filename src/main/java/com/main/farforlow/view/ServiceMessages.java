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
    CLOSE_REQUST("I can't find any options so closing the request (might be that data source doesn't have this connection or no flights are available for these dates). Send /start to shape new request.");

    public final String text;

    private ServiceMessages(String text) {
        this.text = text;
    }
}
