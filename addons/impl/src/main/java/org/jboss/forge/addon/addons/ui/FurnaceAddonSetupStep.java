/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.addons.ui;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.jboss.forge.addon.addons.project.AddonProjectConfigurator;
import org.jboss.forge.addon.addons.project.FurnaceAddonProjectType;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.repositories.AddonRepository;
import org.jboss.forge.furnace.versions.Version;
import org.jboss.forge.furnace.versions.Versions;

/**
 * Called when the Next button is pressed and the {@link FurnaceAddonProjectType} is selected in NewProjectWizard
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 *
 */
public class FurnaceAddonSetupStep extends AbstractUICommand implements UIWizardStep
{
   private static final String DEFAULT_CONTAINER_NAME = "org.jboss.forge.furnace.container:cdi";
   private static final String DEFAULT_DEPENDENCY_NAME = "org.jboss.forge.addon:core";

   @Inject
   @WithAttributes(label = "Furnace container", required = true, requiredMessage = "You must select one Furnace container")
   private UISelectOne<AddonId> furnaceContainer;

   @Inject
   @WithAttributes(label = "Create API, Impl, SPI, Tests, and Addon modules")
   private UIInput<Boolean> splitProjects;

   @Inject
   @WithAttributes(label = "Depend on these addons")
   private UISelectMany<AddonId> addons;

   @Inject
   private Furnace furnace;

   @Inject
   private AddonProjectConfigurator addonProjectFactory;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.forCommand(getClass()).name("Furnace Addon Setup")
               .description("Enable Furnace Addon development in your project.")
               .category(Categories.create("Project", "Furnace"));
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      configureAddonDependencies();

      builder.add(furnaceContainer).add(splitProjects).add(addons);
   }

   private void configureAddonDependencies()
   {
      Set<AddonId> addonChoices = new TreeSet<>();
      Set<AddonId> containerChoices = new TreeSet<>();
      AddonId defaultContainer = null;
      AddonId defaultDependency = null;
      for (AddonRepository repository : furnace.getRepositories())
      {
         for (AddonId id : repository.listEnabled())
         {
            if (DEFAULT_CONTAINER_NAME.equals(id.getName()))
            {
               defaultContainer = id;
            }
            else if (DEFAULT_DEPENDENCY_NAME.equals(id.getName()))
            {
               defaultDependency = id;
            }
            // TODO: Furnace should provide some way to detect if an addon is a Container type
            boolean isContainerAddon = id.getName().contains("org.jboss.forge.furnace.container");
            if (isContainerAddon)
            {
               containerChoices.add(id);
            }
            else
            {
               addonChoices.add(id);
            }
         }
      }
      furnaceContainer.setValueChoices(containerChoices).setDefaultValue(defaultContainer);
      addons.setValueChoices(addonChoices);
      if (defaultDependency != null)
         addons.setDefaultValue(Arrays.asList(defaultDependency));
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      final Project project = (Project) context.getUIContext().getAttributeMap().get(Project.class);
      Set<AddonId> dependencyAddons = new TreeSet<>();
      if (addons.hasValue() || addons.hasDefaultValue())
      {
         for (AddonId id : addons.getValue())
         {
            dependencyAddons.add(id);
         }
      }
      dependencyAddons.add(furnaceContainer.getValue());
      Version forgeVersion = Versions.getImplementationVersionFor(getClass());
      if (splitProjects.getValue())
      {
         addonProjectFactory.setupComplexAddonProject(project, forgeVersion, dependencyAddons);
      }
      else
      {
         addonProjectFactory.setupSimpleAddonProject(project, forgeVersion, dependencyAddons);
      }

      return Results.success();
   }

   @Override
   public NavigationResult next(UINavigationContext context) throws Exception
   {
      return null;
   }
}