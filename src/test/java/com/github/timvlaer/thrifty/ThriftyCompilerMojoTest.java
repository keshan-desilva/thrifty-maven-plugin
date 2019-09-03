package com.github.timvlaer.thrifty;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Files;

import static com.github.timvlaer.thrifty.plugin.TaggedUnionMethodTypeProcessor.TAG_METHOD_NAME;
import static com.github.timvlaer.thrifty.plugin.TaggedUnionMethodTypeProcessor.VALUE_METHOD_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ThriftyCompilerMojoTest {

  private ThriftyCompilerMojo mojo = new ThriftyCompilerMojo();

  @Rule
  public TemporaryFolder outputFolder = new TemporaryFolder();
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private MavenProject project;

  @Before
  public void setUp() {
    mojo.setProject(project);
    mojo.setOutputDirectory(outputFolder.getRoot().getAbsolutePath());
  }

  @Test
  public void execute() throws Exception {
    mojo.setThriftFiles(new File[]{new File("src/test/resources/testcase.thrift")});

    mojo.execute();

    Mockito.verify(project).addCompileSourceRoot(outputFolder.getRoot().getAbsolutePath());

    File resultFile = new File(outputFolder.getRoot(), "com/sentiance/thrift/LocationFix.java");
    assertThat(resultFile).exists();

    String result = new String(Files.readAllBytes(resultFile.toPath()), UTF_8);
    assertThat(result).contains("package com.sentiance.thrift;");
    assertThat(result).contains("public final class LocationFix implements Struct");
  }

  @Test
  public void executeWithProcessor() throws Exception {
    mojo.setThriftFiles(new File[]{new File("src/test/resources/union.thrift")});

    mojo.execute();

    Mockito.verify(project).addCompileSourceRoot(outputFolder.getRoot().getAbsolutePath());

    File codeForUnion = new File(outputFolder.getRoot(), "com/sentiance/thrift/TestUnion.java");
    assertThat(codeForUnion).exists();

    String result = new String(Files.readAllBytes(codeForUnion.toPath()), UTF_8);
    assertThat(result).contains("public String " + TAG_METHOD_NAME + "() {\n" +
        "    if (value1 != null) return \"value1\";\n" +
        "    if (value2 != null) return \"value2\";\n" +
        "    if (value3 != null) return \"value3\";\n" +
        "    throw new IllegalStateException(\"Union type should have one value!\");\n" +
        "  }");
    assertThat(result).contains("public Object " + VALUE_METHOD_NAME + "() {\n" +
        "    if (value1 != null) return value1;\n" +
        "    if (value2 != null) return value2;\n" +
        "    if (value3 != null) return value3;\n" +
        "    throw new IllegalStateException(\"Union type should have one value!\");\n" +
        "  }");

    File codeForStruct = new File(outputFolder.getRoot(), "com/sentiance/thrift/TestStruct.java");
    assertThat(codeForStruct).exists();
    result = new String(Files.readAllBytes(codeForStruct.toPath()), UTF_8);
    assertThat(result).doesNotContain(TAG_METHOD_NAME + "()");
    assertThat(result).doesNotContain(VALUE_METHOD_NAME + "()");
  }

}