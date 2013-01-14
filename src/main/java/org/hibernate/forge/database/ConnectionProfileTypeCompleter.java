package org.hibernate.forge.database;

import org.jboss.forge.shell.completer.SimpleTokenCompleter;

public class ConnectionProfileTypeCompleter extends SimpleTokenCompleter
{
   
   @Override
   public Iterable<?> getCompletionTokens()
   {
      return ConnectionProfileType.allTypes().keySet();
   }

}
