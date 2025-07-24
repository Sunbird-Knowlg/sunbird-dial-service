# sunbird-dial-service
Micro-service for DIAL code management

## DIAL-Service local setup
This readme file contains the instruction to set up and run the DIAL-service in local machine.
### Prerequisites:
* Elasticsearch
* Redis
* Cassandra


### Prepare folders for database data and logs

```shell
mkdir -p ~/sunbird-dbs/neo4j ~/sunbird-dbs/cassandra ~/sunbird-dbs/redis ~/sunbird-dbs/es ~/sunbird-dbs/kafka
export sunbird_dbs_path=~/sunbird-dbs
```

### Elasticsearch database setup in docker:
```shell
docker run --name sunbird_es -d -p 9200:9200 -p 9300:9300 \
-v $sunbird_dbs_path/es/data:/usr/share/elasticsearch/data \
-v $sunbird_dbs_path/es/logs://usr/share/elasticsearch/logs \
-v $sunbird_dbs_path/es/backups:/opt/elasticsearch/backup \
 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:6.8.22

```
> --name -  Name your container (avoids generic id)
>
> -p - Specify container ports to expose
>
> Using the -p option with ports 7474 and 7687 allows us to expose and listen for traffic on both the HTTP and Bolt ports. Having the HTTP port means we can connect to our database with Neo4j Browser, and the Bolt port means efficient and type-safe communication requests between other layers and the database.
>
> -d - This detaches the container to run in the background, meaning we can access the container separately and see into all of its processes.
>
> -v - The next several lines start with the -v option. These lines define volumes we want to bind in our local directory structure so we can access certain files locally.
>
> --env - Set config as environment variables for Neo4j database
>


### Redis database setup in docker:
1. we need to get the redis image from docker hub using the below command.
```shell
docker pull redis:6.0.8 
```
2. We need to create the redis instance, By using the below command we can create the same and run in a container.
```shell
docker run --name sunbird_redis -d -p 6379:6379 redis:6.0.8
```
3. To SSH to redis docker container, run the below command
```shell
docker exec -it sunbird_redis bash
```
### cassandra database setup in docker:
1. we need to get the cassandra image and can be done using the below command.
```shell
docker pull cassandra:3.11.8 
```
2. We need to create the cassandra instance, By using the below command we can create the same and run in a container.
```shell
docker run --name sunbird_cassandra -d -p 9042:9042 \
-v $sunbird_dbs_path/cassandra/data:/var/lib/cassandra \
-v $sunbird_dbs_path/cassandra/logs:/opt/cassandra/logs \
-v $sunbird_dbs_path/cassandra/backups:/mnt/backups \
--network bridge cassandra:3.11.8 
```
For network, we can use the existing network or create a new network using the following command and use it.
```shell
docker network create sunbird_db_network
```
3. To start cassandra cypher shell run the below command.
```shell
docker exec -it sunbird_cassandra cqlsh
```
4. To ssh to cassandra docker container, run the below command.
```shell
docker exec -it sunbird_cassandra /bin/bash
```
5. Load seed data to cassandra using the instructions provided in the [link](https://github.com/project-sunbird/sunbird-learning-platform/blob/master/ansible/roles/cassandra-db-update/templates/dialcode.cql.j2)

### Running kafka using docker:
1. Kafka stores information about the cluster and consumers into Zookeeper. ZooKeeper acts as a coordinator between them. we need to run two services(zookeeper & kafka), Prepare your docker-compose.yml file using the following reference.
```shell
version: '3'

services:
  zookeeper:
    image: 'wurstmeister/zookeeper:latest'
    container_name: zookeeper
    ports:
      - "2181:2181"    
    environment:
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://127.0.0.1:2181     
    
  kafka:
    image: 'wurstmeister/kafka:2.11-1.0.1'
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      - KAFKA_BROKER_ID=1
      - KAFKA_LISTENERS=PLAINTEXT://:9092
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://127.0.0.1:9092
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181      
      - ALLOW_PLAINTEXT_LISTENER=yes
    depends_on:
      - zookeeper  
```
2. Go to the path where docker-compose.yml placed and run the below command to create and run the containers (zookeeper & kafka).
```shell
docker-compose -f docker-compose.yml up -d
```
3. To start kafka docker container shell, run the below command.
```shell
docker exec -it kafka sh
```
Go to path /opt/kafka/bin, where we will have executable files to perform operations(creating topics, running producers and consumers, etc).
Example:
```shell
kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic test_topic 
```

### Running DIAL-service:
1. Go to the path: /sunbird-dial-service and run the below maven command to build the application.
```shell
mvn clean install -DskipTests
```
2. Go to the path: /sunbird-dial-service and run the below maven command to run the netty server.
```shell
mvn play2:run
```
3. Using the below command we can verify whether the databases(neoj,redis & cassandra) connection is established or not. If all connections are good, health is shown as 'true' otherwise it will be 'false'.
```shell
curl http://localhost:9000/health
```



# DIAL code Custom Context Setup

1. 'jsonld-schema' folder is used to save context and json-ld files. 
2. For implementing custom context, you can create a folder under 'jsonld-schema'. Folder should contain 'context.json' file. Specify the folder name in 'jsonld.type' attribute in 'application.conf' file for application to refer to new context.
3. 'contextValidation.json' file is optional. If available, context data input validation will happen when dial code update v2 API call is made with 'contextInfo'.
4. Reference of 'sb' vocabulary (schema.jsonld) in the custom 'context.json' is a must.


## GitHub Actions Workflow 

### Build Docker image Workflow

Prerequisites

To ensure the GitHub Actions workflows in this repository function correctly, the following prerequisites must be met:

1. **Secrets Configuration**:
   - Ensure the secrets are configured in your GitHub repository, depending on the value of `REGISTRY_PROVIDER`. The workflow will push the image to the respective container registry if the required credentials are provided.

   - Note: If No REGISTRY_PROVIDER is provided the image will be pushed to GHCR.

    #### GCP (Google Cloud Platform)
    - `REGISTRY_PROVIDER`: Set to `gcp`
    - `GCP_SERVICE_ACCOUNT_KEY`: Base64-encoded service account key for GCP.
    - `REGISTRY_NAME`: GCP registry name (e.g., `asia-south1-docker.pkg.dev`).
    - `REGISTRY_URL`: URL of the GCP container registry (e.g., `asia-south1-docker.pkg.dev/<project_id>/<repository_name>`).

    #### DockerHub
    - `REGISTRY_PROVIDER`: Set to `dockerhub`
    - `REGISTRY_USERNAME`: DockerHub username.
    - `REGISTRY_PASSWORD`: DockerHub password.
    - `REGISTRY_NAME`: DockerHub registry name (e.g., `docker.io`).
    - `REGISTRY_URL`: URL of the DockerHub registry (e.g., `docker.io/<username>`).

    #### Azure Container Registry (ACR)
    - `REGISTRY_PROVIDER`: Set to `azure`
    - `REGISTRY_USERNAME`: ACR username (service principal or admin username).
    - `REGISTRY_PASSWORD`: ACR password (service principal secret or admin password).
    - `REGISTRY_NAME`: ACR registry name (e.g., `myregistry.azurecr.io`).
    - `REGISTRY_URL`: URL of the ACR registry (e.g., `myregistry.azurecr.io`).

    #### GitHub Container Registry (GHCR)
    - `REGISTRY_PROVIDER`: Set to any value other than above (default is GHCR)
    - No additional secrets are required. The workflow uses the built-in `GITHUB_TOKEN` provided by GitHub Actions for authentication.

    Ensure these secrets are added to the repository settings under **Settings > Secrets and variables > Actions**.
    By ensuring these prerequisites are met, the workflows in this repository will execute successfully.

### Pull Request Quality Checks

Every pull request triggers a GitHub Actions workflow that:

- Spins up **Redis (6.0.8)** and **Elasticsearch (7.10.2)** containers
- Runs unit tests and publishes results
- Builds the project using Maven
- Performs **SonarCloud** static code analysis
- Comments test and analysis results on the PR

> Requires `SONAR_TOKEN` to be set in repository secrets.
