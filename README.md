# farforlow
This service is searching for the lowest ticket price for loose search (e.g. Paris to Lima for 2-3 weeks in May-June).
The search is run daily.

Telegram bot is used as a front end. (messages from users to shape requests)
Tickets price data is parsed from https://skiplagged.com/ with selenium headless browser. (10 browsers spawned in simultaneously with directional time for 1 search execution - 30s)
Source for list of cities and airports: https://ourairports.com/data/
Back end written on Java. (users request processing and sending results back to Telegram plus developer end points to manipulate information in database - see RequestController)
Search requests information is storied in mongodb.
Search for cities and airports was implemented with elasticsearch.

Deployment requirements:
 - Docker with mongodb and elasticsearch
 - Java 8
 - Chrome driver
 - Bot in Telegram created with BotFather

Settings:
 - application.properties
    - uri for your mongodb
    - enabled elasticsearch repositories
    - logging file path with name
 - Set your list of proxies in SkiplaggedClient for rotation to avoid block from skiplagged.com
 - Set path to chrome driver in SkiplaggedClient
 - Set access header token in AccessCheck for secure access to developer end points (add "token=your_token" to headers of your requests to the end points)
 - Set url of your Telegram bot in Messenger (https://api.telegram.org/botYOUR_BOT_TOKEN/sendMessage)
 - Set host and port of your elasticsearch database in ElasticsearchClientConfig

