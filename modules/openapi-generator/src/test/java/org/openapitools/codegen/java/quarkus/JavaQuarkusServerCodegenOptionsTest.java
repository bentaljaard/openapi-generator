package org.openapitools.codegen.java.quarkus;

import org.openapitools.codegen.AbstractOptionsTest;
import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.languages.JavaQuarkusServerCodegen;
import org.openapitools.codegen.options.JavaQuarkusServerCodegenOptionsProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JavaQuarkusServerCodegenOptionsTest extends AbstractOptionsTest {
    private JavaQuarkusServerCodegen codegen = mock(JavaQuarkusServerCodegen.class, mockSettings);

    public JavaQuarkusServerCodegenOptionsTest() {
        super(new JavaQuarkusServerCodegenOptionsProvider());
    }

    @Override
    protected CodegenConfig getCodegenConfig() {
        return codegen;
    }

    @SuppressWarnings("unused")
    @Override
    protected void verifyOptions() {
        // TODO: Complete options using Mockito
        // verify(codegen).someMethod(arguments)
    }
}

