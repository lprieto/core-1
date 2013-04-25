/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.aesh;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.container.services.Exported;
import org.jboss.forge.resource.DirectoryResource;
import org.jboss.forge.ui.UICommand;
import org.jboss.forge.ui.context.UIBuilder;
import org.jboss.forge.ui.context.UIContext;
import org.jboss.forge.ui.context.UIValidationContext;
import org.jboss.forge.ui.impl.UIInputImpl;
import org.jboss.forge.ui.input.UICompleter;
import org.jboss.forge.ui.input.UIInput;
import org.jboss.forge.ui.input.InputComponent;
import org.jboss.forge.ui.metadata.UICommandMetadata;
import org.jboss.forge.ui.result.Result;
import org.jboss.forge.ui.result.Results;
import org.jboss.forge.ui.util.Metadata;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@Exported
public class FooCommand implements UICommand
{
   @Inject
   private UIInput<String> name;

   @Inject
   private UIInput<String> help;

   @Inject
   private UIInput<Boolean> bool;

   @Inject
   private UIInput<String> bar;

    @Inject
    private UIInput<String> bar2;

    @Inject
    private UIInput<DirectoryResource> targetLocation;

   @Override
   public UICommandMetadata getMetadata()
   {
      return Metadata.forCommand(getClass()).name("foo bar").description("Do some foo");
   }

   @Override
   public boolean isEnabled(UIContext context)
   {
      return true;
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
       name = new UIInputImpl<String>("name", String.class);
      name.setLabel("name the foo");
      name.setRequired(true);

       bar = new UIInputImpl<String>("bar", String.class);
       bar.setLabel("bar");
       bar.setDefaultValue("BAAAR");
       bar.setRequired(true);

       bar2 = new UIInputImpl<String>("bar2", String.class);
       bar2.setLabel("bar2");

       bool = new UIInputImpl<Boolean>("bool", Boolean.class);
       bool.setLabel("bool");

       help = new UIInputImpl<String>("help", String.class);
       help.setLabel("foo");

       targetLocation = new UIInputImpl<DirectoryResource>("targetLocation", DirectoryResource.class);
       targetLocation.setLabel("project location");

       /*
      foo.setCompleter(new UICompleter<String>()
      {
         @Override
         public Iterable<String> getCompletionProposals(InputComponent<?, String> input, String value)
         {
            List<String> out = new ArrayList<String>();
            out.add("foo1");
            return out;
         }
      });
      */

      builder.add(name).add(help).add(bool).add(bar).add(bar2).add(targetLocation);
   }

   @Override
   public void validate(UIValidationContext context)
   {
   }

   @Override
   public Result execute(UIContext context) throws Exception
   {
      return Results.success("boo");
   }
}