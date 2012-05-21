package org.hibernate.forge.datasource;

import org.jboss.forge.shell.completer.SimpleTokenCompleter;

public class DataSourceTypeCompleter extends SimpleTokenCompleter
{
   
   @Override
   public Iterable<?> getCompletionTokens()
   {
      return DataSourceType.allTypes().keySet();
   }

}
