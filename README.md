# aws-lambda-spring-cloud-function


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

## Deploying the AWS Lambda

````bash
sam deploy --region <Your Region>
````

### Delete the AWS Lambda

````bash
sam delete SpringCloudFunctionLambda
````
