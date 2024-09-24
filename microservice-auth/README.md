# 1. Authentication & Authorization Microservice
This is a starter project for an authentication and authorization microservice written with Java and Spring Boot. Take it and do what you want with it, I wrote it as a demo project to give a head start when building something like this but you'll still have to extend it, integrate it with other cloud components and secure it.

* https://auth0.com/docs/authenticate/protocols/oauth
* https://jwt.io/
* https://stytch.com/docs/b2b/guides/rbac/overview

## 1.1 High Level Overview
In a microservices architecture, authentication and authorization are probably the most critical components of the architecture, ensuring the security and proper functioning of the system as a whole. When one of these go down, everything goes down, so it's important to build this component with scalability, resilience and performance in mind.

Authentication (Authn) is the process of verifying the identity of a user or service, ensuring that only legitimate entities can access the system. This is vital in a microservices environment, where multiple independent services interact and exchange data. Without robust authentication, malicious actors could potentially gain access to sensitive parts of the system, leading to unauthorized data access or manipulation. Each service would need to authenticate incoming requests to ensure they come from trusted sources, maintaining the integrity and confidentiality of the overall system.

Authorization (Authz), on the other hand, determines what authenticated entities are allowed to do within the system. In a microservices architecture, this involves setting permissions and access controls for various resources and operations across different services. Proper authorization mechanisms ensure that even authenticated users or services can only perform actions or access data that they are explicitly permitted to. This prevents overreach and enforces the principle of least privilege, minimizing potential damage from compromised accounts or services. By clearly defining and enforcing authorization policies, organizations can protect against unauthorized access and ensure that each microservice adheres to its intended security boundaries. Together, authentication and authorization form a foundational layer of security that helps safeguard a microservices-based system against various threats and vulnerabilities.

## 1.2 Security Considerations
### 1.2.1 Identity Providers (IDPs)
It is highly recommended that you use an external identity provider (IDP) or directory service (DS) to store user credentials. Something like Okta, Auth0, Azure DS or other LDAP providers are generally suitable. Get this information offsite, out of your database and into hands of a trusted third party who specializes in managing this data.

### 1.2.2 Personally Identifiable Information (PII)
I also highly recommend that you do not offload any PII data to these IDPs, offload a UUID user identifier and their passwords, nothing more. In the event of a breach at the IDP the attacker would have no usernames, emails, addresses or phone numbers to associate with those hashed passwords so it would make it quite difficult for them to sell on the dark web. Similarly, in the event of a breach of your systems, you would have no passwords in your database to associate with the user data. An attacker would have to breach two separate systems and put the data together to got the full picture.

### 1.2.3 Enterprise Directory Services (DSs)
This may be a little tricky for large enterprises who manage their own DSs but the same principle applies, get the data into a different data center, a different subnet and away from your central database so there's no PII to associate with passwords. Make life difficult for anyone attacking your data.

### 1.2.4 Cryptographic Key Management for JWT Tokens
In this demo project we are using a hardcoded UUID as the secret for signing JWT tokens but this is not secure so you'd need to adjust the approach if you wanted to adapt this for productin use. I'd suggest storing a key in AWS KMS, or similar secure key storage, and storing enough information to look up the key at boot time. Using HMAC with KMS should give a pretty secure system and allow you to rotate keys easily. Consult your security team for recommendations on how you should approach this problem.

## 1.3 Performance Considerations
### 1.3.1 Managing Vendor Downtime & Network Latency
Vendor downtime and network latency are completely out of our control but will impact us significantly when we offload our authentication and authorization components, so I built this service to mitigate these problems as much as possible.

This service will authenticate users against the IDP/DS but it will generate and sign JWT tokens itself for better control of performance and uptime. The fewer requests that have to traverse the WAN the lower the chances of widespread failure. 

The service needs to utilize caching to lower request latency and reduce the number of callbacks it makes to the IDP/DS. It should validate tokens for all internal microservices so it doesn't have to share its crypto keys with any other services, and it should operate with multiple replicas, load balanced and have an aggressive autoscaling policy. Note that the use of caching here to validate tokens offers another security benefit to address one of the drawbacks of using JWT tokens, the ability to invalidate or revoke a token immediately so you won't have to wait for the token to expire.

## 1.4 Observability
When you stand this service up in Kubernetes you should really consider attaching some enterprise logging, monitoring and alerting solutions to the pods.

This service utilizes the standard Java logging frameworks and there is a custom component called the ExecutionTimer that will capture the execution time of each REST request and dump to the logs with the method, resource path, query parameters, protocol, source IP, user agent and if the header is available a trace ID.

It is recommended to modify the logback.xml configuration to separate named loggers so we can use the standard appenders to offload audit logs, console logs, traces, etc. to different log stores for different purposes.

Note that this service uses Spring Boot Actuator to expose a health endpoint that can be used by Kubernetes for its readiness and liveliness probes once you containerize this service and stand it up in a pod.
