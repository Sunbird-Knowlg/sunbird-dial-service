#!/bin/bash
sudo apt update
sudo apt install redis-server -y
curl -O https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.8.22.deb
sudo dpkg -i elasticsearch-6.8.22.deb
sudo service elasticsearch start
sudo service elasticsearch status
find ./ -type f -name "logback.xml" -print0 | xargs -0 sed -i -e 's/\/data\/logs/logs/g'
find ./ -type f -name "application.conf" -print0 | xargs -0 sed -i -e 's/\/data\//~\//g'
find ./ -type f -name "*.java" -print0 | xargs -0 sed -i -e 's/\/data\//~\//g'
java -version
mvn -v
mvn scoverage:report
JAVA_REPORT_PATHS=`find /home/circleci/project  -iname jacoco.xml | awk 'BEGIN { RS = "" ; FS = "\n"; OFS = ","}{$1=$1; print $0}'`
mvn verify sonar:sonar -Dsonar.projectKey=project-sunbird_sunbird-dial-service -Dsonar.organization=project-sunbird -Dsonar.host.url=https://sonarcloud.io -Dsonar.coverage.jacoco.xmlReportPaths=${JAVA_REPORT_PATHS}
