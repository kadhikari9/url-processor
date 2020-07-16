# url-processor

Dependencies:
<p>
Maven for building and packaging the application. Make sure maven is installed
and mvn command is enabled on your terminal.
</p>
<p>
Junit, Apache HttpClient and logback/slf4j for logging
</p>
Steps to Run:
<br/>
Pre-steps:
<p>
- Create following directories:
<br/>
# Directory to upload compressed url files
<br/>
# Directory to move processed url files
<br/>
# Update properties, url.files.path and url.files.processed.path accordingly in
application.properties file before building
<br/>
# There is a bash script provided which will listen to new flies in directory, decompress them and split to max chunks of 10MB. Each file can have max 2500 
lines of urls at max. If all url are of max length total file size can be max 10MB.
This is to avoid reading large files.
<br/>
# To use this script install inotify tools on your linux machine. 
<br/>
sudo apt-get install -y inotify-tools 
<br/>
# If you don't want to use file splitting feature or are in different environment, 
pls just decompress the gz files 
and place on the directory configured on application.properties file
</p>
<p>Run Project:
<br/>
#Please run mock httpserver before running application.
<br/>- Go to Project directory
<br/>
- run command: mvn clean package
<br/>
- After build is complete go to target folder
<br/>
- Run command: java -jar url-processor-1.0-SNAPSHOT-jar-with-dependencies.jar 
</p>
