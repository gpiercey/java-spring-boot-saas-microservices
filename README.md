# java-spring-boot-saas-microservices
This is a starter project for anyone wanting to build out the backend of a SaaS application using Java, Spring Boot, MongoDB, Redis and AWS infrastructure components.

None of the microservices contained in this project are production ready and will need to be modified and secured further to ensure quality and security, although we should have most of the important stuff covered already, or at least annotated with a TODO comment where it isn't.

**microservice-auth:**<br/>
A microservice used on the **internal** network for authentication and authorization. This is usually the single most critical component in a microservice architecture and is built as a layer that sits between your central data store and an Identity Provider or Directory Service, usually external. The external IDP stores only user id and hashed password so there is nothing more to link a password to a user and an attacker would need to breach two systems to put that data together. This approach doesn't eliminate risk but reduces it by making the attack much harder.

**microservice-userdata:**<br/>
A data-layer microservice used to store the user data. These records share a user id with the external IDP but nothing else. All PII is stored in this record in the internal database and this microservice provides the CRUD endpoints to support it. It utilizes request caching to boost performance and reduce database load, and it will sanitize all user input to address CSS vulnerabilities.

**microservice-company:**<br/>
A simple data-layer microservice used to store a company's meta data.

## 1. Simulated Infrastructure for Local Development
For this project the infrastructure components used for local development are managed with Docker Compose, they are containerized and ephemeral. Note that I didn't mock out any of these components in the automated tests so you'll need to ensure they are all running in the background when you run the tests. Mocking would be a good improvement here to decouple the tests from the infrastructure.

 * https://docs.docker.com/reference/cli/docker/compose/ <br>
 * https://docs.docker.com/reference/compose-file/

```shell
docker compose up -d --build
docker compose ps
```

### 1.1 MongoDB
```shell
docker compose up mongodb -d
docker compose logs mongodb --follow
docker compose down mongodb --remove-orphans
```

### 1.2 Loki / Grafana
```shell
docker compose up loki grafana -d
docker compose logs loki --follow
docker compose down loki grafana --remove-orphans
```

### 1.3 Redis
```shell
docker compose up redis -d
docker compose logs redis --follow
docker compose down redis --remove-orphans
```

## 2. Database Design
When considering whether to use a dedicated database for each microservice as recommended by best practices versus a single shared relational database across multiple microservices as is commonly seen in the wild, there are arguments for and against each approach.

What we'll see in practice is that the choice between dedicated databases and a shared relational database, or some combination thereof, depends on the specific requirements of the system, including the need for scalability, autonomy, and operational complexity. Many organizations start with a shared database for simplicity and transition to dedicated databases as their microservices architecture matures and requires more independence and scaling. This is also a common approach when companies are faced with the task of decomposing a monolith into microservices, they start out with a highly relational database design and start to peel off layers into microservices, the relationships in the database are often a lot harder to decompose than the business logic in the services. Ultimately, the decision should align with the overall architecture strategy and the specific use cases of the microservices involved, which will often land in some middle ground between the status quo and theoretical best practice.

In this project I am using MongoDB and attempting to break apart relationships in the data model to support the NoSQL approach, but I am using a separate collection for each microservice instead of a separate database for each, this is just being done for simplicity in local development. This won't be the best choice for everyone. Adjust this as you see fit.

### 2.1 Arguments for Dedicated Databases
**Decoupling:**</br>
Each microservice can evolve independently. Changes in one service's data model won’t affect others, promoting autonomy and reducing single points of failure.

**Technology Diversity:**</br>
Teams can choose the best database technology for their specific service's needs, for example NoSQL for flexibility, SQL for complex queries, etc.

**Performance Optimization:**</br>
Dedicated databases can be optimized for specific workloads, reducing contention and improving performance for each service.

**Fault Isolation:**</br>
Issues in one database (e.g., performance bottlenecks) won’t impact others, enhancing system reliability.

**Security:**</br>
Each database can optionally have its own security model, reducing the risk of unauthorized access across services.

### 2.2 Arguments Against Dedicated Databases
**Data Consistency:**</br>
Maintaining data consistency across microservices can be challenging, especially if transactions span multiple databases.

**Operational Overhead:**</br>
More databases mean more operational complexity, including provisioning, scaling, and maintenance for each database instance.

**Increased Resource Usage:**</br>
Dedicated databases may lead to under utilization of resources, as some databases might be idle while others are heavily loaded.

**Complex Data Access:**</br>
Inter-service communication can become complex if services need to access each other's data, leading to potential performance issues. Essentially, relationships would need to be pushed up a layer into the microservices instead of the conventional wisdom of letting an RDBMS handle it.

### 2.3 Arguments for a Shared Relational Database
**Simplified Data Management:**</br>
A single database can simplify schema management and backup processes, reducing the operational burden.

**Easier Data Consistency:**</br>
Using a shared database can make it easier to ensure data consistency, especially for operations that require transactions across services.

**Resource Efficiency:**</br>
Shared databases can lead to better resource utilization since multiple services can access the same instance.

**Familiarity:**</br>
Teams may already be familiar with a single database solution, reducing the learning curve and easing development.

### 2.4 Arguments Against a Shared Relational Database
**Tight Coupling:**</br>
Services become interdependent, making it harder to evolve or deploy them independently, which goes against the microservices philosophy.

**Scalability Issues:**</br>
A single database can become a bottleneck as the number of services and their load increases, affecting performance.

**Complexity in Data Access:**</br>
With multiple services accessing the same database, complex queries can arise, and a poorly designed schema can lead to contention.

**Single Point of Failure:**</br>
If the shared database goes down, all services depending on it are affected, increasing the risk to system reliability.
