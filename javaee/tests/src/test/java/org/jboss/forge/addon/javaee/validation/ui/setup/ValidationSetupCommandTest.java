package org.jboss.forge.addon.javaee.validation.ui.setup;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.javaee.ProjectHelper;
import org.jboss.forge.addon.javaee.validation.ValidationFacet;
import org.jboss.forge.addon.javaee.validation.ui.ValidationProviderSetupCommand;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.command.AbstractCommandExecutionListener;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ValidationSetupCommandTest
{

   @Deployment
   @AddonDependencies({
            @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
            @AddonDependency(name = "org.jboss.forge.addon:shell-test-harness"),
            @AddonDependency(name = "org.jboss.forge.addon:javaee"),
            @AddonDependency(name = "org.jboss.forge.addon:maven"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
   })
   public static AddonArchive getDeployment()
   {
      return ShrinkWrap.create(AddonArchive.class).addBeansXML().addClass(ProjectHelper.class);
   }

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private UITestHarness uiTestHarness;

   @Inject
   private ShellTest shellTest;

   private Project project;

   @Before
   public void setUp()
   {
      project = projectFactory.createTempProject();
   }

   @Test
   public void checkCommandMetadata() throws Exception
   {
      try (CommandController controller = uiTestHarness.createCommandController(ValidationProviderSetupCommand.class,
               project.getRoot()))
      {
         controller.initialize();
         // Checks the command metadata
         assertTrue(controller.getCommand() instanceof ValidationProviderSetupCommand);
         UICommandMetadata metadata = controller.getMetadata();
         assertEquals("Constraint: Setup", metadata.getName());
         assertEquals("Java EE", metadata.getCategory().getName());
         assertEquals("Bean Validation", metadata.getCategory().getSubCategory().getName());
         assertEquals(5, controller.getInputs().size());
         assertFalse("Project is created, shouldn't have targetLocation", controller.hasInput("targetLocation"));
         assertTrue(controller.hasInput("providers"));
         assertTrue(controller.hasInput("providedScope"));
         assertTrue(controller.hasInput("messageInterpolator"));
         assertTrue(controller.hasInput("traversableResolver"));
         assertTrue(controller.hasInput("constraintValidatorFactory"));
      }
   }

   @Test
   public void checkCommandShell() throws Exception
   {
      shellTest.getShell().setCurrentResource(project.getRoot());
      Result result = shellTest.execute(("constraint-setup"), 10, TimeUnit.SECONDS);

      Assert.assertThat(result, not(instanceOf(Failed.class)));
      Assert.assertTrue(project.hasFacet(ValidationFacet.class));
   }

   @Test
   public void testBeanValidationSetup() throws Exception
   {
      try (CommandController tester = uiTestHarness.createCommandController(ValidationProviderSetupCommand.class,
               project.getRoot()))
      {
         // Launch
         tester.initialize();

         Assert.assertTrue(tester.isValid());
         tester.setValueFor("providedScope", false);
         Assert.assertTrue(tester.isValid());

         final AtomicBoolean flag = new AtomicBoolean();
         tester.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener()
         {
            @Override
            public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result)
            {
               if (result.getMessage().equals("Bean Validation is installed."))
               {
                  flag.set(true);
               }
            }
         });
         tester.execute();
         // Ensure that the two pages were invoked
         Assert.assertEquals(true, flag.get());
      }
   }
}
