package com.intellij.aws.cloudformation.metadata

data class CloudFormationManualResourceType(
    val name: String,
    val url: String,
    val description: String,
    val properties: List<CloudFormationManualResourceProperty>,
    val attributes: List<CloudFormationManualResourceAttribute> = emptyList()
) {
}

data class CloudFormationManualResourceProperty(
    val name: String,
    val type: String,
    val description: String,
    val required: Boolean = false,
    val url: String? = null,
    val updateRequires: String? = null,
    val excludedFromGlobals: Boolean = false
)

data class CloudFormationManualResourceAttribute(
    val name: String,
    val description: String
)

// from https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md

val awsServerlessFunction = CloudFormationManualResourceType(
    name = "AWS::Serverless::Function",
    url = "https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#awsserverlessfunction",
    description = "Creates a Lambda function, IAM execution role, and event source mappings which trigger the function.",
    properties = listOf(
        CloudFormationManualResourceProperty(name = "Handler", type = "string", description = "Function within your code that is called to begin execution", required = true),
        CloudFormationManualResourceProperty(name = "Runtime", type = "string", description = "The runtime environment", required = true),
        CloudFormationManualResourceProperty(name = "CodeUri", type = "string | S3 Location Object", description = "S3 Uri or location to the function code. The S3 object this Uri references MUST be a Lambda deployment package.", required = true),
        CloudFormationManualResourceProperty(name = "FunctionName", type = "string", description = "A name for the function. If you don't specify a name, a unique name will be generated for you. More Info", excludedFromGlobals = true),
        CloudFormationManualResourceProperty(name = "Description", type = "string", description = "Description of the function."),
        CloudFormationManualResourceProperty(name = "MemorySize", type = "integer", description = "Size of the memory allocated per invocation of the function in MB. Defaults to 128."),
        CloudFormationManualResourceProperty(name = "Timeout", type = "integer", description = "Maximum time that the function can run before it is killed in seconds. Defaults to 3."),
        CloudFormationManualResourceProperty(name = "Role", type = "string", description = "ARN of an IAM role to use as this function's execution role. If omitted, a default role is created for this function.", excludedFromGlobals = true),
        CloudFormationManualResourceProperty(name = "Policies", type = "string | List of string | IAM policy document object | List of IAM policy document object", description = "Names of AWS managed IAM policies or IAM policy documents that this function needs, which should be appended to the default role for this function. If the Role property is set, this property has no meaning.", excludedFromGlobals = true),
        CloudFormationManualResourceProperty(name = "Environment", type = "Function environment object", description = "Configuration for the runtime environment."),
        CloudFormationManualResourceProperty(name = "VpcConfig", type = "VPC config object", description = "Configuration to enable this function to access private resources within your VPC."),
        CloudFormationManualResourceProperty(name = "Events", type = "Map of string to Event source object", description = "A map (string to Event source object) that defines the events that trigger this function. Keys are limited to alphanumeric characters.", excludedFromGlobals = true),
        CloudFormationManualResourceProperty(name = "Tags", type = "Map of string to string", description = "A map (string to string) that specifies the tags to be added to this function. Keys and values are limited to alphanumeric characters. Keys can be 1 to 127 Unicode characters in length and cannot be prefixed with aws:. Values can be 1 to 255 Unicode characters in length. When the stack is created, SAM will automatically add a lambda:createdBy:SAM tag to this Lambda function."),
        CloudFormationManualResourceProperty(name = "Tracing", type = "string", description = "String that specifies the function's X-Ray tracing mode. Accepted values are Active and PassThrough"),
        CloudFormationManualResourceProperty(name = "KmsKeyArn", type = "string", description = "The Amazon Resource Name (ARN) of an AWS Key Management Service (AWS KMS) key that Lambda uses to encrypt and decrypt your function's environment variables."),
        CloudFormationManualResourceProperty(name = "DeadLetterQueue", type = "map | DeadLetterQueue Object", description = "Configures SNS topic or SQS queue where Lambda sends events that it can't process."),
        CloudFormationManualResourceProperty(name = "DeploymentPreference", type = "DeploymentPreference Object", description = "Settings to enable Safe Lambda Deployments. Read the usage guide for detailed information."),
        CloudFormationManualResourceProperty(name = "AutoPublishAlias", type = "string", description = "Name of the Alias. Read AutoPublishAlias Guide for how it works"),
        CloudFormationManualResourceProperty(name = "ReservedConcurrentExecutions", type = "integer", description = "The maximum of concurrent executions you want to reserve for the function. For more information see AWS Documentation on managing concurrency", excludedFromGlobals = true)
    ),
    attributes = listOf(
        CloudFormationManualResourceAttribute("Arn", "The ARN of the Lambda function.")
    )
)

val awsServerlessApi = CloudFormationManualResourceType(
    name = "AWS::Serverless::Api",
    url = "https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#awsserverlessapi",
    description = "Creates a collection of Amazon API Gateway resources and methods that can be invoked through HTTPS endpoints.",
    properties = listOf(
        CloudFormationManualResourceProperty(name = "Name", type = "string", description = "A name for the API Gateway RestApi resource"),
        CloudFormationManualResourceProperty(name = "StageName", type = "string", description = "The name of the stage, which API Gateway uses as the first path segment in the invoke Uniform Resource Identifier (URI).", required = true, excludedFromGlobals = true),
        CloudFormationManualResourceProperty(name = "DefinitionUri", type = "string | S3 Location Object", description = "S3 URI or location to the Swagger document describing the API. Either one of DefinitionUri or DefinitionBody must be specified."),
        CloudFormationManualResourceProperty(name = "DefinitionBody", type = "JSON or YAML Object", description = "Swagger specification that describes your API. Either one of DefinitionUri or DefinitionBody must be specified.", excludedFromGlobals = true),
        CloudFormationManualResourceProperty(name = "CacheClusterEnabled", type = "boolean", description = "Indicates whether cache clustering is enabled for the stage."),
        CloudFormationManualResourceProperty(name = "CacheClusterSize", type = "string", description = "The stage's cache cluster size."),
        CloudFormationManualResourceProperty(name = "Variables", type = "string", description = "A map (string to string map) that defines the stage variables, where the variable name is the key and the variable value is the value. Variable names are limited to alphanumeric characters. Values must match the following regular expression: [A-Za-z0-9._~:/?#&amp;=,-]+."),
        CloudFormationManualResourceProperty(name = "MethodSettings", type = "CloudFormation MethodSettings property", description = "Configures all settings for API stage including Logging, Metrics, CacheTTL, Throttling. This value is passed through to CloudFormation. So any values supported by CloudFormation MethodSettings property can be used here."),
        CloudFormationManualResourceProperty(name = "EndpointConfiguration", type = "string", description = "string\tSpecify the type of endpoint for API endpoint. Value is either REGIONAL or EDGE."),
        CloudFormationManualResourceProperty(name = "BinaryMediaTypes", type = "List of string", description = "List of MIME types that your API could return. Use this to enable binary support for APIs. Use ~1 instead of / in the mime types (See examples in template.yaml)."),
        CloudFormationManualResourceProperty(name = "Cors", type = "string or Cors Configuration", description = "Enable CORS for all your APIs. Specify the domain to allow as a string or specify a dictionary with additional Cors Configuration. NOTE: Cors requires SAM to modify your Swagger definition. Hence it works only inline swagger defined with DefinitionBody.")
    )
)

val awsServerlessSimpleTable = CloudFormationManualResourceType(
    name = "AWS::Serverless::SimpleTable",
    url = "https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#awsserverlesssimpletable",
    description = "The AWS::Serverless::SimpleTable resource creates a DynamoDB table with a single attribute primary key. It is useful when data only needs to be accessed via a primary key. To use the more advanced functionality of DynamoDB, use an AWS::DynamoDB::Table resource instead.",
    properties = listOf(
        CloudFormationManualResourceProperty(name = "PrimaryKey", type = "Primary Key Object", description = "Attribute name and type to be used as the table's primary key. This cannot be modified without replacing the resource. Defaults to String attribute named ID.", excludedFromGlobals = true),
        CloudFormationManualResourceProperty(name = "ProvisionedThroughput", type = "Provisioned Throughput Object", description = "Read and write throughput provisioning information. Defaults to 5 read and 5 write capacity units per second.", excludedFromGlobals = true),
        CloudFormationManualResourceProperty(name = "Tags", type = "Map of string to string", description = "A map (string to string) that specifies the tags to be added to this table. Keys and values are limited to alphanumeric characters.", excludedFromGlobals = true),
        CloudFormationManualResourceProperty(name = "TableName", type = "string", description = "Name for the DynamoDB Table", excludedFromGlobals = true),
        CloudFormationManualResourceProperty(name = "SSESpecification", type = "DynamoDB SSESpecification", description = "Specifies the settings to enable server-side encryption.")
    )
)

val awsServerless20161031ResourceTypes = listOf(awsServerlessFunction, awsServerlessApi, awsServerlessSimpleTable)
