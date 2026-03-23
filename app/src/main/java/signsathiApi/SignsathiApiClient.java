/*
 * Copyright 2010-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package signsathiApi;

import java.util.*;



@com.amazonaws.mobileconnectors.apigateway.annotation.Service(endpoint = "https://5lxpwrx38a.execute-api.ap-south-1.amazonaws.com/dev")
public interface SignsathiApiClient {


    /**
     * A generic invoker to invoke any API Gateway endpoint.
     * @param request
     * @return ApiResponse
     */
    com.amazonaws.mobileconnectors.apigateway.ApiResponse execute(com.amazonaws.mobileconnectors.apigateway.ApiRequest request);
    
    /**
     * 
     * 
     * @return void
     */
    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "/lessons", method = "OPTIONS")
    void lessonsOptions();
    
    /**
     * 
     * 
     * @param proxy 
     * @return void
     */
    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "/lessons/{proxy+}", method = "OPTIONS")
    void lessonsProxyOptions(
            @com.amazonaws.mobileconnectors.apigateway.annotation.Parameter(name = "proxy", location = "path")
            String proxy);
    
    /**
     * 
     * 
     * @return void
     */
    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "/progress", method = "OPTIONS")
    void progressOptions();
    
    /**
     * 
     * 
     * @param proxy 
     * @return void
     */
    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "/progress/{proxy+}", method = "OPTIONS")
    void progressProxyOptions(
            @com.amazonaws.mobileconnectors.apigateway.annotation.Parameter(name = "proxy", location = "path")
            String proxy);
    
}

