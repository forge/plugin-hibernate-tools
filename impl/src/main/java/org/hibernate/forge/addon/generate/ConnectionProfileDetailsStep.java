package org.hibernate.forge.addon.generate;

import java.io.File;
import java.util.Properties;

import javax.inject.Inject;

import org.hibernate.forge.addon.connections.ConnectionProfile;
import org.hibernate.forge.addon.connections.ConnectionProfileDetailsPage;
import org.hibernate.forge.addon.connections.ConnectionProfileManager;
import org.hibernate.forge.addon.connections.HibernateDialect;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

public class ConnectionProfileDetailsStep extends ConnectionProfileDetailsPage implements UIWizardStep
{

   private static String NAME = "Connection Profile Details";
   private static String DESCRIPTION = "Edit the connection profile details";

   @Inject
   private ConnectionProfileManager manager;

   @Inject
   private GenerateEntitiesCommandDescriptor descriptor;
   
   @Inject 
   private ResourceFactory factory;
   
   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata
               .forCommand(getClass())
               .name(NAME)
               .description(DESCRIPTION);
   }

   @Override
   public boolean isEnabled(UIContext context)
   {
      return true;
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      super.initializeUI(builder);
      ConnectionProfile cp =
               manager.loadConnectionProfiles().get(
                        descriptor.connectionProfileName);
      if (cp != null)
      {
         jdbcUrl.setValue(cp.url);
         userName.setValue(cp.user);
         userPassword.setValue(cp.password);
         hibernateDialect.setValue(HibernateDialect.fromClassName(cp.dialect));
         driverLocation.setValue(createResource(cp.path));
         driverClass.setValue(cp.driver);
      }
   }

   @Override
   public Result execute(UIContext context)
   { 
      return Results.success();
   }

   @Override
   public NavigationResult next(UIContext context) throws Exception
   {
      return Results.navigateTo(DatabaseTableSelectionStep.class);
   }

   @Override
   public void validate(UIValidationContext context)
   {
      super.validate(context);
      descriptor.urls = urls;
      descriptor.driver = driver;
      descriptor.connectionProperties = createConnectionProperties();
   }
   
   private Properties createConnectionProperties() {
      Properties result = new Properties();
      result.setProperty("hibernate.connection.driver_class", driverClass.getValue());
      result.setProperty("hibernate.connection.username", userName.getValue());
      result.setProperty("hibernate.dialect", hibernateDialect.getValue().getClassName());
      result.setProperty("hibernate.connection.password",
               userPassword.getValue() == null ? "" : userPassword.getValue());
      result.setProperty("hibernate.connection.url", jdbcUrl.getValue());
      return result;
   }
   
   private FileResource<?> createResource(String fullPath) {
      return (FileResource<?>)factory.create(new File(fullPath));
   }
   
}
