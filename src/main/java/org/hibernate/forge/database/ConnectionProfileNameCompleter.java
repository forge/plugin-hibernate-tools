package org.hibernate.forge.database;

import javax.inject.Inject;

import org.jboss.forge.shell.completer.SimpleTokenCompleter;

public class ConnectionProfileNameCompleter extends SimpleTokenCompleter
{
   
   @Inject
   private ConnectionProfileHelper dataSourceHelper;
   
   @Override
   public Iterable<?> getCompletionTokens()
   {
      return dataSourceHelper.loadConnectionProfiles().keySet();
   }

}
