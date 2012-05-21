package org.hibernate.forge.datasource;

import javax.inject.Inject;

import org.jboss.forge.shell.completer.SimpleTokenCompleter;

public class DataSourceNameCompleter extends SimpleTokenCompleter
{
   
   @Inject
   private DataSourceHelper dataSourceHelper;
   
   @Override
   public Iterable<?> getCompletionTokens()
   {
      return dataSourceHelper.loadDataSources().keySet();
   }

}
