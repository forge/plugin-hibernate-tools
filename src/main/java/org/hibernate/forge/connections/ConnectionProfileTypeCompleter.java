package org.hibernate.forge.connections;

import org.jboss.forge.shell.completer.SimpleTokenCompleter;

public class ConnectionProfileTypeCompleter extends SimpleTokenCompleter
{
   
   @Override
   public Iterable<?> getCompletionTokens()
   {
      return ConnectionProfileType.allTypes().keySet();
   }

}
