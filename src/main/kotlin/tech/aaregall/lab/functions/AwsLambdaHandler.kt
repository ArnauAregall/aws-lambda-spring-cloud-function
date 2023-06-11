package tech.aaregall.lab.functions

import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler.getAwsProxyHandler
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import java.io.InputStream
import java.io.OutputStream

class AwsLambdaHandler : RequestStreamHandler {

    private val handler = getAwsProxyHandler(App::class.java)!!

    override fun handleRequest(input: InputStream?, output: OutputStream?, context: Context?) {
        handler.proxyStream(input, output, context)
    }

}