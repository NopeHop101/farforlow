# farforlow
This service is searching for the lowest ticket price for loose search (e.g. Paris to Lima for 2-3 weeks in May-June).
The search is run daily.

Telegram bot is used as a front end. (messages from users shape requests)

Tickets price data is parsed from https://skiplagged.com/ with selenium headless browser.

Time for one search execution is circa 30s.

Quantity of threads depends on server capacity. Safe load is one thread per core. 

Source for list of cities and airports: https://ourairports.com/data/

Back end written on Java. (users request processing and sending results back to Telegram plus developer end points to manipulate information in database - see RequestController)

Search requests information is storied in mongodb.


Deployment through Docker (see docker-compose.yml to add your settings)
Bot in Telegram needs to be created with BotFather (see Telegram api documentation)
