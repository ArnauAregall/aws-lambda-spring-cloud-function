build-SpringCloudFunctionLambda:
	./gradlew --no-daemon clean nativeCompile
	echo '#!/bin/sh' > ./build/bootstrap
	echo 'set -euo pipefail' >> ./build/bootstrap
	echo './aws-lambda-spring-cloud-function' >> ./build/bootstrap
	chmod +x ./build/bootstrap
	cp ./build/bootstrap $(ARTIFACTS_DIR)
	cp ./build/native/nativeCompile/aws-lambda-spring-cloud-function $(ARTIFACTS_DIR)