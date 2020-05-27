package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.Operation;
import org.apache.commons.lang3.BooleanUtils;
import org.openapitools.codegen.*;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.parameters.Parameter;

import java.io.File;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import org.openapitools.codegen.meta.features.DocumentationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaQuarkusServerCodegen extends AbstractJavaJAXRSServerCodegen {
    public static final String PROJECT_NAME = "projectName";
    private String openApiSpecFileLocation = "src/main/openapi/openapi.yaml";

    static Logger LOGGER = LoggerFactory.getLogger(JavaQuarkusServerCodegen.class);

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getName() {
        return "java-quarkus";
    }

    @Override
    public String getHelp() {
        return "Generates a java-quarkus server.";
    }

    public JavaQuarkusServerCodegen() {
        super();
        artifactId = "openapi-quarkus-server";
        outputFolder = "generated-code" + File.separator + "java-quarkus";

        updateOption(CodegenConstants.ARTIFACT_ID, this.getArtifactId());

        apiTemplateFiles.put("apiService.mustache", ".java");
        apiTemplateFiles.put("apiServiceImpl.mustache", ".java");
        apiTestTemplateFiles.clear(); // TODO: add test template
        embeddedTemplateDir = templateDir = "java-quarkus-server";

        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        // clear model and api doc template as AbstractJavaJAXRSServerCodegen
        // does not support auto-generated markdown doc at the moment
        //TODO: add doc templates
        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");

        // TODO: Fill this out.
    }

    @Override
    public void processOpts() {
        super.processOpts();

        supportingFiles.add(new SupportingFile("pom.mustache", "", "pom.xml")
                .doNotOverwrite());
        supportingFiles.add(new SupportingFile("gradle.mustache", "", "build.gradle")
                .doNotOverwrite());
        supportingFiles.add(new SupportingFile("settingsGradle.mustache", "", "settings.gradle")
                .doNotOverwrite());
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md")
                .doNotOverwrite());

        supportingFiles.add(new SupportingFile("ApiException.mustache",
                (sourceFolder + '/' + apiPackage).replace(".", "/"), "ApiException.java")
                .doNotOverwrite());
        supportingFiles.add(new SupportingFile("ApiOriginFilter.mustache",
                (sourceFolder + '/' + apiPackage).replace(".", "/"), "ApiOriginFilter.java")
                .doNotOverwrite());
        supportingFiles.add(new SupportingFile("ApiResponseMessage.mustache",
                (sourceFolder + '/' + apiPackage).replace(".", "/"), "ApiResponseMessage.java")
                .doNotOverwrite());
        supportingFiles.add(new SupportingFile("NotFoundException.mustache",
                (sourceFolder + '/' + apiPackage).replace(".", "/"), "NotFoundException.java")
                .doNotOverwrite());


        supportingFiles.add(new SupportingFile("RestApplication.mustache",
                (sourceFolder + '/' + invokerPackage).replace(".", "/"), "RestApplication.java")
                .doNotOverwrite());
        supportingFiles.add(new SupportingFile("StringUtil.mustache",
                (sourceFolder + '/' + invokerPackage).replace(".", "/"), "StringUtil.java"));
        supportingFiles.add(new SupportingFile("JacksonConfig.mustache",
                (sourceFolder + '/' + invokerPackage).replace(".", "/"), "JacksonConfig.java"));
        supportingFiles.add(new SupportingFile("RFC3339DateFormat.mustache",
                (sourceFolder + '/' + invokerPackage).replace(".", "/"), "RFC3339DateFormat.java"));

        if ("joda".equals(dateLibrary)) {
            supportingFiles.add(new SupportingFile("JodaDateTimeProvider.mustache",
                    (sourceFolder + '/' + apiPackage).replace(".", "/"), "JodaDateTimeProvider.java"));
            supportingFiles.add(new SupportingFile("JodaLocalDateProvider.mustache",
                    (sourceFolder + '/' + apiPackage).replace(".", "/"), "JodaLocalDateProvider.java"));
        } else if (dateLibrary.startsWith("java8")) {
            supportingFiles.add(new SupportingFile("OffsetDateTimeProvider.mustache",
                    (sourceFolder + '/' + apiPackage).replace(".", "/"), "OffsetDateTimeProvider.java"));
            supportingFiles.add(new SupportingFile("LocalDateProvider.mustache",
                    (sourceFolder + '/' + apiPackage).replace(".", "/"), "LocalDateProvider.java"));
        }


        supportingFiles.add(new SupportingFile("application.properties.mustache", "src/main/resources", "application.properties")
                .doNotOverwrite());
        supportingFiles.add(new SupportingFile("Dockerfile.jvm.mustache", "src/main/docker", "Dockerfile.jvm")
                .doNotOverwrite());
        supportingFiles.add(new SupportingFile("Dockerfile.native.mustache", "src/main/docker", "Dockerfile.native")
                .doNotOverwrite());
        supportingFiles.add(new SupportingFile("dockerignore.mustache", "", ".dockerignore")
                .doNotOverwrite());

        openApiSpecFileLocation = "src/main/resources/META-INF/openapi.yaml";
        if (StringUtils.isNotEmpty(openApiSpecFileLocation)) {
            int index = openApiSpecFileLocation.lastIndexOf('/');
            String fileFolder;
            String fileName;
            if (index >= 0) {
                fileFolder = openApiSpecFileLocation.substring(0, index);
                fileName = openApiSpecFileLocation.substring(index + 1);
            } else {
                fileFolder = "";
                fileName = openApiSpecFileLocation;
            }
            supportingFiles.add(new SupportingFile("openapi.mustache", fileFolder, fileName));
        }


    }

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
        String basePath = resourcePath;
        if (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }
        int pos = basePath.indexOf("/");
        if (pos > 0) {
            basePath = basePath.substring(0, pos);
        }

        if (StringUtils.isEmpty(basePath)) {
            basePath = "default";
        } else {
            if (co.path.startsWith("/" + basePath)) {
                co.path = co.path.substring(("/" + basePath).length());
            }
            co.subresourceOperation = !co.path.isEmpty();
        }
        List<CodegenOperation> opList = operations.get(basePath);
        if (opList == null || opList.isEmpty()) {
            opList = new ArrayList<CodegenOperation>();
            operations.put(basePath, opList);
        }
        opList.add(co);
        co.baseName = basePath;
    }

    @Override
    public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> objs, List<Object> allModels) {
        return super.postProcessOperationsWithModels(objs, allModels);
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        //Add imports for Jackson
        if (!BooleanUtils.toBoolean(model.isEnum)) {
            model.imports.add("JsonProperty");

            if (BooleanUtils.toBoolean(model.hasEnums)) {
                model.imports.add("JsonValue");
            }
        }
    }

    @Override
    public Map<String, Object> postProcessModelsEnum(Map<String, Object> objs) {
        objs = super.postProcessModelsEnum(objs);

        //Add imports for Jackson
        List<Map<String, String>> imports = (List<Map<String, String>>) objs.get("imports");
        List<Object> models = (List<Object>) objs.get("models");
        for (Object _mo : models) {
            Map<String, Object> mo = (Map<String, Object>) _mo;
            CodegenModel cm = (CodegenModel) mo.get("model");
            // for enum model
            if (Boolean.TRUE.equals(cm.isEnum) && cm.allowableValues != null) {
                cm.imports.add(importMapping.get("JsonValue"));
                Map<String, String> item = new HashMap<String, String>();
                item.put("import", importMapping.get("JsonValue"));
                imports.add(item);
            }
        }

        return objs;
    }


    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        generateYAMLSpecFile(objs);
        return super.postProcessSupportingFileData(objs);
    }
}
