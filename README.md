# aws-lambda-spring-cloud-function

Demo project to showcase how to build an AWS Lambda using:

- Kotlin
- Gradle
- GraalVM
- Spring Boot 3
- Spring Reactive Web (WebFlux, Netty framework)
- Spring Cloud Function
- OpenMeteo API
- OpenAI API
- Docker & Testcontainers

Articles that were written based in this project:

- [Exploring Spring Cloud Function, AI and AWS Lambda: building a native serverless application with Kotlin powered by OpenAI.](https://aregall.tech/aws-lambda-spring-cloud-function-kotlin-graalvm-native-openai)

----

## Requirements

The application requires JDK 17 at least on a GraalVM distribution.

````shell
$ sdk install java 22.3.r17-grl
$ sdk use java 22.3.r17-grl
````
----

## Build and test

````
./gradlew build
````
----
## Running the application

### On a JDK

````
./gradlew bootRun
````

### Natively

````
./gradlew nativeRun
````

## Building the native AWS Lambda artifact with SAM

**Linux**:

````bash
sam build
````

**macOS**:

AWS Lambda requires native applications to be built in an Amazon Linux compatible OS, so on macOS we need to run the build
in a compatible Docker container.

1. Build the Docker image compatible with AWS Linux:

    ````bash
    ./aws-image/build-aws-image.sh
    ````

2. Run `sam build` specifying the Docker image:

    ````bash
    sam build --use-container --build-image tech.aaregall.lab/amazonlinux-graalvm:latest
    ````

## Deploy the application as an AWS Lambda using SAM

Define the following environment variables on the deployment host.

````bash
export AWS_REGION=...
export OPENAI_API_KEY=...
````

### Deploy the Lambda application

````bash
sam deploy --region $AWS_REGION --parameter-overrides ParameterKey=OpenAiApiKey,ParameterValue=$OPENAI_API_KEY
````

### See CloudWatch logs

````bash
sam logs -n SpringCloudFunctionLambda --stack-name aws-lambda-spring-cloud-function
````

### Delete the Lambda application

````bash
sam delete SpringCloudFunctionLambda
````
