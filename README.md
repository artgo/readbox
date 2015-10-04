# Reader of most recent files from Box

In order to be able to run it you need to go to get access and refresh tokens here: http://box-token-generator.herokuapp.com/
And then set the following environment variables:
 - BOX_REFRESH_TOKEN
 - BOX_ACCESS_TOKEN
 - BOX_CLIENT_SECRET
 - BOX_CLIENT_ID
 
You can execute with ```mvn exec:java``` or run tests with ```mvn clean test```

